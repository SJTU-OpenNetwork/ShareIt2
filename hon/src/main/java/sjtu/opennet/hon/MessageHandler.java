package sjtu.opennet.hon;

import android.util.Log;

import net.gotev.uploadservice.Logger;

import java.util.Set;

import mobile.Event;
import mobile.Messenger;
import sjtu.opennet.textilepb.Mobile.MobileEventType;
import sjtu.opennet.textilepb.Mobile.MobileQueryEvent;
import sjtu.opennet.textilepb.Model.CafeSyncGroupStatus;
import sjtu.opennet.textilepb.Model.Contact;
import sjtu.opennet.textilepb.Model.VideoChunk;
import sjtu.opennet.textilepb.Model.Notification;
import sjtu.opennet.textilepb.Thread2OuterClass;
import sjtu.opennet.textilepb.View.AccountUpdate;
import sjtu.opennet.textilepb.View.FeedItem;
import sjtu.opennet.textilepb.Model.Thread;
import sjtu.opennet.textilepb.MessageOuterClass.Error;
import sjtu.opennet.textilepb.Model.SyncFile;


class MessageHandler implements Messenger {

    private static final String TAG = "MessageHandler";

    private Set<TextileEventListener> listeners;

    MessageHandler(final Set<TextileEventListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void notify(final Event event) {
//        System.out.println("=========notifyEvent:"+MobileEventType.forNumber(event.getType()));
        switch (MobileEventType.forNumber(event.getType())) {
            case NODE_START:
                for (final TextileEventListener listener : listeners) {
                    listener.nodeStarted();
                }
                break;
            case NODE_STOP:
                for (final TextileEventListener listener : listeners) {
                    listener.nodeStopped();
                }
                break;
            case NODE_ONLINE:
                for (final TextileEventListener listener : listeners) {
                    listener.nodeOnline();
                }
                break;
            case ACCOUNT_UPDATE:
                try {
                    final AccountUpdate walletUpdate = AccountUpdate.parseFrom(event.getData());
                    switch (walletUpdate.getType()) {
                        case THREAD_ADDED:
                            for (final TextileEventListener listener : listeners) {
                                listener.threadAdded(walletUpdate.getId());
                            }
                            break;
                        case THREAD_REMOVED:
                            for (final TextileEventListener listener : listeners) {
                                listener.threadRemoved(walletUpdate.getId());
                            }
                            break;
                        case ACCOUNT_PEER_ADDED:
                            for (final TextileEventListener listener : listeners) {
                                listener.accountPeerAdded(walletUpdate.getId());
                            }
                            break;
                        case ACCOUNT_PEER_REMOVED:
                            for (final TextileEventListener listener : listeners) {
                                listener.accountPeerRemoved(walletUpdate.getId());
                            }
                            break;
                        default:
                            break;
                    }
                } catch (final Exception e) {
                    Logger.error(TAG, e.getMessage());
                }
                break;
            case THREAD_UPDATE:
                try {
                    final FeedItem feedItem = FeedItem.parseFrom(event.getData());
                    final FeedItemData data = Util.feedItemData(feedItem);
                    for (final TextileEventListener listener : listeners) {
                        listener.threadUpdateReceived(feedItem.getThread(), data);
                    }
                } catch (final Exception e) {
                    Logger.error(TAG, e.getMessage());
                }
                break;

            case THREAD2_UPDATE:
                try {
                    final Thread2OuterClass.Thread2MessageUpdate messageUpdate = Thread2OuterClass.Thread2MessageUpdate.parseFrom(event.getData());
                    final Thread2Data data = Util.getThread2Data(messageUpdate);
                    for (final TextileEventListener listener : listeners) {
                        listener.thread2UpdateReceived(messageUpdate.getThreadId(), data);
                    }
                } catch (final Exception e) {
                    Logger.error(TAG, e.getMessage());
                }
                break;
            case NOTIFICATION:
                try {
                    final Notification notification = Notification.parseFrom(event.getData());
                    for (final TextileEventListener listener : listeners) {
                        listener.notificationReceived(notification);
                    }
                } catch (final Exception e) {
                    Logger.error(TAG, e.getMessage());
                }
                break;
            case QUERY_RESPONSE:
                try {
                    final MobileQueryEvent queryEvent = MobileQueryEvent.parseFrom(event.getData());
                    switch (queryEvent.getType()) {
                        case DATA:
                            final String type = queryEvent.getData().getValue().getTypeUrl();
//                            Log.d("hon-sdk", "queryEvent.type:"+type);
                            if (type.equals("/Thread")) {
                                final Thread clientThread = Thread.parseFrom(queryEvent.getData().getValue().getValue());
                                for (final TextileEventListener listener : listeners) {
                                    listener.clientThreadQueryResult(queryEvent.getId(), clientThread);
                                }
                            } else if (type.equals("/Contact")) {
                                final Contact contact = Contact.parseFrom(queryEvent.getData().getValue().getValue());
                                for (final TextileEventListener listener : listeners) {
                                    listener.contactQueryResult(queryEvent.getId(), contact);
                                }
                            } else if (type.equals("/VideoChunk")) {
                                final VideoChunk vchunk = VideoChunk.parseFrom(queryEvent.getData().getValue().getValue());
                                for (final TextileEventListener listener : listeners) {
                                    listener.videoChunkQueryResult(queryEvent.getId(), vchunk);
                                }
                            } else if (type.equals("/SyncFile")) {
                                final SyncFile sf = SyncFile.parseFrom(queryEvent.getData().getValue().getValue());
                                for (final TextileEventListener listener : listeners) {
                                    listener.syncFileQueryResult(queryEvent.getId(), sf);
                                }
                            }
                            break;
                        case DONE:
                            for (final TextileEventListener listener : listeners) {
                                listener.queryDone(queryEvent.getId());
                            }
                            break;
                        case ERROR:
                            final Error error = queryEvent.getError();
                            final Exception e = new Exception(error.getMessage());
                            for (final TextileEventListener listener : listeners) {
                                listener.queryError(queryEvent.getId(), e);
                            }
                            break;
                    }
                } catch (final Exception e) {
                    Logger.error(TAG, e.getMessage());
                }
                break;
            case CAFE_SYNC_GROUP_UPDATE:
                try {
                    final CafeSyncGroupStatus status = CafeSyncGroupStatus.parseFrom(event.getData());
                    for (final TextileEventListener listener : listeners) {
                        listener.syncUpdate(status);
                    }
                } catch (final Exception e) {
                    Logger.error(TAG, e.getMessage());
                }
                break;
            case CAFE_SYNC_GROUP_COMPLETE:
                try {
                    final CafeSyncGroupStatus status = CafeSyncGroupStatus.parseFrom(event.getData());
                    for (final TextileEventListener listener : listeners) {
                        listener.syncComplete(status);
                    }
                } catch (final Exception e) {
                    Logger.error(TAG, e.getMessage());
                }
                break;
            case CAFE_SYNC_GROUP_FAILED:
                try {
                    final CafeSyncGroupStatus status = CafeSyncGroupStatus.parseFrom(event.getData());
                    for (final TextileEventListener listener : listeners) {
                        listener.syncFailed(status);
                    }
                } catch (final Exception e) {
                    Logger.error(TAG, e.getMessage());
                }
                break;
        }
    }
}
