//package sjtu.opennet.video;
//
//import java.io.IOException;
//
//public class ChunkInfoFileOpt {
//
//    private String filePath;
//    public ChunkInfoFileOpt(String filePath){
//        this.filePath = filePath;
//    }
//
//    public synchronized String writeOrReadData(String data, String way) throws IOException {
//        if(way.equals("read")){
//
//        }else if(way.equals("write")){
//            FileWriter fw = new FileWriter(filePath);
//            fw.write(data);
//            fw.flush();
//            fw.close();
//        }
//        return ;
//
//    }
//
//
//}
