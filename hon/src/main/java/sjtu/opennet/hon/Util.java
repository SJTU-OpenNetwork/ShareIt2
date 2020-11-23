package sjtu.opennet.hon;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import sjtu.opennet.textilepb.Thread2OuterClass;
import sjtu.opennet.textilepb.View;
import sjtu.opennet.textilepb.View.Announce;
import sjtu.opennet.textilepb.View.Comment;
import sjtu.opennet.textilepb.View.FeedItem;
import sjtu.opennet.textilepb.View.Files;
import sjtu.opennet.textilepb.View.Ignore;
import sjtu.opennet.textilepb.View.Join;
import sjtu.opennet.textilepb.View.Leave;
import sjtu.opennet.textilepb.View.Like;
import sjtu.opennet.textilepb.View.Text;
import sjtu.opennet.textilepb.View.RemovePeer;
import sjtu.opennet.textilepb.View.AddAdmin;
import sjtu.opennet.textilepb.View.FeedVideo;


public class Util {

    public static Date timestampToDate(Timestamp timestamp) {
        double milliseconds = timestamp.getSeconds() * 1e3 + timestamp.getNanos() / 1e6;
        return new Date((long)milliseconds);
    }

    static FeedItemData feedItemData(FeedItem feedItem) throws Exception {
        FeedItemData feedItemData;
        String typeUrl = feedItem.getPayload().getTypeUrl();
        ByteString bytes = feedItem.getPayload().getValue();

        feedItemData = new FeedItemData();
        feedItemData.block = feedItem.getBlock();
//        System.out.println("=========Thread更新1："+typeUrl);
        switch (typeUrl) {
            case "/Text":
                feedItemData.type = FeedItemType.TEXT;
                feedItemData.text = Text.parseFrom(bytes);
                break;
            case "/Comment":
                feedItemData.type = FeedItemType.COMMENT;
                feedItemData.comment = Comment.parseFrom(bytes);
                break;
            case "/Like":
                feedItemData.type = FeedItemType.LIKE;
                feedItemData.like = Like.parseFrom(bytes);
                break;
            case "/Files":
                feedItemData.type = FeedItemType.FILES;
                feedItemData.files = Files.parseFrom(bytes);
                break;
            case "/Ignore":
                feedItemData.type = FeedItemType.IGNORE;
                feedItemData.ignore = Ignore.parseFrom(bytes);
                break;
            case "/Join":
                feedItemData.type = FeedItemType.JOIN;
                feedItemData.join = Join.parseFrom(bytes);
                break;
            case "/Removepeer":
                feedItemData.type = FeedItemType.REMOVEPEER;
                feedItemData.removePeer = RemovePeer.parseFrom(bytes);
                break;
            case "/Addadmin":
                feedItemData.type = FeedItemType.ADDADMIN;
                feedItemData.addAdmin = AddAdmin.parseFrom(bytes);
                break;
            case "/Video":
                feedItemData.type = FeedItemType.VIDEO;
                feedItemData.feedVideo = FeedVideo.parseFrom(bytes);
                break;
            case "/Streammeta":
                feedItemData.type=FeedItemType.STREAMMETA;
                feedItemData.feedStreamMeta= View.FeedStreamMeta.parseFrom(bytes);
                break;
            case "/Leave":
                feedItemData.type = FeedItemType.LEAVE;
                feedItemData.leave = Leave.parseFrom(bytes);
                break;
            case "/Announce":
                feedItemData.type = FeedItemType.ANNOUNCE;
                feedItemData.announce = Announce.parseFrom(bytes);
                break;
            case "/Picture":
                feedItemData.type = FeedItemType.PICTURE;
                feedItemData.files = Files.parseFrom(bytes);
                break;
            case "/Simple_file":
                feedItemData.type = FeedItemType.SIMPLEFILE;
                feedItemData.feedSimpleFile = View.FeedSimpleFile.parseFrom(bytes);
                break;
            default:
                throw new Exception("Unknown feed item typeUrl: " + typeUrl);
        }
        return feedItemData;
    }

    static Thread2Data getThread2Data(Thread2OuterClass.Thread2MessageUpdate messageUpdate) {

        final String  collectionMember  = "GroupMember";
        final String  collectionMessage = "GroupMessage";
        final String  collectionGroup = "GroupInfo";
        Thread2Data thread2Data = new Thread2Data();
        thread2Data.threadId = messageUpdate.getThreadId();
        thread2Data.collection = messageUpdate.getCollection();//可以得到thread新收到的record的type。
        thread2Data.instanceId = messageUpdate.getInstanceId();

        byte[] bytes = messageUpdate.getInstance().toByteArray();
        String str = new String(bytes);
        try{
            JSONObject jsonObj = new JSONObject(str);

            System.out.println("*********get thread2 update date   :" + jsonObj.toString());
            if (thread2Data.collection.equals(collectionMember)){
                thread2Data.memberInstance.pid = jsonObj.getString("member_id");
                thread2Data.memberInstance.name = jsonObj.getString("name");
                thread2Data.memberInstance.role = jsonObj.getString("role");
            }else if (thread2Data.collection.equals(collectionMessage)){
                thread2Data.messageInstance.sender = jsonObj.getString("sender");
                thread2Data.messageInstance.sendTime = jsonObj.getInt("time");
                thread2Data.messageInstance.type = jsonObj.getString("type");
                thread2Data.messageInstance.content = jsonObj.getString("content");
            } else if (thread2Data.collection.equals(collectionGroup)){
                thread2Data.groupInstance.groupName = jsonObj.getString("name");
                thread2Data.groupInstance.groupCreator = jsonObj.getString("creator");
                thread2Data.groupInstance.createdTime = jsonObj.getInt("time");
                thread2Data.groupInstance.groupType = jsonObj.getString("type");
                thread2Data.groupInstance.groupContent = jsonObj.getInt("number");
            } else{
                System.out.println("=========cant identify type of new record");
            }
        }catch (JSONException e){
            System.out.println("Exception thrown  :" + e);
            e.printStackTrace();
        }

        return thread2Data;
    }
}
