package sjtu.opennet.multicastfile.tcp;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import sjtu.opennet.multicastfile.Handlers;

public class RetransClient {
    private static final String TAG = "=================RetransClient";
    Socket s;
    Handlers.RetransHandler handler;
    DataOutputStream dataOutputStream;
//    BufferedInputStream bufferedInputStream;
    DataInputStream dataInputStream;


    public RetransClient(Socket s, Handlers.RetransHandler handler) {
        this.s = s;
        this.handler = handler;
        try {
            dataOutputStream=new DataOutputStream(s.getOutputStream());
//            bufferedInputStream = new BufferedInputStream(s.getInputStream());
            dataInputStream=new DataInputStream(s.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        startReceive();
    }

    public void sendRequest(long fid, int gid){
//        Log.d(TAG, "sendRequest: 请求重传："+fid+" "+gid);
        try {
            dataOutputStream.writeLong(fid);
            dataOutputStream.writeInt(gid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startReceive(){
        new Thread(()->{
            while(true) {
//                byte[] fidBytes = new byte[8];
//                byte[] gidBytes = new byte[4];
//                byte[] lenBytes = new byte[4];
                long fid=0;
                int gid=0;
                int length=0;
                byte[] gData=null;
                try {
//                    long fid = bufferedInputStream.read(fidBytes, 0, 8);
//                    int gid = bufferedInputStream.read(gidBytes, 0, 4);
//                    int length = bufferedInputStream.read(lenBytes, 0, 4);

                    fid=dataInputStream.readLong();
                    gid=dataInputStream.readInt();
                    length=dataInputStream.readInt();

                    gData = new byte[length];
                    int read = 0;
                    while (read < length) {
                        int cur = dataInputStream.read(gData, read, (int) Math.min(1024, length - read));
                        if (cur < 0) {
                            break;
                        }
                        read += cur;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

                //处理数据 gData
                Log.d(TAG, "startReceive: 收到重传数据："+fid+" "+gid);
                handler.getReTransGroup(gData,fid,gid);

            }
        }).start();
    }
}
