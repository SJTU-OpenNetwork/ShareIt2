package sjtu.opennet.stream.video;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.concurrent.PriorityBlockingQueue;

import sjtu.opennet.hon.BaseTextileEventListener;
import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.util.FileUtil;
import sjtu.opennet.video.M3U8Util;
import sjtu.opennet.textilepb.Model;
import sjtu.opennet.textilepb.QueryOuterClass;

public class VideoGetter_tkt {
    private static final String TAG = "=============HONVIDEO.VideoGetter_tkt";
    private int tsCount=0;

    Handlers.VideoPrefetchHandler handler;
    boolean isPlaying=false;

    class ChunkCompare implements Comparable<ChunkCompare>{

        long chunkCompIndex;
        Model.VideoChunk videoChunk;

        public ChunkCompare(long chunkCompIndex, Model.VideoChunk videoChunk) {
            this.chunkCompIndex = chunkCompIndex;
            this.videoChunk = videoChunk;
        }

        @Override
        public int compareTo(ChunkCompare o) {
            if(this.chunkCompIndex < o.chunkCompIndex){
                return -1;
            }else if(this.chunkCompIndex > o.chunkCompIndex){
                return 1;
            }
            return 0;
        }
    }


    String videoId;
    Context context;
    Model.Video video;
    PriorityBlockingQueue<ChunkCompare> searchResults;
    ChunkSearchListener chunkSearchListener=new ChunkSearchListener();
    boolean finishSearch=false;
    boolean finishDownload=false;
    Object SEARCHLOCK=new Object();
    Object DOWNLOADLOCK=new Object();
    long chunkIndex=0;
    SearchThread searchThread;
    DownloadThread downloadThread;
    String dir;
    File m3u8file;

    public VideoGetter_tkt(Context context, String videoId) {
        this.context=context;
        this.videoId = videoId;
        Log.d(TAG, "VideoGetter_tkt: get the video: "+videoId);

        try {
            video= Textile.instance().videos.getVideo(videoId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        searchResults=new PriorityBlockingQueue<>();
        searchThread=new SearchThread();
        downloadThread=new DownloadThread();
    }

    public void startGet(Handlers.VideoPrefetchHandler handler){
        this.handler = handler;
        dir= FileUtil.getAppExternalPath(context, "video/"+videoId);
        if(!M3U8Util.existOrNot(dir)){
            Textile.instance().addEventListener(chunkSearchListener);
            m3u8file= M3U8Util.initM3u8(dir,videoId);
            searchThread.start();
            downloadThread.start();
        }else{
            m3u8file=new File(dir+"/"+videoId+".m3u8");
            Log.d(TAG, "startGet: m3u8path: "+m3u8file.getAbsolutePath());
        }
    }

    public void stopGet(){
        Log.d(TAG, "stopGet: 获取结束");
        Textile.instance().removeEventListener(chunkSearchListener);
        finishSearch=true;
//        if(!isPlaying){
//            handler.onPrefetch();
//        }
    }

    public Uri getUri(){
        return Uri.fromFile(m3u8file);
    }

    public String getM3u8Path(){
        return m3u8file.getAbsolutePath();
    }

    // search video chunk
    class SearchThread extends Thread{
        @Override
        public void run() {
            synchronized (SEARCHLOCK){
                while(!finishSearch){
                    Log.d(TAG, "run: will search : "+chunkIndex);
                    searchTheChunk(videoId,chunkIndex);
                    try {
                        Log.d(TAG, "run: wait search: "+chunkIndex);
                        SEARCHLOCK.wait(1200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public void searchTheChunk(String videoid, long chunkIndex){
        QueryOuterClass.QueryOptions options = QueryOuterClass.QueryOptions.newBuilder()
                .setWait(1)
                .setLimit(1)
                .build();
        QueryOuterClass.VideoChunkQuery query=QueryOuterClass.VideoChunkQuery.newBuilder()
                .setStartTime(-1)
                .setEndTime(-1)
                .setIndex(chunkIndex)
                .setId(videoid).build();
        try {
            Textile.instance().videos.searchVideoChunks(query, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    class ChunkSearchListener extends BaseTextileEventListener{
        @Override
        public void videoChunkQueryResult(String queryId, Model.VideoChunk vchunk) {
            Log.d(TAG, "videoChunkQueryResult: get search result: "+vchunk.getIndex()+" "+vchunk.getChunk());
            synchronized (SEARCHLOCK){
                Log.d(TAG, "videoChunkQueryResult, in lock: "+vchunk.getIndex()+" "+vchunk.getChunk()+" "+vchunk.getAddress());
                searchResults.add(new ChunkCompare(vchunk.getIndex(),vchunk));
                if(vchunk.getChunk().equals("VIRTUAL")){
                    stopGet();
                }
                chunkIndex++;
                SEARCHLOCK.notify();
            }
        }
    }

    // download video chunk
    class DownloadThread extends Thread{
        @Override
        public void run() {
            synchronized (DOWNLOADLOCK){
                while(!finishDownload){
                    try {
                        ChunkCompare chunkCompare=searchResults.take();
                        if(chunkCompare.videoChunk.getChunk().equals("VIRTUAL")){
                            finishDownload=true;
                            break;
                        }
                        Textile.instance().ipfs.dataAtPath(chunkCompare.videoChunk.getAddress(), new Handlers.DataHandler() {
                            @Override
                            public void onComplete(byte[] data, String media) {
                                // store to the chunks directory, and write m3u8
                                Log.d(TAG, "onComplete: get the ts file: "+chunkCompare.videoChunk.getChunk());
                                tsCount++;
//                                if(tsCount==1){
//                                    isPlaying=true;
//                                    handler.onPrefetch();
//                                }
                                synchronized (DOWNLOADLOCK){
                                    System.out.println("lock");
                                    String tsName=dir + "/" + chunkCompare.videoChunk.getChunk();
                                    FileUtil.writeByteArrayToFile(tsName,data);
                                    M3U8Util.writeM3u8(m3u8file,chunkCompare.videoChunk.getEndTime(),chunkCompare.videoChunk.getStartTime(),chunkCompare.videoChunk.getChunk());
                                    DOWNLOADLOCK.notify();
                                    System.out.println("unlock");
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                synchronized (DOWNLOADLOCK){
                                    searchResults.add(chunkCompare);
                                    DOWNLOADLOCK.notify();
                                }
                            }
                        });
                        DOWNLOADLOCK.wait();
                        Log.d(TAG, "run: get the compare: "+chunkCompare.chunkCompIndex);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.d(TAG, "run: finish downloading");
        }
    }
}
