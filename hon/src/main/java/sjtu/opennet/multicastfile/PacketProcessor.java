package sjtu.opennet.multicastfile;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import sjtu.opennet.erasure.Shard;
import sjtu.opennet.erasure.ShardCodec;
import sjtu.opennet.erasure.ShardList;
import sjtu.opennet.multicastfile.pb.Multicastpb;
import sjtu.opennet.multicastfile.tcp.FileStat;
import sjtu.opennet.multicastfile.util.Constant;
import sjtu.opennet.multicastfile.video.MultiVideoSender;
import sjtu.opennet.multicastfile.video.TsDescript;
import sjtu.opennet.util.FileUtil;
import sjtu.opennet.video.M3U8Util;

import static sjtu.opennet.multicastfile.util.Constant.FileType.FILE_START;

public class PacketProcessor {
    private static final String TAG = "========PacketProcessor";
    HashMap<Long, ReceivingFile> fileHashMap;
    ShardCodec.Decoder decoder;
    LinkedBlockingQueue<DecodeTask> decodeQueue;
    ArrayList<Handlers.ListenFileHandler> handlers;
    LinkedBlockingQueue<Multicastpb.HONMultiPacket> queue;

    public PacketProcessor(ShardCodec.Decoder decoder, ArrayList<Handlers.ListenFileHandler> handlers) {
        decodeQueue = new LinkedBlockingQueue<>();
        queue = new LinkedBlockingQueue<>(100000);
        fileHashMap = new HashMap<>();
        this.decoder = decoder;
        this.handlers = handlers;

        startReceving();
        startDecode();
    }

    private void startReceving() {
        new Thread(() -> {
            while (true) {
                try {
                    Multicastpb.HONMultiPacket packet = queue.take();
                    long fid = packet.getFid();
                    int gid = packet.getGid();
                    int sid = packet.getSid();
//                    Log.d(TAG, "startReceving: get:"+fid+" "+gid+" "+sid);

                    // 收到的第一个packet，创建ReceivingFile
                    ReceivingFile receivingFile = fileHashMap.get(fid);
                    if (receivingFile == null) {
                        receivingFile = new ReceivingFile();
                        receivingFile.recordTime = System.currentTimeMillis();
                        receivingFile.startTimeOutThread();
                        fileHashMap.put(fid, receivingFile);
                    }
                    receivingFile.lastRecvTime = System.currentTimeMillis();

                    synchronized (receivingFile.stateHashMap) {
                        // 收到新的组，创建ShardList
                        ShardList shardList = receivingFile.shardListMap.get(gid);
                        if (shardList == null) { //
                            shardList = new ShardList(Constant.SHARD_NUM, Constant.PARITY_NUM);
                            receivingFile.shardListMap.put(gid, shardList);
                            receivingFile.stateHashMap.put(gid, ReceivingFile.State.RECEIVING);

//                            if (gid > 0) {
//                                Log.d(TAG, "startReceving: 上一组收到的个数："+receivingFile.shardListMap.get(gid-1).size()+" "+fid);
//                            }

                            // 请求重传
                            if (receivingFile.fileMeta != null && receivingFile.fileMeta.getFileType() != Constant.FileType.TS_TYPE) {
                                for (int id = receivingFile.nextToDecode; id < gid; id++) {
                                    ReceivingFile.State s = receivingFile.stateHashMap.get(id);
                                    if (s == null || s == ReceivingFile.State.RECEIVING) {
                                        if (receivingFile.retransClient != null) {
                                            receivingFile.retransClient.sendRequest(fid, id);
                                            receivingFile.stateHashMap.put(id, ReceivingFile.State.RETRANSMITTING);
                                        }
                                    }
                                }
                            }
                            receivingFile.nextToDecode = gid;
                        }

                        // 如果组正在解码，就忽略packet
                        if (receivingFile.stateHashMap.get(gid) == ReceivingFile.State.DECODING) {
                            continue;
                        }
//                        Log.d(TAG, "startReceving: need:"+shardList.SHARD_SIZE+" got:"+packet.getData().toByteArray().length+" fid,gid,sid:"+fid+" "+gid+" "+sid);
                        shardList.add(new Shard(gid, sid, packet.getData().toByteArray())); //将packet添加到组

                        if (shardList.size() >= Constant.SHARD_NUM) {
                            decodeQueue.put(new DecodeTask(fid, gid, shardList, receivingFile));
                            receivingFile.stateHashMap.put(gid, ReceivingFile.State.DECODING);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public class DecodeTask extends Thread {
        long fid;
        int gid;
        ShardList shardList;
        ReceivingFile receivingFile;

        public DecodeTask(long fid, int gid, ShardList shardList, ReceivingFile rcvFile) {
            this.fid = fid;
            this.gid = gid;
            this.shardList = shardList;
            this.receivingFile = rcvFile;
        }

        public void decode() {
            try {
                byte[] out = decoder.Decode(shardList);
                Multicastpb.FileGroup fileGroup = Multicastpb.FileGroup.parseFrom(out);
                Multicastpb.FileMeta fileMeta = fileGroup.getMeta();
                byte[] dataBytes = fileGroup.getData().toByteArray();
                if (receivingFile.fileMeta == null) {
                    receivingFile.fileMeta = fileMeta;
//                    String filePath = FileUtil.TXTL_FILE_DIR + fileMeta.getFileName();
                    String start_type="";
                    if(fileMeta.getFileType()==Constant.FileType.FILE_TYPE){
                        for (Handlers.ListenFileHandler h : handlers) {
                            h.getFile(fid, fileMeta.getFileName(), fileMeta.getSender(), FILE_START, Constant.START_FILE);
                        }
                    }
//                    else if(fileMeta.getFileType() == Constant.FileType.IMG_TYPE){
//                        for (Handlers.ListenFileHandler h : handlers) {
//                            h.getFile(fid, fileMeta.getFileName(), fileMeta.getSender(), FILE_START, Constant.START_PIC);
//                        }
//                    }

                    new Thread(() -> {
                        try {
                            receivingFile.retransClient = MultiFileTransfer.getInstance().packetSender.getRetransClient(fileMeta.getSenderIP());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
//                synchronized (receivingFile.fileData) {

                receivingFile.storeGroupData(gid, dataBytes);
//                if(gid%10==0) {
//                Log.d(TAG, "decode: 解码成功：" + gid + "/" + fileMeta.getGnum() + " " + receivingFile.groupFileIndex.size()+" "+fid+" "+fileMeta.getFileName());
//                }
                if (receivingFile.groupFileIndex.size() == fileMeta.getGnum()) { //解码完成
                    long useTime = (System.currentTimeMillis() - receivingFile.recordTime);
//                    Log.d(TAG, "decode: 解码完成，耗时：" + fileMeta.getFileName() + ": " + useTime);
                    fileGetFinish(fileMeta, useTime);
                }
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            decode();
        }
    }


    private void fileGetFinish(Multicastpb.FileMeta fileMeta, long useTime) throws Exception {
        String filePath = "";
        int fileType = 0;
        long fid = fileMeta.getFid();
        String descrpt=fileMeta.getFileDescription();
        switch (fileMeta.getFileType()) {
            case Constant.FileType.IMG_TYPE:
                filePath = FileUtil.TXTL_IMG_DIR + fileMeta.getFileName();
                fileType = Constant.FileType.IMG_TYPE;

                JSONObject jsonObject1=new JSONObject();
                jsonObject1.put("fileSize",fileMeta.getFileSize()); // int 文件大小
                jsonObject1.put("useTime",useTime); // 毫秒，耗时
                descrpt=jsonObject1.toJSONString();

                FileStat.sendFileAck(fileMeta.getSenderIP(), fid, useTime);
                break;
            case Constant.FileType.FILE_TYPE:
                filePath = FileUtil.TXTL_FILE_DIR + fileMeta.getFileName();
                fileType = Constant.FileType.FILE_TYPE;

                JSONObject jsonObject=new JSONObject();
                jsonObject.put("fileSize",fileMeta.getFileSize()); // int 文件大小
                jsonObject.put("useTime",useTime); // 毫秒，耗时
                descrpt=jsonObject.toJSONString();

                // 发送文件耗时统计
                FileStat.sendFileAck(fileMeta.getSenderIP(), fid, useTime);
                break;
            case Constant.FileType.VIDEO_TYPE:
                File videoDir = new File(HONMulticaster.TXTL_VIDEO_DIR +"/"+ fileMeta.getFileName());
                videoDir.mkdirs(); //创建视频的文件夹
                filePath = videoDir + "/" + fileMeta.getFileName();
                fileType = Constant.FileType.VIDEO_TYPE;
                break;
            case Constant.FileType.TS_TYPE:
                TsDescript descript = (TsDescript) JSONObject.parseObject(fileMeta.getFileDescription(), TsDescript.class);
                String videoDir1 = HONMulticaster.TXTL_VIDEO_DIR +"/"+ descript.videoId;
                filePath = videoDir1 + "/" + fileMeta.getFileName();
                fileType = Constant.FileType.TS_TYPE;
                break;
        }

        ReceivingFile receivingFile = fileHashMap.get(fid);
        receivingFile.timeOutThread.interrupt();
        receivingFile.restoreFile(filePath);
        Log.d(TAG, "fileGetFinish: 成功接收文件：" + filePath);
        receivingFile.deleteTmpFiles();
        for (Handlers.ListenFileHandler h : handlers) {
            h.getFile(fid, filePath, fileMeta.getSender(), fileType, descrpt);
        }
        fileHashMap.remove(fid);
    }


    public void receivePacket(Multicastpb.HONMultiPacket packet) throws Exception {
        queue.put(packet);
    }

    public void startDecode() {
        new Thread(() -> {
            while (true) {
                try {
                    DecodeTask decodeTask = decodeQueue.take();
//                    decodeTask.start();
                    decodeTask.decode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void getRetransGroup(byte[] gData, long fid, int gid) {
        ReceivingFile receivingFile = fileHashMap.get(fid);
        if (receivingFile != null) {
            receivingFile.storeGroupData(gid, gData);
            if (receivingFile.fileMeta != null) {
                Log.d(TAG, "getRetransGroup: " + receivingFile.groupFileIndex.size());
                if (receivingFile.groupFileIndex.size() == receivingFile.fileMeta.getGnum()) { //解码完成
                    try {
                        fileGetFinish(receivingFile.fileMeta, (System.currentTimeMillis() - receivingFile.recordTime));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
