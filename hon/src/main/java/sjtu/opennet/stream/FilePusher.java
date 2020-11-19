package sjtu.opennet.stream;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

import sjtu.opennet.hon.Textile;
import sjtu.opennet.util.FileUtil;
import sjtu.opennet.textilepb.Model;

public class FilePusher {
    private static final String TAG = "======FilePusher";

    public static String pushPic(String threadId, String path) {
        Log.d(TAG, "onComplete: poster: " + path);
        File picFile=new File(path);
        String streamId=String.valueOf(System.currentTimeMillis());
        try {
            streamId=file2MD5(picFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Model.StreamMeta streamMeta = Model.StreamMeta.newBuilder()
                .setId(streamId)
                .setNsubstreams(1)
                .setPosterid(path)
                .setType(Model.StreamMeta.Type.PICTURE)
                .build();
        try {
            Textile.instance().streams.startStream(threadId, streamMeta);
        } catch (Exception e) {
            e.printStackTrace();
        }


        byte[] fileContent= FileUtil.readAllBytes(path);
        JSONObject object=new JSONObject();
        object.put("picName",picFile.getName());
        String videoDescStr= JSON.toJSONString(object);
        Model.StreamFile streamFile= Model.StreamFile.newBuilder()
                .setData(ByteString.copyFrom(fileContent))
                .setDescription(ByteString.copyFromUtf8(videoDescStr))
                .build();
        try {
            Textile.instance().streams.streamAddFile(streamId,streamFile.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return streamId;
    }

    public static String file2MD5(File file) throws Exception {
        byte[] hash;
        byte[] buffer = new byte[8192];
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(file);
        int len;
        while ((len = fis.read(buffer)) != -1) {
            md.update(buffer, 0, len);
        }
        hash = md.digest();

        //对生成的16字节数组进行补零操作
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        Log.d(TAG, "file2MD5: "+hex.toString());
        return hex.toString();
    }
}
