package sjtu.opennet.stream.video;

import android.content.Context;
import android.net.Uri;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import sjtu.opennet.hon.BaseTextileEventListener;
import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.util.FileUtil;
import sjtu.opennet.textilepb.Model;
import sjtu.opennet.video.M3U8Util;

public class VideoGetter {
    private static final String TAG = "==================HONVIDEO.VideoGetter_stream";
    Context context;
    String videoId;
    String dir;

    File m3u8file;
    VideoTsGetListener videoTsGetListener;
    
    Handlers.VideoPrefetchHandler handler;
    private int tsCount=0;
    boolean isPlaying=false;

    public VideoGetter(Context context, String videoId){
        this.context=context;
        this.videoId=videoId;

        videoTsGetListener=new VideoTsGetListener();
    }

    public void startGet(Handlers.VideoPrefetchHandler handler){
        this.handler=handler;
        dir= FileUtil.getAppExternalPath(context, "tsFile");
        m3u8file= M3U8Util.initM3u8(dir,videoId);
        Textile.instance().addEventListener(videoTsGetListener);
        try {
            Textile.instance().streams.subscribeStream(videoId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Uri getUri(){
        return Uri.fromFile(m3u8file);
    }
    
    public String getM3u8Path(){
        return m3u8file.getAbsolutePath();
    }

    public void stopGet(){
        //close and delete listener
        Log.d(TAG, "stopGet: 拿到最后一个");
        Textile.instance().removeEventListener(videoTsGetListener);
        videoTsGetListener=null;
//        if(!isPlaying){
//            handler.onPrefetch();
//        }
    }

    class VideoTsGetListener extends BaseTextileEventListener {
        @Override
        public void notificationReceived(Model.Notification notification) {
            //TODO
            if(notification.getBody().equals("stream video")){
                Log.d(TAG, "+++++notificationReceived: 收到streamfile");
                if(notification.getBlock().equals("")){
                    Log.d(TAG, "notificationReceived: get endflag");
                    M3U8Util.writeM3u8End(m3u8file);
                    stopGet();
                    return;
                }
                try {
                    JSONObject object= JSON.parseObject(notification.getSubjectDesc());
                    Log.d(TAG, "notificationReceived: "+ notification.getSubjectDesc());
                    Textile.instance().ipfs.dataAtPath(notification.getBlock(), new Handlers.DataHandler() {
                        @Override
                        public void onComplete(byte[] data, String media) {
                            Log.d(TAG, "onComplete: ======成功下载ts文件");
                            tsCount++;
//                            if(tsCount==1){
//                                isPlaying=true;
//                                handler.onPrefetch();
//                            }
                            String tsName=dir + "/" + notification.getBlock();
                            FileUtil.writeByteArrayToFile(tsName,data);
                            long starttime=object.getLongValue("startTime");
                            long endtime=object.getLongValue("endTime");
                            Log.d(TAG, "onComplete: starttime endtime: "+starttime+" "+endtime);
                            M3U8Util.writeM3u8(m3u8file,endtime,starttime,notification.getBlock());
                        }
                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Log.d(TAG, "notificationReceived: video listener get : "+ notification.getBody());
            }
        }
    }
}
