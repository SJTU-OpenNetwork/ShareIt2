package sjtu.opennet.multicastfile.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.MulticastFile;
import sjtu.opennet.multicastfile.HONMulticaster;
import sjtu.opennet.multicastfile.MultiFile;
import sjtu.opennet.multicastfile.MultiFileTransfer;
import sjtu.opennet.multicastfile.util.Constant;
import sjtu.opennet.video.ChunkInfo;
import sjtu.opennet.video.ChunkSender;
import sjtu.opennet.video.VideoMeta;
import sjtu.opennet.video.VideoSendHelper;

public class MultiVideoSender {
    private static final String TAG = "=================MultiVideoSender";

    String threadId;
    String videoPath;
    Context sContext;

    MulticastFile videoMetaFile;

    public VideoSendHelper videoSendHelper;

    LinkedBlockingQueue<TsTask> tsTaskQueue=new LinkedBlockingQueue<>();

    public MultiVideoSender(Context context, String threadId, String videoPath) {
        this.threadId = threadId;
        this.videoPath = videoPath;
        this.sContext = context;

        videoSendHelper = new VideoSendHelper(sContext, videoPath, 2, threadId, new ChunkSender() {
            @Override
            public void sendChunk(String videoId, String tsPath, ChunkInfo chunkInfo) {
                // 每切割出来一个ts就调用一次这个函数，就构造ts发送任务添加到发送任务队列
//                Log.d(TAG, "sendChunk: "+tsPath+" "+chunkInfo.chunkName+" "+chunkInfo.chunkStartTime+" "+chunkInfo.chunkEndTime);
                TsTask tsTask=new TsTask(tsPath, new TsDescript(videoId, chunkInfo.chunkStartTime, chunkInfo.chunkEndTime),chunkInfo.chunkIndex);
                try {
                    tsTaskQueue.put(tsTask);
//                    Log.d(TAG, "sendChunk: --------------队列长度："+tsTaskQueue.size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void finishSend(String videoId, int chunkNum) {

            }
        });
    }

    public void startSend(String posterPath){
        videoSendHelper.startSend(new Handlers.VideoStartHandler() {
            @Override
            public void startSend(VideoMeta videoMeta, String threadId) {
                //首先发送缩略图，用VIDEO类型，本质也是发送文件
                MultiFileTransfer.getInstance().sendFile(new MultiFile(
                        String.valueOf(System.currentTimeMillis()),HONMulticaster.getSelfName(),posterPath,Constant.FileType.VIDEO_TYPE,""
                ));

                //开始切割和发送TS
                new Thread(()->{
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    while(true){
                        try {
                            TsTask t=tsTaskQueue.take();
                            String desc= JSON.toJSONString(t.tsDescript);

                            MultiFileTransfer.getInstance().sendFile(new MultiFile(
                                    String.valueOf(System.currentTimeMillis())+t.index,HONMulticaster.getSelfName(), t.tsPath, Constant.FileType.TS_TYPE, desc
                            ));

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    public String getVideoId() {
        return videoSendHelper.getVideoId();
    }

    public Bitmap getPosterBitmap() {
        return videoSendHelper.getPosterBitmap();
    }

    class TsTask{
        public String tsPath;
        public long index;
        public TsDescript tsDescript;

        public TsTask(String tsPath, TsDescript tsDescript, long index) {
            this.tsPath = tsPath;
            this.tsDescript = tsDescript;
            this.index = index;
        }
    }
}
