package sjtu.opennet.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingDeque;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.util.FileUtil;

public class VideoSendHelper {
    private static final String TAG = "=====VideoSendHelper";

    private Context context;
    private String threadId;
    private VideoMeta videoMeta;
    private int segmentTime = 3;

    private String videoCacheDir;
    private String chunkDir;
    private String m3u8Path;

    private String command;

    private ChunkSender chunkSender;
    HashSet<String> chunkNames = new HashSet<>();
    long currentIndex = 0;
    long startTime = 0;
    LinkedBlockingDeque<ChunkInfo> chunkQueue = new LinkedBlockingDeque<>();

    private ExecuteBinaryResponseHandler segHandler=new ExecuteBinaryResponseHandler(){
        @Override
        public void onFinish() {
            Log.d(TAG, "onFinish: finish segmenting " + videoMeta.getHash());
            chunkQueue.add(new ChunkInfo("VIRTUAL",0,0,0));
        }
    };

    public VideoSendHelper(Context context, String videoFilePath , int segmentTime , String threadId, ChunkSender chunkSender){
        this.context=context;
        this.threadId=threadId;
        this.chunkSender=chunkSender;
        this.segmentTime=segmentTime;

        // get metadata of the video with the inputted path
        String getVideoMetaCmd = String.format("-i %s -c copy -bsf:v h264_mp4toannexb -map 0 -f segment " +
                "-segment_time %d " +
                "-segment_list_size 1 ", videoFilePath,segmentTime);
        videoMeta=new VideoMeta(videoFilePath,getVideoMetaCmd.getBytes());

        // set the file paths
        String tmpPath = "video/"+videoMeta.getHash();
        videoCacheDir = FileUtil.getAppExternalPath(context, tmpPath);
        chunkDir = FileUtil.getAppExternalPath(context, tmpPath+"/chunks");
        m3u8Path = videoCacheDir+"/playlist.m3u8";

        videoMeta.saveThumbnail(videoCacheDir);

        command=String.format("-i %s -c copy -bsf:v h264_mp4toannexb -map 0 -f segment " +
                "-segment_time %d " +
                "-segment_list_size 10 " +
                "-segment_list %s " +
                "%s/out%%04d.ts", videoFilePath,segmentTime, m3u8Path, chunkDir);
    }


    public String getVideoId(){return videoMeta.getHash();}

    public Bitmap getPosterBitmap() { // temporarily not add the poster to ipfs
        return videoMeta.getPoster();
    }

    public void startSend(Handlers.VideoStartHandler videoStartHandler){
        videoStartHandler.startSend(videoMeta,threadId);

        FileObserver fileObserver=new FileObserver(videoCacheDir){
            @Override
            public void onEvent(int i, @Nullable String s) {
                if(i==MOVED_TO){ //如果监测到有ts文件生成
                    ArrayList<M3U8Util.ChunkInfo> infos=M3U8Util.getInfos(videoCacheDir+"/"+s);
                    synchronized (chunkNames){
                        for(M3U8Util.ChunkInfo chk:infos){
                            if(!chunkNames.contains(chk.filename)){
                                chunkNames.add(chk.filename);

                                // make chunkPbInfo
                                ChunkInfo chunkPbInfo=new ChunkInfo(chk.filename,startTime,startTime+chk.duration,currentIndex);
                                startTime+=chk.duration;
                                currentIndex++;
                                chunkQueue.add(chunkPbInfo);
                            }
                        }
                    }
                }
            }
        };
        new Thread(()->{
            fileObserver.startWatching(); // 监听文件夹的m3u8文件
        }).start();

        // 开始切割ts文件
        new Thread(()->{
            try {
                File outDirf = new File(chunkDir);
                if(!outDirf.exists()){
                    outDirf.mkdir();
                }
                Segmenter.segment(context, command, segHandler);
            } catch (Exception e) {
                Log.e(TAG, "Error occur when segment video.");
                e.printStackTrace();
            }
        }).start();

        // 开始发送ts文件
        new Thread(()->{
            while (true) {
                try {
                    ChunkInfo chunkInfo = chunkQueue.take(); // if queue is empty, thread will be blocked here
                    if(chunkInfo.chunkName.equals("VIRTUAL")){
                        chunkSender.finishSend(getVideoId(), chunkNames.size());
                        break;
                    }
                    String tsAbsolutePath = videoCacheDir + "/chunks/" + chunkInfo.chunkName;
                    chunkSender.sendChunk(videoMeta.getHash(),tsAbsolutePath, chunkInfo);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // at this time, task finish
            fileObserver.stopWatching();
            Log.d(TAG, "run: chunkadder finished");
        }).start();

        Log.d(TAG, "startSend over ");
    }
}
