package sjtu.opennet.hon;

import mobile.DataCallbackWithTime;
import mobile.Mobile_;
import mobile.PathCallbackWithTime;
import sjtu.opennet.textilepb.Model;
import sjtu.opennet.textilepb.View;

/**
 * Provides access to Textile IPFS related APIs
 */
public class Ipfs extends NodeDependent {

    Ipfs(final Mobile_ node) {
        super(node);
    }

    /**
     * Fetch the IPFS peer id
     * @return The IPFS peer id of the local Textile node
     * @throws Exception The exception that occurred
     */
    public String peerId() throws Exception {
        return node.peerId();
    }

    /**
     * Open a new direct connection to a peer using an IPFS multiaddr
     * @param multiaddr Peer IPFS multiaddr
     * @return Whether the peer swarm connect was successfull
     * @throws Exception The exception that occurred
     */
    public Boolean swarmConnect(final String multiaddr) throws Exception {
        final String result = node.swarmConnect(multiaddr);
        return result.length() > 0;
    }


    /**
     * Get the Address used to swarm connect
     * @param peerId Id of the peer
     * @return Swarm address
     * @throws Exception The exception that occurred
     */
    public String getSwarmAddress(final String peerId) throws  Exception {
        return node.getSwarmAddress(peerId);
    }

    /**
     * Used to get the swarm connected peers
     * @return Peers that swarm connected to
     * @throws Exception The exception that occurred
     */
    public Model.SwarmPeerList connectedAddresses() throws  Exception {
        final byte[] bytes = node.connectedAddresses();
        return Model.SwarmPeerList.parseFrom(bytes);
    }

    /**
     * Get raw data stored at an IPFS path
     * @param path The IPFS path for the data you want to retrieve
     * @param handler An object that will get called with the resulting data and media type
     */
    public void dataAtPath(final String path, final Handlers.DataHandler handler) {
        node.dataAtPath(path, (data, media, e) -> {
            if (e != null) {
                handler.onError(e);
                return;
            }
            try {
                handler.onComplete(data, media);
            } catch (final Exception exception) {
                handler.onError(exception);
            }
        });
    }

    /**
     * Used to get raw data in case of SimpleFile,
     * <p> This function will be called if the type of feeditem is SimpleFile</p>
     * @param feed Feeditem.After the upper layer received the thread update, it actually received a feeditem
     * @param handler An object that will get called with the resulting data and media type
     */
    public void dataAtFeedSimpleFile(View.FeedSimpleFile feed, final Handlers.DataHandlerWithTime handler) {
        node.dataAtFeedSimpleFile(feed.toByteArray(), new DataCallbackWithTime() {
            @Override
            public void call(byte[] bytes, String s, long l, Exception e) {
                if (e != null) {
                    handler.onError(e);
                    return;
                }
                try {
                    handler.onComplete(bytes, s, l);
                } catch (final Exception exception) {
                    handler.onError(exception);
                }
            }
        });
    }

//    public void dataAtFeedSimpleFileWithTime(View.FeedSimpleFile feed, final Handlers.PathHandlerWithTime handler) {
//        node.dataAtFeedSimpleFile(feed.toByteArray(), new DataCallback() {
//            @Override
//            public void call(byte[] data, String media, Exception e) {
//                if (e != null) {
//                    handler.onError(e);
//                    return;
//                }
//                try {
//                    handler.onComplete(data, media);
//                } catch (final Exception exception) {
//                    handler.onError(exception);
//                }
//            }
//        });
//    }

    public void pathAtSimpleFile(View.FeedSimpleFile feed, final Handlers.PathHandlerWithTime handler){
        node.bigFileAtSimpleFile(feed.toByteArray(), new PathCallbackWithTime() {
            @Override
            public void call(String s, String s1, long duration, Exception e) {
                try {
                    handler.onComplete(s, s1,duration);
                } catch (final Exception exception) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * Add raw data to IPFS
     * @param data Raw data to be added
     * @param pin Whether or not to pin it
     * @param hashOnly Whether or not only hash it
     * @param handler An object that will get called with the resulting data and media type
     */
    public void ipfsAddData(final byte[] data, boolean pin, boolean hashOnly, final Handlers.IpfsAddDataHandler handler) {
        node.ipfsAddData(data, pin, hashOnly, (path, e) -> {
            if (e != null) {
                handler.onError(e);
                return;
            }
            try {
                handler.onComplete(path);
            } catch (final Exception exception) {
                handler.onError(exception);
            }
        });
    }
}
