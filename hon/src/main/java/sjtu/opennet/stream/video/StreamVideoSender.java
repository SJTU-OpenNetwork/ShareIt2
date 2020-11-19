package sjtu.opennet.stream.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;

import java.nio.file.Files;
import java.nio.file.Paths;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.textilepb.Model;
import sjtu.opennet.video.ChunkInfo;
import sjtu.opennet.video.ChunkSender;
import sjtu.opennet.video.VideoMeta;
import sjtu.opennet.video.VideoSendHelper;

public class StreamVideoSender {
    private static final String TAG = "========StreamVideoSender";

    Context context;
    String threadId;
    String videoPath;

    VideoSendHelper videoSendHelper;

    public StreamVideoSender(Context context, String threadId, String videoPath) {
        this.context = context;
        this.threadId = threadId;
        this.videoPath = videoPath;

        videoSendHelper=new VideoSendHelper(context, videoPath,2, threadId, new ChunkSender() {
            @Override
            public void sendChunk(String videoId, String tsPath, ChunkInfo chunkInfo) {
                try {
                    byte[] tsFileContent = Files.readAllBytes(Paths.get(tsPath));
                    JSONObject object=new JSONObject();
                    object.put("startTime",String.valueOf(chunkInfo.chunkStartTime));
                    object.put("endTime",String.valueOf(chunkInfo.chunkEndTime));
                    String videoDescStr= JSON.toJSONString(object);
                    Log.d(TAG, "streamAddFile: "+videoDescStr);
                    Model.StreamFile streamFile= Model.StreamFile.newBuilder()
                            .setData(ByteString.copyFrom(tsFileContent))
                            .setDescription(ByteString.copyFromUtf8(videoDescStr))
                            .build();
                    Textile.instance().streams.streamAddFile(videoId,streamFile.toByteArray());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void finishSend(String videoId, int chunkNum) {
                try {
                    Textile.instance().streams.closeStream(threadId,videoId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void startSend(){
        videoSendHelper.startSend(new Handlers.VideoStartHandler() {
            @Override
            public void startSend(VideoMeta videoMeta, String threadId) {
                Textile.instance().ipfs.ipfsAddData(videoMeta.getPosterByte(), true, false, new Handlers.IpfsAddDataHandler() {
                    @Override
                    public void onComplete(String path) {
                        // startStream will sync the video message to thread
                        Log.d(TAG, "onComplete: poster: "+path);
                        Model.StreamMeta streamMeta= Model.StreamMeta.newBuilder()
                                .setId(videoMeta.getHash())
                                .setType(Model.StreamMeta.Type.VIDEO)
                                .setNsubstreams(1)
                                .setPosterid(path)
                                .build();
                        try {
                            Textile.instance().streams.startStream(threadId,streamMeta);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    public String getVideoId(){return videoSendHelper.getVideoId();}

    public Bitmap getPosterBitmap() { // temporarily not add the poster to ipfs
        return videoSendHelper.getPosterBitmap();
    }

}
