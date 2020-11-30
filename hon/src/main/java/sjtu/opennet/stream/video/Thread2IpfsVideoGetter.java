package sjtu.opennet.stream.video;

import android.content.Context;
import android.net.Uri;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.util.Log;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.util.FileUtil;
import sjtu.opennet.video.ChunkInfo2;
import sjtu.opennet.video.M3U8Util;


public class Thread2IpfsVideoGetter {
    private static final String TAG = "=============HONVIDEO.VideoGetter_thread2";
    private int tsCount=0;//用于标识几段xmlMessage已经在处理
    Handlers.VideoPrefetchHandler handler;

    //PriorityBlockingQueue<VideoGetter_tkt.ChunkCompare> searchResults;
    DownloadThread downloadThread;
    SearchThread searchThread;
    PriorityBlockingQueue<ChunkCompare> searchResults;
    List<ChunkInfo2> chunksQueue=new LinkedList<>();
    String dir;
    String tsInfoPath;
    Context context;
    File m3u8file;
    String videoId;
    Object SEARCHLOCK=new Object();
    Object DOWNLOADLOCK=new Object();
    boolean finishSearch=false;
    boolean finishDownload=false;
    long chunkIndex=0;

    class ChunkCompare implements Comparable<ChunkCompare>{

        long chunkCompIndex;
        ChunkInfo2 videoChunk;

        public ChunkCompare(long chunkCompIndex, ChunkInfo2 videoChunk) {
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


    public Thread2IpfsVideoGetter(Context context, String videoId) {
        this.videoId = videoId;
        this.context = context;
        searchResults=new PriorityBlockingQueue<>();
        searchThread = new SearchThread();
        downloadThread=new DownloadThread();
       // downloadThread=new VideoGetter_tkt.DownloadThread();



    }

    public void startGet(Handlers.VideoPrefetchHandler handler){
        System.out.println("=======================Start get video chunks");
        this.handler = handler;
        tsInfoPath= FileUtil.getAppExternalPath(context, "videoChunks")+"/"+videoId+".txt";//更新的xml保存在这个文件里面
        //m3u8
        dir= FileUtil.getAppExternalPath(context, "video/"+videoId);
        if(!M3U8Util.existOrNot(dir)){//第一次点击视频
            // Textile.instance().addEventListener(chunkSearchListener);
            m3u8file= M3U8Util.initM3u8(dir,videoId);//创建一个以videoid为名字的m3u8文件

            searchThread.start();

            downloadThread.start();
        }else{
            m3u8file=new File(dir+"/"+videoId+".m3u8");
            Log.d(TAG, "startGet: m3u8path: "+m3u8file.getAbsolutePath());
        }
    }

    public void stopGet(){
        Log.d(TAG, "stopGet: 获取结束");
    }

    //search video chunkinfo from file, and listen xml file update.读取该文件，并将更新数据加入到队列
    class SearchThread extends Thread{
        @Override
        public void run() {
            File file = new File(tsInfoPath);
            if (file.exists()) {//如果文件存在，就直接读出xml内容
                System.out.println("=============检测到文件存在："+tsInfoPath);
                //String xmlMessage = txt2String(file);
                String xmlMessage = readToString(tsInfoPath);
                String[] xmlMessages = xmlMessage.split("##");
                System.out.println("============检测到文件中有"+xmlMessages.length+"段xml信息");
                for (String xmlInfo: xmlMessages){
                    getChunksInfo(xmlInfo);
                }
            }else {
                System.out.println("=============未检测到文件存在："+tsInfoPath);
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            //fileobserver 用来监视保存xml的文件的变化
            FileObserver fileObserver=new FileObserver(tsInfoPath){
                @Override
                public void onEvent(int i, @Nullable String s) {
                    if(i==MODIFY){ //如果监测到该文件修改
                        System.out.println("=============检测到文件修改:"+tsInfoPath);
                        String xmlMessage = readToString(tsInfoPath);
                        System.out.println("============文件内容为："+xmlMessage);

//                        try {
//                            Document doc=DocumentHelper.parseText(xmlMessage);
//
//                        } catch (DocumentException e) {
//                            e.printStackTrace();
//                        }


                       String[] xmlMessages = xmlMessage.split("##");
                       System.out.println("============检测到文件中有"+xmlMessages.length+"段xml信息");

                        String updateXmlMessage = xmlMessages[xmlMessages.length-1];
                        //只取最后一段xml信息
                        getChunksInfo(updateXmlMessage);
                    }
                }
            };
            fileObserver.startWatching();
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
                        if(chunkCompare.videoChunk.chunkHash.equals("VIRTUAL")){
                            finishDownload=true;
                            stopGet();
                            break;
                        }
                        Textile.instance().ipfs.dataAtPath(chunkCompare.videoChunk.chunkHash, new Handlers.DataHandler() {
                            @Override
                            public void onComplete(byte[] data, String media) {
                                // store to the chunks directory, and write m3u8
                                Log.d(TAG, "onComplete: get the ts file: "+chunkCompare.videoChunk.chunkName);
                                tsCount++;

                                synchronized (DOWNLOADLOCK){
                                    System.out.println("lock");
                                    String tsName=dir + "/" + chunkCompare.videoChunk.chunkName;
                                    FileUtil.writeByteArrayToFile(tsName,data);
//                                    M3U8Util.writeM3u8(m3u8file,chunkCompare.videoChunk.chunkEndTime,chunkCompare.videoChunk.chunkStartTime,chunkCompare.videoChunk.chunkHash);
                                    M3U8Util.onTsArriveThread2(m3u8file, videoId,chunkCompare.videoChunk.chunkEndTime,chunkCompare.videoChunk.chunkStartTime,chunkCompare.videoChunk.chunkName);
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

    public void getChunksInfo(String xmlString){
       // List<ChunkInfo2> chunks=new LinkedList<>();
        //解析xml 获取 ts信息, 存入chunks
        try{
            System.out.println("============messtring:"+xmlString);
            Document doc2;
            doc2 = DocumentHelper.parseText(xmlString);
            Element rootElt = doc2.getRootElement();
            Iterator iterator = rootElt.elementIterator();

            while (iterator.hasNext()){
                Element stu = (Element) iterator.next();
                System.out.println("======遍历子节点======");
                Iterator iterator1 = stu.elementIterator();
                String chunkHash = stu.elementText("hash");
                String chunkName = stu.elementText("name");
                String chunkIndex = stu.elementText("index");
                String chunkStart = stu.elementText("startTime");
                String chunkEnd = stu.elementText("endTime");
                System.out.println("======hash:======"+chunkHash);
                System.out.println("======name:======"+chunkName);
                System.out.println("======index:======"+chunkIndex);
                System.out.println("======start:======"+chunkStart);
                System.out.println("======end:======"+chunkEnd);

                long vindex =  Long.parseLong(chunkIndex);
                long vstart =  Long.parseLong(chunkStart);
                long vend =  Long.parseLong(chunkEnd);
                ChunkInfo2 chunkInfo = new ChunkInfo2(chunkName,chunkHash,vstart,vend,vindex);

                searchResults.add(new ChunkCompare(vindex,chunkInfo));

            }
        } catch (DocumentException e) {
            System.out.println("=========== error when decode xml");
            e.printStackTrace();
        }
    }
    public static String txt2String(File file){
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                result.append(System.lineSeparator()+s);
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return result.toString();
    }

    public String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    public Uri getUri(){
        return Uri.fromFile(m3u8file);
    }
}
