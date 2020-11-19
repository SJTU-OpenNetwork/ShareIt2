package sjtu.opennet.multicastfile.util;

public class Constant {
    public final static int SHARD_NUM = 200;
    public final static int SHARD_SIZE = 1400;
    public final static int PARITY_NUM = 56;

    public final static int MULTI_PORT = 18611;
    public final static String MULTI_ADDR = "239.0.0.3";
    public final static long TEXT_FID = 0;
    public final static long HEART_BEAT_FID = 1;

    public final static int RETRANS_PORT = 15567;
    public final static int STAT_PORT = 15568;

    public final static int END_GID = -1;

    public static class FileType {
        public final static int TEXT_TYPE = 0;
        public final static int IMG_TYPE = 1;
        public final static int FILE_TYPE = 2;
        public final static int VIDEO_TYPE = 3;
        public final static int TS_TYPE = 4;
        public final static int FILE_START = 5;
        public final static int FILE_STAT = 6;
    }

    public enum TransState{
        MULTICASTING, RETRANSMITTING
    }

    public final static String START_PIC="0";
    public final static String START_FILE="1";
}
