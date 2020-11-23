package sjtu.opennet.stream.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.textilepb.Model;
import sjtu.opennet.video.ChunkInfo;
import sjtu.opennet.video.ChunkSender;
import sjtu.opennet.video.VideoMeta;
import sjtu.opennet.video.VideoSendHelper;

public class TicketVideoSender {
    private static final String TAG = "============TicketVideoSender";

    Context context;
    String threadId;
    String videoPath;
    String videoInstanceId;

    VideoSendHelper videoSendHelper;
    ArrayList<Thread2VideoChunk> chunkArray50 = new ArrayList<>();

    class Thread2VideoChunk{
        // 存hash

    }

    public TicketVideoSender(Context context, String threadId, String videoPath) {
        this.context = context;
        this.threadId = threadId;
        this.videoPath = videoPath;

        videoSendHelper = new VideoSendHelper(context, videoPath, 2, threadId, new ChunkSender() {
            @Override
            public void sendChunk(String videoId, String tsPath, ChunkInfo chunkInfo) {

                byte[] tsFileContent = new byte[]{};
                try {
                    tsFileContent = Files.readAllBytes(Paths.get(tsPath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Textile.instance().ipfs.ipfsAddData(tsFileContent, true, false, new Handlers.IpfsAddDataHandler() {
                    @Override
                    public void onComplete(String path) {
//                        Model.VideoChunk videoChunk = Model.VideoChunk.newBuilder()
//                                .setId(videoId)
//                                .setChunk(chunkInfo.chunkName)
//                                .setAddress(path)
//                                .setStartTime(chunkInfo.chunkStartTime)
//                                .setEndTime(chunkInfo.chunkEndTime)
//                                .setIndex(chunkInfo.chunkIndex)
//                                .build();
//                        try {
//                            Textile.instance().videos.addVideoChunk(videoChunk);
//                            Textile.instance().videos.publishVideoChunk(videoChunk);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }

                        // 构造这个chunk对象
                        Thread2VideoChunk thread2VideoChunk=new Thread2VideoChunk();

                        chunkArray50.add(thread2VideoChunk);
                        if (chunkArray50.size() == 50) {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (Thread2VideoChunk c : chunkArray50) {

                                stringBuilder.append("<ts>");

                                stringBuilder.append("<name>");
                                stringBuilder.append(c.chunkName);
                                stringBuilder.append("</name>");

                                stringBuilder.append("<index>");
                                stringBuilder.append(String.valueOf(c.chunkIndex));
                                stringBuilder.append("</index>");

                                //添加hash值


                                stringBuilder.append("<startTime>");
                                stringBuilder.append(String.valueOf(c.chunkStartTime));
                                stringBuilder.append("</startTime>");

                                stringBuilder.append("<endTime>");
                                stringBuilder.append(String.valueOf(c.chunkEndTime));
                                stringBuilder.append("</endTime>");

                                stringBuilder.append("</ts>");
                            }
                            Textile.instance().videos.updateTicketVideo(threadId, videoInstanceId, videoId, stringBuilder.toString());
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
            }

            @Override
            public void finishSend(String videoId, int chunkNum) {
                Model.VideoChunk virtualChunk = Model.VideoChunk.newBuilder()
                        .setId(videoId)
                        .setChunk("VIRTUAL")
                        .setAddress("VIRTUAL")
                        .setStartTime(-2)
                        .setEndTime(-2)
                        .setIndex(chunkNum) //index from 0 to size-1, so the index of virtual chunk is size
                        .build();
                try {
                    Textile.instance().videos.addVideoChunk(virtualChunk);
                    Textile.instance().videos.publishVideoChunk(virtualChunk);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void startSend() {
        videoSendHelper.startSend(new Handlers.VideoStartHandler() {
            @Override
            public void startSend(VideoMeta videoMeta, String threadId) {
                Textile.instance().ipfs.ipfsAddData(videoMeta.getPosterByte(), true, false, new Handlers.IpfsAddDataHandler() {
                    @Override
                    public void onComplete(String path) {
                        Log.d(TAG, "onComplete: tkt poster hash: " + path);
                        Model.Video videoPb = videoMeta.getPb(path);
                        try {
                            Textile.instance().videos.addTicketVideo(threadId, getVideoId(), new Handlers.Thread2AddFileCallback() {
                                @Override
                                public void onComplete(String instanceId) {
                                    videoInstanceId = instanceId;
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });

                            Textile.instance().threads2.thread2AddTicketVideo(threadId,videoMeta.getHash());
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

    public String getVideoId() {
        return videoSendHelper.getVideoId();
    }

    public Bitmap getPosterBitmap() { // temporarily not add the poster to ipfs
        return videoSendHelper.getPosterBitmap();
    }

}
