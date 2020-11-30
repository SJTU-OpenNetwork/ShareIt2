package sjtu.opennet.stream.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
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
    int sendNum = 0;//标识正在进行发送的线程个数
    Object SENDLOCK=new Object();//SENDLOCK is used to handle
    VideoSendHelper videoSendHelper;
    ArrayList<Thread2VideoChunk> chunkArray50 = new ArrayList<>();

    class Thread2VideoChunk{
        // ts信息
        String chunkName;
        long chunkIndex;
        String chunkHash;
        long chunkStartTime;
        long chunkEndTime;

        public Thread2VideoChunk(String chunkName, long chunkIndex, String chunkHash, long chunkStartTime, long chunkEndTime) {
            this.chunkName = chunkName;
            this.chunkIndex = chunkIndex;
            this.chunkHash = chunkHash;
            this.chunkStartTime = chunkStartTime;
            this.chunkEndTime = chunkEndTime;
        }
    }

    public TicketVideoSender(Context context, String threadId, String videoPath) {
        this.context = context;
        this.threadId = threadId;
        this.videoPath = videoPath;

        videoSendHelper = new VideoSendHelper(context, videoPath, 1, threadId, new ChunkSender() {
            @Override
            public void sendChunk(String videoId, String tsPath, ChunkInfo chunkInfo) {
                ++sendNum ;//在sendchunk的时候不允许去执行finishsend，必须等待sendchunk结束
                System.out.println("========begin sendChunk,send num :"+sendNum+",index:"+chunkInfo.chunkIndex);
                byte[] tsFileContent = new byte[]{};
                try {
                    tsFileContent = Files.readAllBytes(Paths.get(tsPath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Textile.instance().ipfs.ipfsAddData(tsFileContent, true, false, new Handlers.IpfsAddDataHandler() {
                    @Override
                    public void onComplete(String path) {

                        // 构造chunk对象
                        Thread2VideoChunk thread2VideoChunk=new Thread2VideoChunk(chunkInfo.chunkName,
                                chunkInfo.chunkIndex,path,chunkInfo.chunkStartTime,chunkInfo.chunkEndTime);

                        chunkArray50.add(thread2VideoChunk);
                        Log.d(TAG, "onComplete: add "+thread2VideoChunk.chunkIndex+" "+chunkArray50.size());
                        if (chunkArray50.size() == 50) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("<tss>");
                            for (Thread2VideoChunk c : chunkArray50) {

                                stringBuilder.append("<ts>");

                                stringBuilder.append("<name>");
                                stringBuilder.append(c.chunkName);
                                stringBuilder.append("</name>");

                                stringBuilder.append("<index>");
                                stringBuilder.append(String.valueOf(c.chunkIndex));
                                stringBuilder.append("</index>");

                                //添加hash值
                                stringBuilder.append("<hash>");
                                stringBuilder.append(c.chunkHash);
                                stringBuilder.append("</hash>");

                                stringBuilder.append("<startTime>");
                                stringBuilder.append(String.valueOf(c.chunkStartTime));
                                stringBuilder.append("</startTime>");

                                stringBuilder.append("<endTime>");
                                stringBuilder.append(String.valueOf(c.chunkEndTime));
                                stringBuilder.append("</endTime>");

                                stringBuilder.append("</ts>");
                            }
                            stringBuilder.append("</tss>");
                           //System.out.println("============update thread2  instance:"+videoInstanceId);
                            //System.out.println("============construct xml:"+stringBuilder.toString());
                           // Stirng videoName =
                            Textile.instance().videos.updateTicketVideo(threadId, videoInstanceId, videoId, stringBuilder.toString());

                            //清空一下
                            chunkArray50.clear();
                        }
                        synchronized (SENDLOCK) {//如果sendchunk全部发送完，则发notify给finishsend
                            --sendNum;
                            if(sendNum == 0)
                                SENDLOCK.notify();
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                    }
                });

                System.out.println("========after sendChunk,send num :"+sendNum+",index:"+chunkInfo.chunkIndex);
            }

            @Override
            public void finishSend(String videoId, int chunkNum) {
              //  System.out.println("========finish sendChunk,send num :"+sendNum);
              //  if (sendNum == 0){
                synchronized (SENDLOCK){
                        try {
                            SENDLOCK.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                }
                System.out.println("========finish sendChunk,send num :"+sendNum);
                Thread2VideoChunk virtualChunk=new Thread2VideoChunk(videoId,
                        chunkNum,"VIRTUAL",-2,-2);

                chunkArray50.add(virtualChunk);
                System.out.println("======there are chunks:"+chunkArray50.size());




                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("<tss>");
                for (Thread2VideoChunk c : chunkArray50){
                    stringBuilder.append("<ts>");

                    stringBuilder.append("<name>");
                    stringBuilder.append(c.chunkName);
                    stringBuilder.append("</name>");

                    stringBuilder.append("<index>");
                    stringBuilder.append(String.valueOf(c.chunkIndex));
                    stringBuilder.append("</index>");

                    //添加hash值
                    stringBuilder.append("<hash>");
                    stringBuilder.append(c.chunkHash);
                    stringBuilder.append("</hash>");

                    stringBuilder.append("<startTime>");
                    stringBuilder.append(String.valueOf(c.chunkStartTime));
                    stringBuilder.append("</startTime>");

                    stringBuilder.append("<endTime>");
                    stringBuilder.append(String.valueOf(c.chunkEndTime));
                    stringBuilder.append("</endTime>");

                    stringBuilder.append("</ts>");
                }
                stringBuilder.append("</tss>");
                System.out.println("============construct xml:"+stringBuilder.toString());
                //解析xml 获取 ts信息, 存入chunks
//                try{
//                   // System.out.println("============messtring:"+mesString);
//                    Document doc2;
//                    doc2 = DocumentHelper.parseText(stringBuilder.toString());
//                    Element rootElt = doc2.getRootElement();
//                    Iterator iterator = rootElt.elementIterator();
//
//                    while (iterator.hasNext()){
//                        Element stu = (Element) iterator.next();
//                        System.out.println("======遍历子节点======");
//                        Iterator iterator1 = stu.elementIterator();
//                        while (iterator1.hasNext()){
//                            Element stuChild = (Element) iterator1.next();
//                            System.out.println("节点名："+stuChild.getName()+"---节点值："+stuChild.getStringValue());
//                        }
//                    }
//                } catch (DocumentException e) {
//                    e.printStackTrace();
//                }

                Textile.instance().videos.updateTicketVideo(threadId, videoInstanceId, videoId, stringBuilder.toString());

                //清空一下
                chunkArray50.clear();
       //     }

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
                       // Model.Video videoPb = videoMeta.getPb(path);
                       // String videoId = videoPb.getId();
                        System.out.println("*************"+videoMeta.getHash());
                        try {
                            Textile.instance().videos.addTicketVideo(threadId, path,videoMeta.getHash() ,new Handlers.Thread2AddFileCallback() {
                                @Override
                                public void onComplete(String instanceId) {
                                    videoInstanceId = instanceId;
                                    Log.d(TAG, "onComplete: Video meta added to thread2: " + instanceId);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.d(TAG, "onError: Error when added video meta to thread2");
                                }
                            });

                            //  Textile.instance().threads2.thread2AddTicketVideo(threadId,videoMeta.getHash());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//                        try {
//                            Textile.instance().threads2.addThread2TicketVideo(threadId,getVideoId(),new Handlers.Thread2AddFileCallback(){
//                                @Override
//                                public void onComplete(String instanceId) {
//                                    videoInstanceId = instanceId;
//                                    Log.d(TAG, "onComplete: Video meta added to thread2: " + instanceId);
//                                }
//
//                                @Override
//                                public void onError(Exception e) {
//                                    Log.d(TAG, "onError: Error when added video meta to thread2");
//                                }
//                            });
//                        } catch (Exception e) {
//                        e.printStackTrace();
//                    }


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
