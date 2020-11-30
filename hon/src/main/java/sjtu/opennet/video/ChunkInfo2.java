package sjtu.opennet.video;

public class ChunkInfo2 {
    public String chunkName;
    public long chunkStartTime;
    public long chunkEndTime;
    public long chunkIndex;
    public String chunkHash;

    public ChunkInfo2(String chunkName, String chunkHash, long chunkStartTime, long chunkEndTime, long chunkIndex) {
        this.chunkName = chunkName;
        this.chunkHash = chunkHash;
        this.chunkStartTime = chunkStartTime;
        this.chunkEndTime = chunkEndTime;
        this.chunkIndex = chunkIndex;
    }
}
