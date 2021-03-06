package sjtu.opennet.hon;

import mobile.Mobile_;
import sjtu.opennet.textilepb.Thread2OuterClass.Thread2List;
import sjtu.opennet.textilepb.View;

public class Threads2 extends NodeDependent {
    Threads2(final Mobile_ node) {
        super(node);
    }

    public String addGroup(String threadName) throws Exception {
        String threadId = node.createGroup(threadName);
        return threadId;
    }

    public String createFriendGroup(String name) throws Exception{
        String threadId  = node.createSingleGroup(name);
        return threadId;
    }


    public Thread2List listThreads2() throws Exception {
        final byte[] bytes = node.listDBs();
        return Thread2List.parseFrom(bytes != null ? bytes : new byte[0]);
    }

    public String groupName(String threadId) throws Exception {
        final String gName = node.threadGroupName(threadId);
        return gName;
    }

    public String groupType(String threadId) throws Exception {
        final String gType = node.threadGroupName(threadId);
        return gType;
    }

    public void groupRename(String threadId,String newName) throws Exception {
        node.threadModifyGroupInfo(threadId,newName);
    }

    public View.ExternalInvite GetThreadAddrKey(String threadId) throws Exception {
        final byte[] bytes = node.dbAddrKey(threadId);
        return View.ExternalInvite.parseFrom(bytes);
    }

    public void joinThroughAddrKey(final String threadId, final String addr, final String key) throws Exception {
        node.createDBFromAddrKey(threadId, addr, key);
    }

    public void ListenAllThread() {
        node.listenAllThreads();
    }

    public void invitePeer(String threadId,String peerID) throws Exception {
        node.thread2InvitePeer(threadId,peerID);
    }

    //return role of an account address
    public String isAdmin(final String threadId,final String address) throws Exception {
        return node.thread2IsAdmin(threadId,address);
    }

    public String thread2PeerBySort(final String threadId,final String role) throws Exception {
        return node.thread2PeersBySort(threadId,role);
    }

    public void thread2SetAdmin(final String threadId,final String address) throws Exception {
        node.thead2SetAdmin(threadId,address);
    }

    //
    public void addMessage(String threadId,String mes) throws Exception {
        node.thread2AddMessage(threadId,mes);
    }

    public void addThread2Picture(final String path, final String threadId, final Handlers.Thread2AddFileCallback handler) {
        node.thread2AddPicture(path, threadId, (instanceId, e) -> {
            if (e != null) {
                handler.onError(e);
                return;
            }
            try {
                handler.onComplete(instanceId);
            } catch (final Exception exception) {
                handler.onError(exception);
            }
        });
    }

    public void addThread2File(final String path, final String threadId, final Handlers.Thread2AddFileCallback handler) {
        node.thread2AddFile(path, threadId, (instanceId, e) -> {
            if (e != null) {
                handler.onError(e);
                return;
            }
            try {
                handler.onComplete(instanceId);
            } catch (final Exception exception) {
                handler.onError(exception);
            }
        });
    }

    public void addThread2TicketVideo(final String threadId, final String posterPath, final String videoId, final Handlers.Thread2AddFileCallback handler) {
        node.thread2AddTicketVideo(threadId, posterPath, videoId, (instanceId, e) -> {
            if (e != null) {
                handler.onError(e);
                return;
            }
            try {
                handler.onComplete(instanceId);
            } catch (final Exception exception) {
                handler.onError(exception);
            }
        });
    }

//    public void thread2AddTicketVideo(final String threadId, final String videoId) throws Exception {
//        node.thread2AddTicketVideo(threadId, videoId);
//    }
}
