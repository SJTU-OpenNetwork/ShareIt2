package sjtu.opennet.hon;

import android.util.Log;

import java.util.logging.Handler;

import mobile.DataCallback;
import mobile.Mobile_;
import mobile.PathCallback;
import mobile.PathCallbackWithTime;
import sjtu.opennet.textilepb.Model;
import sjtu.opennet.textilepb.View;
/**
 * Provides access to Textile Stream related APIs
 */
public class Stream extends NodeDependent{

    private static final String TAG = "Stream:==============";

    public Stream(Mobile_ node) {
        super(node);
    }

    /**
     * Used to start a stream in a thread with related streamMeta,
     * <p>Actually, startStream does two tasks, add stream to datastore and start stream routine.</p>
     * @param threadId Id of the thread to start the stream
     * @param streamMeta Related meta information of the stream
     * @throws Exception The exception that occurred
     */
    public void startStream(String threadId, Model.StreamMeta streamMeta) throws Exception {
        Log.d(TAG, "startStream,streamid: "+streamMeta.getId());
        node.startStream_Text(threadId,streamMeta.toByteArray());
    }

    /**
     * Used to subscribe a stream with the stream id
     * @param streamId Id of the stream
     * @throws Exception The exception that occurred
     */
    public void subscribeStream(String streamId) throws Exception{
        Log.d(TAG, "subscribeStream,streamid: "+streamId);
        node.subscribeStream(streamId); //temp
    }
    /**
     * Used to add file to an active stream
     * @param streamId The id of stream that will be add file to
     * @param streamFile Raw data of the file
     * @throws Exception The exception that occurred
     */
    public void streamAddFile(String streamId, byte[] streamFile) throws Exception{
        Log.d(TAG, "streamAddFile,streamid: "+streamId);
        node.streamAddFile(streamId, streamFile);
    }

    /**
     * Start a new stream, where the stream id is exactly the file cid, and the stream is dedicated to the file
     * @param threadId Id of the thread
     * @param streamFile stream file
     * @param fileType Type of the file
     * @return
     * @throws Exception
     */
    public Model.StreamMeta fileAsStream(String threadId, Model.StreamFile streamFile, Model.StreamMeta.Type fileType) throws Exception {
        Log.d(TAG, "fileAsStream");
        return Model.StreamMeta.parseFrom(node.fileAsStream_Text(threadId, streamFile.toByteArray(), fileType.getNumber()));
    }

    /**
     * Used to close a stream already exist in a thread
     * @param threadId The id of the thread to start the stream
     * @param streamId The id of stream that will be closed
     * @throws Exception The exception that occurred
     */
    public void closeStream(String threadId, String streamId) throws Exception{
        Log.d(TAG, "closeStream: "+streamId);
        node.closeStream(threadId, streamId);
    }

    /**
     * Used to get state of a stream with streamId
     * @param streamId Id of the stream
     * @return The state of stream
     */
    public String getState(String streamId){
        String state=node.streamGetStatus(streamId);
        return state;
    }

    /**
     * Used to add a stream to a thread.
     * @param threadId Id of the thread
     * @param streamId Id of the stream
     * @throws Exception
     */
    public void threadAddStream(String threadId, String streamId) throws Exception{

    }

    /**
     * Used to set the out degree of the stream transfer tree
     * @param num The degree of stream transfer tree
     */
    public void setDegree(int num){
        node.setMaxWorkers((long)num);
    }

    /**
     * Used to get the max workers in the stream
     * @return The max stream workers
     */
    public long getWorker(){
        return node.getMaxWorkers();
    }

    /**
     * Used to get raw data with stream way
     * @param feed FeedStreamMeta that own some information of stream
     * @param hash Id of the stream
     * @param handler An object that will get called with the resulting data and media type
     */
    public void dataAtStreamFile(View.FeedStreamMeta feed,String hash, final Handlers.DataHandler handler) {
        node.dataAtStreamFile(feed.toByteArray(),hash.getBytes(), new DataCallback() {
            @Override
            public void call(byte[] data, String media, Exception e) {
                if (e != null) {
                    handler.onError(e);
                    return;
                }
                try {
                    handler.onComplete(data, media);
                } catch (final Exception exception) {
                    handler.onError(exception);
                }
            }
        });
    }
//
    public void tmpFilePathAtStream(View.FeedStreamMeta feed, String hash, Handlers.PathHandler handler){
        node.bigFileAtStream(feed.toByteArray(), hash.getBytes(), new PathCallback() {
            @Override
            public void call(String s, String s1, Exception e) {
                handler.onComplete(s,s1);
            }
        });
    }


//    public long getStreamDuration(String streamId){
//        return node.getStreamDuration(streamId);
//    }

    /**
     * Checks whether the stream is finished
     * @param streaId Id of the stream
     */
    public boolean isStreamFinshed(String streaId){
        return node.isStreamFinished(streaId);
    }

    public void setSpeedIntervalMillis(long interval){
        node.setStreamSpeedInterval(interval);
    }
}
