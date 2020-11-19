package sjtu.opennet.video;

public class ChunkInfo {
    public String chunkName;
    public long chunkStartTime;
    public long chunkEndTime;
    public long chunkIndex;

    public ChunkInfo(String chunkName, long chunkStartTime, long chunkEndTime, long chunkIndex) {
        this.chunkName = chunkName;
        this.chunkStartTime = chunkStartTime;
        this.chunkEndTime = chunkEndTime;
        this.chunkIndex = chunkIndex;
    }
}
