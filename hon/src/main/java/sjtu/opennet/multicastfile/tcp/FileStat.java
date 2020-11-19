package sjtu.opennet.multicastfile.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import sjtu.opennet.multicastfile.HONMulticaster;
import sjtu.opennet.multicastfile.Handlers;
import sjtu.opennet.multicastfile.util.Constant;

public class FileStat {

    private static ServerSocket serverSocket;

    private static ArrayList<Handlers.ListenFileHandler> handlers;

    public static void start(ArrayList<Handlers.ListenFileHandler> _handlers){
        handlers=_handlers;
        try {
            serverSocket=new ServerSocket(Constant.STAT_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(()->{
            while(true){
                try {
                    Socket s=serverSocket.accept();

                    //每拿到一个就新建一个线程去处理
                    new Thread(new FileAckThread(s)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 不定时会传文件，没有必要一直维护连接，拿到一个文件就建立一个连接回复一个就行。
    public static void sendFileAck(String senderIp, long fid, long useTime){
        try {
            Socket socket=new Socket(senderIp, Constant.STAT_PORT);
            DataOutputStream dataOutputStream=new DataOutputStream(socket.getOutputStream());

            dataOutputStream.writeUTF(HONMulticaster.getSelfName());
            dataOutputStream.writeLong(fid);
            dataOutputStream.writeLong(useTime);

            dataOutputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class FileAckThread implements Runnable {

        Socket socket;

        public FileAckThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            // 开始处理请求
            try {
                DataInputStream dataInputStream=new DataInputStream(socket.getInputStream());

                String receiverName=dataInputStream.readUTF();
                long fid=dataInputStream.readLong();
                long useTime = dataInputStream.readLong();

                dataInputStream.close();
                socket.close();

                // 反馈给app
                synchronized (handlers) {
                    for (Handlers.ListenFileHandler h : handlers){
                        h.getFile(fid,"",receiverName,Constant.FileType.FILE_STAT,String.valueOf(useTime));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
