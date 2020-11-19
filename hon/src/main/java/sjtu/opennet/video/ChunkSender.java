package sjtu.opennet.video;

public interface ChunkSender {
    void sendChunk(String videoId, String tsPath, ChunkInfo chunkInfo);
    void finishSend(String videoId, int chunkNum);
}
