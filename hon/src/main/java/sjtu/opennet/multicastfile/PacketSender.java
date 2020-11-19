package sjtu.opennet.multicastfile;

import android.util.Log;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;

import sjtu.opennet.multicastfile.pb.Multicastpb;
import sjtu.opennet.multicastfile.tcp.RetransClient;
import sjtu.opennet.multicastfile.tcp.RetransServer;
import sjtu.opennet.multicastfile.util.Constant;

public class PacketSender {
    private static final String TAG = "============PacketSender";

    public static int speedKbNow = 3000;

    private static Thread sendThread;
    private HashSet<String> otherNodes;
    private HashMap<Socket, RetransServer> serverHashMap;
    private HashMap<String, RetransClient> clientHashMap;
    private static LinkedBlockingQueue<Multicastpb.HONMultiPacket> sendQueue = new LinkedBlockingQueue<>(1000000);
    private LinkedBlockingQueue<RetransServer.RetransCmd> cmdsQueue = new LinkedBlockingQueue<>();

    public static void addPacket(Multicastpb.HONMultiPacket packet) {
        try {
            sendQueue.put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public PacketSender() {
        otherNodes = new HashSet<>();
        serverHashMap = new HashMap<>();
        clientHashMap = new HashMap<>();
    }

    public void startSend() {
        sendThread = new Thread(() -> {
            long lastSendTime = System.nanoTime();

            while (true) {
                try {
                    while (sendQueue.size() > 0) {
                        if ((System.nanoTime() - lastSendTime) < (Constant.SHARD_SIZE * 1000000 / speedKbNow)) { // 1200*10^9/3*10^6
                            continue;
                        }
                        lastSendTime = System.nanoTime();
                        Multicastpb.HONMultiPacket packet = sendQueue.take();
                        if(packet.getGid() == Constant.END_GID){
                            Log.d(TAG, "startSend: 组播发送完："+packet.getFid());
                            break;
                        }
                        MultiFileTransfer.getInstance().sendUDPPacket(packet.toByteArray());
                    }
                    while (cmdsQueue.size() > 0) {
                        RetransServer.RetransCmd cmd = cmdsQueue.take();
                        serverHashMap.get(cmd.ss).reTransmit(cmd.fid, cmd.gid);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        sendThread.start();
    }

    public void addNode(String ip) {
        otherNodes.add(ip);
    }

    // 监听连接请求
    public void startListenRequest() {
        new Thread(() -> {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(Constant.RETRANS_PORT);
            } catch (Exception e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    Socket s = serverSocket.accept();
                    RetransServer retransServer = new RetransServer(s, cmdsQueue);
                    serverHashMap.put(s, retransServer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public RetransClient getRetransClient(String serverIP) throws Exception {
        RetransClient retransClient = clientHashMap.get(serverIP);
//        Log.d(TAG, "getRetransClient: 重传请求发给："+serverIP);
        if (retransClient == null) {
            Socket s = new Socket(serverIP, Constant.RETRANS_PORT);
            RetransClient client = new RetransClient(s, new Handlers.RetransHandler() {
                @Override
                public void getReTransGroup(byte[] gData, long fid, int gid) {
                    //将数据传回PacketProcessor
                    MultiFileTransfer.getInstance().packetProcessor.getRetransGroup(gData, fid, gid);
                }
            });
            clientHashMap.put(serverIP, client);
        }
        return retransClient;
    }
}
