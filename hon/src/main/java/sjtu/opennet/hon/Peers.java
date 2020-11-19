package sjtu.opennet.hon;

import mobile.Mobile_;
import mobile.SearchHandle;
import sjtu.opennet.textilepb.Model.User;

/**
 * Provides access to Textile peers related APIs
 */
public class Peers extends NodeDependent {

    Peers(final Mobile_ node) {
        super(node);
    }

    /**
     * PeerUser returns a user object with the most recently updated contact for the given id
     * <p>If no underlying contact is found, this will return a blank object with a generic username, just for display.</p>
     * @param peerId Id of the peer
     * @return user object
     * @throws Exception
     */
    public User peerUser(final String peerId) throws Exception {
        final byte[] bytes = node.peerUser(peerId);
        return User.parseFrom(bytes);
    }
    
}
