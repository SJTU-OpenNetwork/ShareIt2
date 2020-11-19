package sjtu.opennet.multicastfile.tcp;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sjtu.opennet.util.FileUtil;

public class GroupStore {
    private static final String TAG = "===============GroupStore";
    static FileGroupInfo[] gQueue=new FileGroupInfo[50];
    static int fileIndex=0;
    static ExecutorService pool= Executors.newFixedThreadPool(5);

    static class FileGroupInfo{
        public long fid;
        public int gNum;

        public FileGroupInfo(long fid, int gNum) {
            this.fid = fid;
            this.gNum = gNum;
        }
    }

    public static void addFile(long fid, int gNum){
        int index=fileIndex%50;
        if(gQueue[index]!=null){
            deleteGroups(gQueue[index].fid, gQueue[index].gNum);
        }
        gQueue[index]=new FileGroupInfo(fid,gNum);
        if(fileIndex==Integer.MAX_VALUE){
            fileIndex=0;
        }else {
            fileIndex++;
        }
    }

    public static void storeGroup(byte[] gData, long fid, int gid){
        pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream fos=new FileOutputStream(FileUtil.TXTL_TMP+fid+"_"+gid);
                    fos.write(gData);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public static byte[] getGroup(long fid, int gid){
        byte[] data=null;
        try {
            File f=new File(FileUtil.TXTL_TMP+fid+"_"+gid);
            FileInputStream fis=new FileInputStream(f);
            data=new byte[(int)f.length()];
            fis.read(data,0,data.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static void deleteGroups(long fid, int gNum){
        pool.submit(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<gNum; i++){
                    File file=new File(FileUtil.TXTL_TMP+fid+"_"+i);
                    file.delete();
                }
            }
        });
    }
}
