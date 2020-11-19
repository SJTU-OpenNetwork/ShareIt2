package sjtu.opennet.hon;

//import sjtu.opennet.multicast.MulticastFileMeta;
import sjtu.opennet.textilepb.Model;
import sjtu.opennet.video.VideoMeta;

/**
 * Wrapper around common handlers
 */
public class Handlers {

//    public interface SendStatHandler{
//        void recvComplete(MulticastFileMeta fileMeta);
//    }

    public interface VideoPrefetchHandler{
        void onPrefetch();
    }

    public interface VideoStartHandler{
        void startSend(VideoMeta videoMeta, String threadId);
    }

    public interface ConnectShadowHandler{
        void onSuccess();
    }

    public interface MultiFileHandler{
        void onGetMulticastFile(MulticastFile multicastFile);
        void onReceivingMulticastFile(MulticastFile multicastFile);
    }

    public interface PathHandler {
        void onComplete(String tmpFilePath, String media);
    }

    public interface PathHandlerWithTime{
        void onComplete(String tmpFilePath, String media, long duration);
    }

    /**
     * Interface representing an object that can be
     * called to indicate completion
     */
    public interface ErrorHandler {
        /**
         * Called to indicate completion
         */
        void onComplete();

        /**
         * Called in the case of an error
         * @param e The exception
         */
        void onError(final Exception e);
    }

    /**
     * Interface representing an object that can be
     * called with a data and media type result
     */
    public interface DataHandler {
        /**
         * Called with a data and meta result
         * @param data The data
         * @param media The media type
         */
        void onComplete(final byte[] data, final String media);

        /**
         * Called in the case of an error
         * @param e The exception
         */
        void onError(final Exception e);
    }

    public interface DataHandlerWithTime {
        /**
         * Called with a data and meta result
         * @param data The data
         * @param media The media type
         */
        void onComplete(final byte[] data, final String media, long duration);

        /**
         * Called in the case of an error
         * @param e The exception
         */
        void onError(final Exception e);
    }

    /**
     * Interface representing an object that can be
     * called when adding raw data to ipfs
     */
    public interface IpfsAddDataHandler {
        /**
         * Called with a path
         * @param path The returned path
         */
        void onComplete(final String path);

        /**
         * Called in the case of an error
         * @param e The exception
         */
        void onError(final Exception e);
    }

    /**
     * Interface representing an object that can be
     * called with a Block result
     */
    public interface BlockHandler {
        /**
         * Called with a block result
         * @param block The block
         */
        void onComplete(final Model.Block block);

        /**
         * Called in the case of an error
         * @param e The exception
         */
        void onError(final Exception e);
    }

    /**
     * Interface representing an object that can be
     * called with a cafe session
     */
    public interface CafeSessionHandler {
        /**
         * Called with a cafe session
         * @param session The cafe session
         */
        void onComplete(final Model.CafeSession session);

        /**
         * Called in the case of an error
         * @param e The exception
         */
        void onError(final Exception e);
    }

    public interface ForegroundHandler {
        void onForeground();
    }

    public interface Thread2AddFileCallback{
        void onComplete(final String instanceId);

        void onError(final Exception e);
    }
}
