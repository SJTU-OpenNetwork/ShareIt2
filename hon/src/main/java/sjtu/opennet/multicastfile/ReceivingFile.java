package sjtu.opennet.multicastfile;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.PriorityQueue;

import sjtu.opennet.erasure.ShardList;
import sjtu.opennet.multicastfile.pb.Multicastpb;
import sjtu.opennet.multicastfile.tcp.RetransClient;
import sjtu.opennet.multicastfile.util.Constant;
import sjtu.opennet.util.FileUtil;

public class ReceivingFile {

    public enum State{
        RECEIVING,DECODING,RETRANSMITTING
    }
    public HashMap<Integer, State> stateHashMap=new HashMap<>();
    public Multicastpb.FileMeta fileMeta=null;
    public HashMap<Integer, ShardList> shardListMap=new HashMap<>(); //存放编码组，用来解码
    public long recordTime=0;
    public int nextToDecode = 0;
    public Thread timeOutThread;
    public long lastRecvTime;
    public RetransClient retransClient;
    private static final String TAG = "========ReceivingFile";

//    public PriorityQueue<GroupBytes> fileData=new PriorityQueue<>(); // 已经解码成功的group数据，最后只需要按顺序写到文件就行



//    public static class GroupBytes implements Comparable<GroupBytes>{
//        public int gid;
//        public byte[] gData;
//
//        public GroupBytes(int gid, byte[] gData) {
//            this.gid = gid;
//            this.gData = gData;
//        }
//
//        @Override
//        public int compareTo(GroupBytes groupBytes) {
//            return this.gid-groupBytes.gid;
//        }
//    }

    public PriorityQueue<Integer> groupFileIndex=new PriorityQueue<>();
    public void storeGroupData(int gid, byte[] data) {
        String tmpFilePath = FileUtil.TXTL_TMP + fileMeta.getFid();
        File file = new File(tmpFilePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String dataPath = tmpFilePath + "/" + gid + ".data";
        try {//异常处理
            FileOutputStream fos = new FileOutputStream(dataPath);
            fos.write(data);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        groupFileIndex.add(gid);
    }

    public byte[] getGroupData(int gid) {
        String tmpFilePath = FileUtil.TXTL_TMP + fileMeta.getFid();
        File file = new File(tmpFilePath);
        if (!file.exists()) {
            file.mkdirs();
            return null;
        }
        String dataPath = tmpFilePath + "/" + gid + ".data";
        try {//异常处理
            FileInputStream in=new FileInputStream(dataPath);
            ByteArrayOutputStream out=new ByteArrayOutputStream(1024);
            byte[] temp=new byte[1024];
            int size=0;
            while((size=in.read(temp))!=-1)
            {
                out.write(temp,0,size);
            }
            in.close();
            byte[] bytes=out.toByteArray();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void restoreFile (String filePath) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        int gNum = fileMeta.getGnum();
        for (int i = 0; i < gNum; i++) {
            byte[] data = getGroupData(i);
            fileOutputStream.write(data);
            fileOutputStream.flush();
        }
        fileOutputStream.close();
    }

    public void startTimeOutThread() {

        timeOutThread = new Thread() {
            final long TIMEOUT = 1200;
            @Override
            public void run() {
                while (true) {
                    if(Thread.currentThread().isInterrupted()){
                        Log.d(TAG,"timeout thread quit");
                        break;
                    }
                    long cur=System.currentTimeMillis();
                    Log.d(TAG, "run: "+cur+" "+lastRecvTime+" "+TIMEOUT);
                    if (cur > lastRecvTime + TIMEOUT) {
                        Log.d(TAG, "startTimeOutThread: Timeout, request retransmitting");
                        if(fileMeta!=null) {
                            for (int id = 0; id < fileMeta.getGnum(); id++) {
                                synchronized (stateHashMap) {
                                    ReceivingFile.State s = stateHashMap.get(id);
                                Log.d(TAG, "run: "+id+" "+s);
                                    if (s == null || s == ReceivingFile.State.RECEIVING) {
                                        try {
                                            retransClient = MultiFileTransfer.getInstance().packetSender.getRetransClient(fileMeta.getSenderIP());
                                            Log.d(TAG, "startTimeOutThread: requesting " + fileMeta.getFid() + "-" + id);
                                            retransClient.sendRequest(fileMeta.getFid(), id);
                                            stateHashMap.put(id, ReceivingFile.State.RETRANSMITTING);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                    try {
                        sleep(lastRecvTime + TIMEOUT - cur);
                    } catch (InterruptedException e) {
//                        e.printStackTrace();
                    }
                }
            }
        };
        timeOutThread.start();
    }

    public void deleteTmpFiles() {
        String tmpFilePath = FileUtil.TXTL_TMP + fileMeta.getFid();
        File file = new File(tmpFilePath);
        if (!file.exists()) {
            return;
        }
        FileUtil.deleteDirectory(file);
    }

}
