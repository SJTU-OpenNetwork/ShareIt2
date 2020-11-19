package sjtu.opennet.multicastfile;

import android.util.Log;

import com.google.protobuf.ByteString;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import sjtu.opennet.erasure.ShardCodec;
import sjtu.opennet.erasure.ShardList;
import sjtu.opennet.multicastfile.pb.Multicastpb;
import sjtu.opennet.multicastfile.tcp.FileStat;
import sjtu.opennet.multicastfile.util.Constant;
import sjtu.opennet.util.FileUtil;

public class MultiFileTransfer {
    private static final String TAG = "=============MultiFileTransfer";
    private static MultiFileTransfer INSTANCE;
    private InetAddress multiAddress;
    private MulticastSocket udpSocket;
    private String localAddr;
    private ShardCodec encoder;
    private ShardCodec.Decoder decoder;

    private Lock LOCK=new ReentrantLock();
    public Constant.TransState transState;
    public boolean isMulticasting=false;
    public PacketSender packetSender; //组播发送队列
    private ArrayList<Handlers.ListenFileHandler> handlers;
    private LinkedBlockingQueue<SendFileTask> sendFileTasks; //文件发送队列
    public PacketProcessor packetProcessor; //组播接收

    public static synchronized MultiFileTransfer getInstance(){
        if(INSTANCE==null){
            INSTANCE=new MultiFileTransfer();
        }
        return INSTANCE;
    }

    private MultiFileTransfer(){
        handlers=new ArrayList<>();
        sendFileTasks=new LinkedBlockingQueue<>();

        // init encoder and decoder
        encoder = new ShardCodec(Constant.SHARD_NUM, Constant.PARITY_NUM);
        decoder = new ShardCodec.Decoder();
        ShardList shardList = null;
        try {
            shardList = encoder.encode("0123456789".getBytes(), 10,0);
            decoder.Decode(shardList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addHandler(Handlers.ListenFileHandler handler){
        handlers.add(handler);
    }

    public void start(String ip) throws Exception{
        localAddr=ip;

        //创建组播相关的对象
        udpSocket = new MulticastSocket(Constant.MULTI_PORT);
        int si=72 * 1024 * 1024;
        udpSocket.setReceiveBufferSize(si);
        Log.d(TAG, "listenMulticast: recv buffer size: "+si+" "+udpSocket.getReceiveBufferSize());
        udpSocket.setLoopbackMode(true);
        multiAddress = InetAddress.getByName(Constant.MULTI_ADDR);
        udpSocket.joinGroup(multiAddress);

        //启动组播发送队列
        packetSender=new PacketSender();
        packetSender.startSend();
        packetSender.startListenRequest();

        //启动监听文件任务队列
        packetProcessor=new PacketProcessor(decoder, handlers);
        startSendFile();

        //监听文件统计消息
        FileStat.start(handlers);

        //while监听接收packet
        startListen();

        // 组播心跳
        startHeartbeat();
    }

    private void startListen(){
        new Thread(()->{
            byte[] buf = new byte[2000];
            while(true){
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    udpSocket.receive(packet);
                    int plength = packet.getLength();
                    byte[] pByte = Arrays.copyOf(buf, plength);
                    Multicastpb.HONMultiPacket multiPacket= Multicastpb.HONMultiPacket.parseFrom(pByte);

                    if(multiPacket.getFid()==Constant.TEXT_FID){ //文字消息
                        getTxt(multiPacket.getData());
                        continue;
                    }
                    if(multiPacket.getFid()==Constant.HEART_BEAT_FID){
                        Log.d(TAG, "startListen: 收到心跳");
                        getHeartBeat(multiPacket);
                        continue;
                    }
                    packetProcessor.receivePacket(multiPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 监听文件发送队列，一旦有任务就取出执行，文件发送是串行的
    private void startSendFile(){
        new Thread(()->{
            while (true){
                try {
                    SendFileTask sendFileTask=sendFileTasks.take();
//                    Log.d(TAG, "startSendFile: 得到发送任务："+FileUtil.getFileNameFromPath(sendFileTask.multiFile.filePath));
                    sendFileTask.execute();// 这个最好是单线程，执行完了再从队列中take下一个任务
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 向文件发送队列中添加一个任务
    public void sendFile(MultiFile multiFile){
        SendFileTask sendFileTask=new SendFileTask(multiFile, encoder, localAddr);
        try {
            sendFileTasks.put(sendFileTask);
//            Log.d(TAG, "sendFile: 文件发送队列长度："+sendFileTasks.size());
//            Log.d(TAG, "sendFile: 添加发送任务："+ FileUtil.getFileNameFromPath(multiFile.filePath));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendTxt(String msg, String sender){
        new Thread(()->{
            Multicastpb.HONMultiPacket packet= Multicastpb.HONMultiPacket.newBuilder()
                    .setFid(Constant.TEXT_FID)
                    .setData(ByteString.copyFromUtf8(msg+"___"+sender))
                    .build();
            sendUDPPacket(packet.toByteArray());
        }).start();
    }

    private void getTxt(ByteString data){
        String[] s=data.toStringUtf8().split("___");
        for(Handlers.ListenFileHandler handler:handlers){
            handler.getFile(0,s[0],s[1],0,"");
        }
    }

    public void sendUDPPacket(byte[] data){
        try {
            DatagramPacket datagramPacket = new DatagramPacket(
                    data,
                    data.length,
                    multiAddress,
                    Constant.MULTI_PORT
            );
            udpSocket.send(datagramPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startHeartbeat() {
        // Sending heart beat
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //发送心跳
                    Multicastpb.HONMultiPacket heartbeat= Multicastpb.HONMultiPacket.newBuilder()
                            .setFid(Constant.HEART_BEAT_FID)
                            .setData(ByteString.copyFromUtf8(localAddr))
                            .build();
                    sendUDPPacket(heartbeat.toByteArray());
                    Log.d(TAG, "run: send heart beat");
                }
            }
        }.start();
    }

    private void getHeartBeat(Multicastpb.HONMultiPacket packet){
        String getIP=packet.getData().toStringUtf8();
        packetSender.addNode(getIP);
    }

    public void config(MulticastConfig config){
        PacketSender.speedKbNow=config.getSpeed();
    }
}
