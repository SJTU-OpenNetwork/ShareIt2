package sjtu.opennet.multicastfile;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.alibaba.fastjson.JSONObject;

import java.io.File;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.MulticastFile;
import sjtu.opennet.multicastfile.util.Constant;
import sjtu.opennet.multicastfile.util.MulticastUtil;
import sjtu.opennet.multicastfile.video.MultiVideoSender;
import sjtu.opennet.multicastfile.video.TsDescript;
import sjtu.opennet.util.FileUtil;
import sjtu.opennet.video.M3U8Util;

public class HONMulticaster {
    static String selfName;
    static String localAddr;
    static final String MULTI_THREAD_ID ="20200729multicast";
    public static  String TXTL_VIDEO_DIR="";

    public static void setSelfName(String name){
        selfName=name;
    }

    public static String getSelfName(){
        return selfName;
    }

    public static void start(Context ctx, Handlers.MultiFileHandler handler){
        localAddr = MulticastUtil.getLocalIpAddress(ctx);
        TXTL_VIDEO_DIR = FileUtil.getAppExternalPath(ctx,"txtlvideo");
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifiManager.createMulticastLock("multicast.test");
        multicastLock.acquire();

        MultiFileTransfer.getInstance().addHandler(new FileListenerImpl(handler));

        try {
            MultiFileTransfer.getInstance().start(localAddr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMulticastFile(MulticastFile multicastFile, MulticastConfig config){
        MultiFileTransfer.getInstance().config(config);
        switch(multicastFile.getType()){
            case TEXT:
                MultiFileTransfer.getInstance().sendTxt(multicastFile.getFilePath(),selfName);
                break;
            case IMG:
                MultiFileTransfer.getInstance().sendFile(new MultiFile(multicastFile.getFileId(),selfName,multicastFile.getFilePath(),Constant.FileType.IMG_TYPE,""));
                break;
            case FILE:
                MultiFileTransfer.getInstance().sendFile(new MultiFile(multicastFile.getFileId(),selfName,multicastFile.getFilePath(),Constant.FileType.FILE_TYPE,""));
                break;
        }
    }
}
