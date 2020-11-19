package sjtu.opennet.multicastfile.tcp;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import sjtu.opennet.multicastfile.SendFileTask;

public class RetransServer {
    private static final String TAG = "===============RetransServer";
    Socket s;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    LinkedBlockingQueue<RetransCmd> cmds;


    public RetransServer(Socket s, LinkedBlockingQueue<RetransCmd> cmds) {
        this.s = s;
        this.cmds = cmds;
        try {
            dataInputStream=new DataInputStream(s.getInputStream());
            dataOutputStream=new DataOutputStream(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        startListenCmd();
    }

    public void reTransmit(long fid, int gid) throws Exception{
        byte[] data= GroupStore.getGroup(fid,gid);

        dataOutputStream.writeLong(fid); dataOutputStream.flush();
        dataOutputStream.writeInt(gid); dataOutputStream.flush();
        dataOutputStream.writeInt((int)data.length); dataOutputStream.flush();

        int offset = 0;
        int writeLength=0;
        while (offset < data.length) {
            writeLength=Math.min(data.length - offset , 1024);
            dataOutputStream.write(data, offset, writeLength);
            dataOutputStream.flush();
            offset += writeLength;
        }

        Log.d(TAG, "reTransmit: 完成重传："+fid+" "+gid+" "+data.length);
    }

    private void startListenCmd(){
        new Thread(()->{
            while (true){
                try {
                    long fid=dataInputStream.readLong();
                    int gid=dataInputStream.readInt();
//                    Log.d(TAG, "startListenCmd: 收到重传请求："+fid+" "+gid);
                    cmds.put(new RetransCmd(s,fid,gid));
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    public static class RetransCmd{
        public Socket ss;
        public long fid;
        public int gid;

        public RetransCmd(Socket _s,long fid, int gid) {
            ss = _s;
            this.fid = fid;
            this.gid = gid;
        }
    }
}
