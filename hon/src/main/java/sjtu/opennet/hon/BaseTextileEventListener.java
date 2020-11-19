package sjtu.opennet.hon;

import sjtu.opennet.textilepb.Model.CafeSyncGroupStatus;
import sjtu.opennet.textilepb.Model.Contact;
import sjtu.opennet.textilepb.Model.VideoChunk;
import sjtu.opennet.textilepb.Model.Notification;
import sjtu.opennet.textilepb.Model.Thread;
import sjtu.opennet.textilepb.Model.SyncFile;

/**
 * A default implementation of TextileEventListener that can be extended to override specific methods
 */
public abstract class BaseTextileEventListener implements TextileEventListener {

    @Override
    public void nodeStarted() {

    }

    @Override
    public void nodeFailedToStart(final Exception e) {

    }

    @Override
    public void nodeStopped() {

    }

    @Override
    public void nodeFailedToStop(final Exception e) {

    }

    @Override
    public void nodeOnline() {

    }

    @Override
    public void willStopNodeInBackgroundAfterDelay(final int seconds) {

    }

    @Override
    public void canceledPendingNodeStop() {

    }

    @Override
    public void notificationReceived(final Notification notification) {

    }

    @Override
    public void threadUpdateReceived(final String threadId, final FeedItemData feedItemData) {

    }

    @Override
    public void thread2UpdateReceived(final String threadId, final Thread2Data thread2Data) {

    }

    @Override
    public void threadAdded(final String threadId) {

    }

    @Override
    public void threadRemoved(final String threadId) {

    }

    @Override
    public void accountPeerAdded(final String peerId) {

    }

    @Override
    public void accountPeerRemoved(final String peerId) {

    }

    @Override
    public void queryDone(final String queryId) {

    }

    @Override
    public void queryError(final String queryId, final Exception e) {

    }

    @Override
    public void clientThreadQueryResult(final String queryId, final Thread thread) {

    }

    @Override
    public void contactQueryResult(final String queryId, final Contact contact) {

    }
    
    @Override
    public void videoChunkQueryResult(final String queryId, final VideoChunk vchunk) {

    }

    @Override
    public void syncFileQueryResult(final String queryId, final SyncFile file) {
    
    }

    @Override
    public void syncUpdate(final CafeSyncGroupStatus status) {

    }

    @Override
    public void syncComplete(final CafeSyncGroupStatus status) {

    }

    @Override
    public void syncFailed(final CafeSyncGroupStatus status) {

    }
}
