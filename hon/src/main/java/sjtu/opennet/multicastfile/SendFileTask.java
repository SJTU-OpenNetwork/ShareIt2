package sjtu.opennet.multicastfile;

import android.util.Log;

import com.google.protobuf.ByteString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import sjtu.opennet.erasure.Shard;
import sjtu.opennet.erasure.ShardCodec;
import sjtu.opennet.erasure.ShardList;
import sjtu.opennet.multicastfile.pb.Multicastpb;
import sjtu.opennet.multicastfile.tcp.GroupStore;
import sjtu.opennet.multicastfile.tcp.RetransServer;
import sjtu.opennet.multicastfile.util.Constant;
import sjtu.opennet.textilepb.Multicast;
import sjtu.opennet.util.FileUtil;

public class SendFileTask {
    private static final String TAG = "================SendFileTask";
    MultiFile multiFile;
    private ShardCodec encoder;
    String localIP;
    long fid;

    public SendFileTask(MultiFile multiFile, ShardCodec encoder, String localIP){
        this.multiFile = multiFile;
        this.encoder = encoder;
        this.localIP = localIP;
        this.fid = Long.parseLong(multiFile.getFid());
    }

    public Multicastpb.FileMeta createMeta(File file,long fid, int gnum, String descriptJSON){
        return Multicastpb.FileMeta.newBuilder()
                .setSenderIP(localIP)
                .setFid(Long.parseLong(multiFile.getFid()))
                .setFileSize((int)file.length())
                .setFileName(file.getName())
                .setFileType(multiFile.getType())
                .setSender(multiFile.sender)
                .setGnum(gnum)
                .setFileDescription(descriptJSON)
                .build();
    }

    public boolean execute() throws Exception{
        // 构造meta，编码，发送数据
        File file=new File(multiFile.getFilePath());
        int bufSize= Constant.SHARD_SIZE*Constant.SHARD_NUM-4;
        int groupNum = (int) Math.ceil(file.length() / (float) bufSize);
        Multicastpb.FileMeta fileMeta=createMeta(file,fid,groupNum,multiFile.descriptJSON);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        byte[] batchData = new byte[bufSize];
        int readCount = 0;
        int x=0;
        while ((readCount = bufferedInputStream.read(batchData)) != -1) { //实际读入的个数是readCount个
            // Encode
            byte[] aBlock = new byte[readCount];
            System.arraycopy(batchData, 0, aBlock, 0, readCount);

            GroupStore.storeGroup(aBlock,fid,x);

            Multicastpb.FileGroup fileGroup= Multicastpb.FileGroup.newBuilder()
                    .setMeta(fileMeta)
                    .setData(ByteString.copyFrom(aBlock))
                    .build();

            byte[] groupStruct=fileGroup.toByteArray();
            // Encode
            ShardList shardlist = encoder.encode(groupStruct,groupStruct.length,x);

            LinkedList<Shard> shards=shardlist.getShards();
            for(Shard s:shards){
                // Build packet
//                Log.d(TAG, "execute: need:"+shardlist.SHARD_SIZE+" got:"+s.data.length+" fid,gid,sid:"+fid+" "+s.gid+" "+s.index);
                Multicastpb.HONMultiPacket packet = Multicastpb.HONMultiPacket.newBuilder()
                        .setFid(fid)
                        .setGid(s.gid)
                        .setSid(s.index)
                        .setData(ByteString.copyFrom(s.data))
                        .build();
                PacketSender.addPacket(packet);
            }
            x++;
//            Log.d(TAG, "execute: 发送："+x+" "+ FileUtil.getFileNameFromPath(multiFile.getFilePath()));
        }
        bufferedInputStream.close();
        GroupStore.addFile(fid,groupNum);
        PacketSender.addPacket(Multicastpb.HONMultiPacket.newBuilder()
                .setFid(fid)
                .setGid(Constant.END_GID)
                .build());
        return false;
    }
}
