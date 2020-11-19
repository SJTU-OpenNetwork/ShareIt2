package sjtu.opennet.multicastfile;

import sjtu.opennet.multicastfile.pb.Multicastpb;

public class Handlers {
    public interface ListenFileHandler {
        void getFile(long fid, String filePath, String sender, int fileType, String descripJSON);
    }
    public interface RetransHandler {
        void getReTransGroup(byte[] gData, long fid, int gid);
    }
}
