package sjtu.opennet.video;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import sjtu.opennet.multicastfile.HONMulticaster;
import sjtu.opennet.textilepb.View;
import sjtu.opennet.util.FileUtil;

public class M3U8Util {
    private static final String TAG = "============HONVIDEO.M3U8Util";

    public static class TsMeta {
        public TsMeta(long start, long end, String chunkName){
            this.start = start;
            this.end = end;
            this.chunkName = chunkName;
        }
        public long start;
        public long end;
        public String chunkName;
    }

    private static Map<String, Map<Integer,TsMeta>> tsMap=new HashMap<>();
    private static Map<String, Integer> nextChunkMap=new HashMap<>();

    private static HashMap<String,FileWriter> writerHashMap=new HashMap<>();

    public static class ChunkInfo{
        public String filename;
        public long duration;
        public ChunkInfo(String filename, long duration){
            this.filename = filename;
            this.duration = duration;
        }
    }

    public static String getHead(int targetDuration, boolean addNewLine){
        String head=String.format("#EXTM3U\n" +
                "#EXT-X-VERSION:3\n" +
                "#EXT-X-MEDIA-SEQUENCE:0\n" +
                "#EXT-X-ALLOW-CACHE:YES\n" +
                "#EXT-X-TARGETDURATION:%d\n" +
                "#EXT-X-PLAYLIST-TYPE:EVENT", targetDuration);
        if(addNewLine){
            return head + "\n";
        }else{
            return head;
        }
    }

    public synchronized static ArrayList<ChunkInfo> getInfos(String listPath){
        ArrayList<ChunkInfo> infos = new ArrayList<>();
        String m3u8content = FileUtil.readAllString(listPath);
        String[] list = m3u8content.split("\n");
        int listLen = list.length;
        for (int i=5; i< listLen - 1 ; i ++){
            if(i % 2 != 0){
                ChunkInfo info = parseM3u8Content(list[i], list[i+1]);
                infos.add(info);
            }
        }
        return infos;
    }

    private synchronized static ChunkInfo parseM3u8Content(String infoLine, String filename){
        long duration = (long)(Double.parseDouble(infoLine.substring(8, infoLine.length()-1))*1000000);
        return new ChunkInfo(filename, duration);
    }

    public synchronized static File initM3u8(String dir, String videoId){
        String head="#EXTM3U\n" +
                "#EXT-X-VERSION:3\n" +
                "#EXT-X-MEDIA-SEQUENCE:0\n" +
                "#EXT-X-ALLOW-CACHE:YES\n" +
                "#EXT-X-TARGETDURATION:2\n" +
                "#EXT-X-PLAYLIST-TYPE:EVENT\n";
        File m3u8file=new File(dir+"/"+videoId+".m3u8");

        try{
            FileWriter fileWriter=new FileWriter(m3u8file,true);
            fileWriter.write(head);
            fileWriter.flush();
            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        Map<Integer, TsMeta> map = new HashMap<>();
        tsMap.put(videoId, map);
        nextChunkMap.put(videoId, 0);
        Log.d(TAG, "initM3u8: 创建m3u8文件："+m3u8file.getAbsolutePath());

        return m3u8file;
    }

    public synchronized  static void onTsArrive(String videoId, long end,long start, String chunkName) {
        Map<Integer,TsMeta> map = tsMap.get(videoId);
        if (map == null) {
            return;
        }
        int index = getChunkIndex(chunkName);
        map.put(index, new TsMeta(start,end,chunkName));
        int nextChunkIndex = nextChunkMap.get(videoId);
        Log.d(TAG, "onTsArrive: "+nextChunkIndex+" "+index);

        File m3u8file = new File(getM3u8File(videoId));
        while(map.containsKey(nextChunkIndex)) {
            TsMeta meta = map.get(nextChunkIndex);
            writeM3u8(m3u8file, meta.end, meta.start, meta.chunkName);
            Log.d(TAG, "onTsArrive: write: "+nextChunkIndex);
            nextChunkIndex++;
        }
        nextChunkMap.put(videoId, nextChunkIndex);
    }

    public synchronized  static void onTsArriveThread2(File m3u8File, String videoId, long end,long start, String chunkName) {
        Map<Integer,TsMeta> map = tsMap.get(videoId);
        if (map == null) {
            return;
        }
        int index = getChunkIndex(chunkName);
        map.put(index, new TsMeta(start,end,chunkName));
        int nextChunkIndex = nextChunkMap.get(videoId);
        Log.d(TAG, "onTsArrive: "+nextChunkIndex+" "+index);

        while(map.containsKey(nextChunkIndex)) {
            TsMeta meta = map.get(nextChunkIndex);
            writeM3u8(m3u8File, meta.end, meta.start, meta.chunkName);
            Log.d(TAG, "onTsArrive: write: "+nextChunkIndex);
            nextChunkIndex++;
        }
        nextChunkMap.put(videoId, nextChunkIndex);
    }

    public synchronized static void writeM3u8(File m3u8file, long end,long start, String chunkName){
        long duration0=end-start; //微秒
        double size = (double)duration0/1000000;
        DecimalFormat df = new DecimalFormat("0.000000");//格式化小数，不足的补0
        String duration = df.format(size);//返回的是String类型的
        String append = "#EXTINF:"+duration+",\n"+
                chunkName+"\n";
        try {
            FileWriter fileWriter=new FileWriter(m3u8file,true);
            fileWriter.write(append);
            Log.d(TAG, "writeM3u8: "+append);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void writeM3u8End(File m3u8file){
        try {
            FileWriter fileWriter=new FileWriter(m3u8file,true);
            fileWriter.write("#EXT-X-ENDLIST");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean existOrNot(String tsDirPath){
        File tsDir=new File(tsDirPath);
        if(!tsDir.exists()){
            tsDir.mkdirs();
            return false;
        }
        long tsLength=tsDir.list().length-1;
        Log.d(TAG, "existOrNot: "+tsLength);
        if(tsLength>0){
            return true;
        }
        return false;
    }

    public static int getChunkIndex(String chunkName) {
        return Integer.parseInt(chunkName.substring(3,chunkName.length()-3));
    }

    public static String getM3u8File(String videoId) {
        String videoDir1 = HONMulticaster.TXTL_VIDEO_DIR +"/"+ videoId;
        return videoDir1+"/"+videoId+".m3u8";
    }
}
