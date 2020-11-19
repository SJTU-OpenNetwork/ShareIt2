package sjtu.opennet.hon;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import mobile.Mobile_;
import mobile.SearchHandle;
import sjtu.opennet.textilepb.Model.Video;
import sjtu.opennet.textilepb.Model.VideoChunk;
import sjtu.opennet.textilepb.Model.VideoChunkList;
import sjtu.opennet.textilepb.QueryOuterClass.QueryOptions;
import sjtu.opennet.textilepb.QueryOuterClass.VideoChunkQuery;

/**
 * Provides access to Textile videos related APIs
 */
public class Videos extends NodeDependent {

    private Lock lock = new ReentrantLock();
    Videos(final Mobile_ node) {
        super(node);
    }

    /**
     * Used to get video by the videoId
     * @param videoId Id of the video
     * @return Video file after parsing its raw data
     */
    public Video getVideo(final String videoId) throws Exception {
        final byte[] bytes = node.getVideo(videoId);
        return Video.parseFrom(bytes);
    }

    /**
     * Used to get a exact chunk of a video by chunkId
     * @param videoId Id of the video
     * @param chunk Id of a chunk in the video
     * @throws Exception The exception that occurred
     * @return Video chunk file after parsing its raw data
     */
    public VideoChunk getVideoChunk(final String videoId, final String chunk) throws Exception {
        final byte[] bytes = node.getVideoChunk(videoId, chunk);
        try {
            return VideoChunk.parseFrom(bytes);
        }catch (NullPointerException e){
            return null;
        }
    }

    /**
     * Used to get a exact chunk of a video by chunk index
     * @param videoId Id of the video
     * @param index Index of a chunk in the video
     * @throws Exception The exception that occurred
     * @return Video chunk file after parsing its raw data
     */
    public VideoChunk getVideoChunk(final String videoId, final long index) throws Exception{
        final byte[] bytes = node.getVideoChunkByIndex(videoId, index);
        try{
            return VideoChunk.parseFrom(bytes);
        }catch (NullPointerException e){
            //e.printStackTrace();
            return null;
        }
    }

    /**
     * Used to transform a video to raw data
     * @throws Exception The exception that occurred
     */
    public void addVideo(final Video video) throws Exception {
        node.addVideo(video.toByteArray());
    }

    /**
     * Used to transform video chunk to raw data
     * @throws Exception The exception that occurred
     */
    public void addVideoChunk(final VideoChunk vchunk) throws Exception {
        node.addVideoChunk(vchunk.toByteArray());
    }

    /**
     * Used to add video to a thread
     * @param threadId The id of the thread
     * @param videoId The id of a video that will be add to thread
     * @throws Exception The exception that occurred
     */
    public void threadAddVideo(final String threadId, final String videoId) throws Exception {
        node.threadAddVideo(threadId, videoId);
    }

    /**
     * Used to publish video to others
     * @param video The video that will be published
     * @param store Whether store the video in cafe
     * @throws Exception The exception that occurred
     */
    public void publishVideo(final Video video, final boolean store) throws Exception {
        node.publishVideo(video.toByteArray(), store);
    }

    /**
     * Used to publish video chunk to others
     * @param vchunk The video chunk that will be published
     * @throws Exception The exception that occurred
     */
    public void publishVideoChunk(final VideoChunk vchunk) throws Exception {
//        lock.lock();
        node.publishVideoChunk(vchunk.toByteArray());
//        lock.unlock();
    }

    /**
     * Used to get video chunks by videoId
     * @param videoId Id of the video
     * @return Video chunk list
     * @throws Exception
     */
    public VideoChunkList chunksByVideoId(final String videoId) throws Exception {
        final byte[] bytes = node.chunksByVideoId(videoId);
        return VideoChunkList.parseFrom(bytes);
    }

    /**
     * Searches the network for video chunks
     * @param query The object describing the query to execute
     * @param options Options controlling the behavior of the search
     * @return A handle that can be used to cancel the search
     * @throws Exception The exception that occurred
     */
    public SearchHandle searchVideoChunks(final VideoChunkQuery query, final QueryOptions options) throws Exception {
        return node.searchVideoChunks(query.toByteArray(), options.toByteArray());
    }
}
