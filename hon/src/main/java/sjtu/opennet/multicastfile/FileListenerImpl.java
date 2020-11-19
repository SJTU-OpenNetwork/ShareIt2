package sjtu.opennet.multicastfile;

import com.alibaba.fastjson.JSONObject;

import java.io.File;

import sjtu.opennet.hon.MulticastFile;
import sjtu.opennet.multicastfile.pb.Multicastpb;
import sjtu.opennet.multicastfile.util.Constant;
import sjtu.opennet.multicastfile.video.TsDescript;
import sjtu.opennet.util.FileUtil;
import sjtu.opennet.video.M3U8Util;


import static sjtu.opennet.multicastfile.HONMulticaster.MULTI_THREAD_ID;

public class FileListenerImpl implements Handlers.ListenFileHandler {
    private sjtu.opennet.hon.Handlers.MultiFileHandler handler;

    public FileListenerImpl(sjtu.opennet.hon.Handlers.MultiFileHandler handler) {
        this.handler = handler;
    }

    @Override
    public void getFile(long fid, String filePath, String sender, int fileType, String descripJSON) {
        switch (fileType){
            case Constant.FileType.TEXT_TYPE:
                handler.onGetMulticastFile(new MulticastFile(
                        MULTI_THREAD_ID,String.valueOf(fid),
                        sender,filePath,filePath,System.currentTimeMillis()/1000,
                        MulticastFile.MulticastFileType.TEXT
                ));
                break;
            case Constant.FileType.IMG_TYPE:
                JSONObject jsonObject1=JSONObject.parseObject(descripJSON);
                jsonObject1.put("filePath",filePath);
                handler.onGetMulticastFile(new MulticastFile(
                        MULTI_THREAD_ID,String.valueOf(fid),
                        sender,
                        FileUtil.getFileNameFromPath(filePath),
                        jsonObject1.toJSONString(),
                        System.currentTimeMillis()/1000,
                        MulticastFile.MulticastFileType.IMG
                ));
                break;
            case Constant.FileType.FILE_TYPE:
                JSONObject jsonObject=JSONObject.parseObject(descripJSON);
                jsonObject.put("filePath",filePath);
                handler.onGetMulticastFile(new MulticastFile(
                        MULTI_THREAD_ID,
                        String.valueOf(fid), //将耗时拼接在fileId中，预先没有设计好，只能这样了
                        sender,
                        FileUtil.getFileNameFromPath(filePath),
                        jsonObject.toJSONString(),
                        System.currentTimeMillis()/1000,
                        MulticastFile.MulticastFileType.FILE
                ));
                break;
            case Constant.FileType.FILE_START:
                if(descripJSON.equals(Constant.START_FILE)) {
                    handler.onReceivingMulticastFile(new MulticastFile(
                            MULTI_THREAD_ID,
                            String.valueOf(fid),
                            sender, FileUtil.getFileNameFromPath(filePath), filePath,
                            System.currentTimeMillis() / 1000,
                            MulticastFile.MulticastFileType.FILE
                    ));
                }else if(descripJSON.equals(Constant.START_PIC)){
                    handler.onReceivingMulticastFile(new MulticastFile(
                            MULTI_THREAD_ID, String.valueOf(fid),
                            sender, FileUtil.getFileNameFromPath(filePath), filePath,
                            System.currentTimeMillis() / 1000,
                            MulticastFile.MulticastFileType.IMG
                    ));
                }
                break;
            case Constant.FileType.VIDEO_TYPE:
                // 拿到了视频元数据和缩略图, 显示到前端界面，创建m3u8
                String videoDir=new File(filePath).getParent();
                String videoId=FileUtil.getFileNameFromPath(filePath);
                M3U8Util.initM3u8(videoDir,videoId);
                handler.onGetMulticastFile(new MulticastFile(
                        MULTI_THREAD_ID,videoId,
                        sender,videoDir,filePath,System.currentTimeMillis()/1000,
                        MulticastFile.MulticastFileType.VIDEO
                ));
                break;
            case Constant.FileType.TS_TYPE:
                TsDescript descript= (TsDescript) JSONObject.parseObject(descripJSON, TsDescript.class);
                String chunkName=FileUtil.getFileNameFromPath(filePath);
                M3U8Util.onTsArrive(descript.videoId,descript.endTime,descript.startTime,chunkName);
//                M3U8Util.updateM3u8(m3u8file,descript.endTime,descript.startTime,chunkName);
                break;
            case Constant.FileType.FILE_STAT:
                handler.onGetMulticastFile(new MulticastFile(
                        "",String.valueOf(fid),sender,"","", Long.valueOf(descripJSON),MulticastFile.MulticastFileType.STAT
                ));
                break;
        }
    }
}
