package com.sjtuopennetwork.shareit.setting;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.setting.util.NotificationAdapter;
import com.sjtuopennetwork.shareit.setting.util.NotificationGroup;
import com.sjtuopennetwork.shareit.setting.util.NotificationItem;

import java.util.ArrayList;
import java.util.List;

import sjtu.opennet.textilepb.Model.Notification;
import sjtu.opennet.hon.Textile;

import static sjtu.opennet.textilepb.Model.Notification.Type.COMMENT_ADDED;

public class NotificationActivity extends AppCompatActivity {

    //UI控件
    private ListView listView;
    private ExpandableListView notification_listView;

    //内存
    private List<Notification> noti_list;

    private NotificationAdapter notificationAdapter=null;
    private ArrayList<NotificationGroup> gData = null;
    private ArrayList<ArrayList<NotificationItem>> iData = null;
    private ArrayList<NotificationItem> inviteData = null;
    private ArrayList<NotificationItem> commentData = null;
    private ArrayList<NotificationItem> likeData = null;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        mContext=NotificationActivity.this;
        listView=findViewById(R.id.notification_listView);
        notification_listView=findViewById(R.id.notification_listView);
        gData = new ArrayList<>();
        iData = new ArrayList<>();
        initData();
        notificationAdapter = new NotificationAdapter(gData,iData,mContext);
        notification_listView.setAdapter(notificationAdapter);
        //为列表设置点击事件
        notification_listView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String name=iData.get(groupPosition).get(childPosition).actor;
            String notiid=iData.get(groupPosition).get(childPosition).notiid;

            AlertDialog.Builder acceptOr=new AlertDialog.Builder(NotificationActivity.this);
            acceptOr.setTitle("邀请处理");
            acceptOr.setMessage("接受 "+name+" 的邀请吗？");
            acceptOr.setPositiveButton("接受", (dialogInterface, i1) -> {
                try {
                    Textile.instance().notifications.acceptInvite(notiid);
                    init();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            acceptOr.setNegativeButton("忽略", (dialog, which) -> {
                try {
                    Textile.instance().notifications.ignoreInvite(notiid);
                    init();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            acceptOr.show();

            return true;
        });

        for(int i=0;i<gData.size();i++){
            notification_listView.collapseGroup(i);
            notification_listView.expandGroup(i);
        }
    }

    private void remove() {
    }

    private void initData() {
        inviteData = new ArrayList<>();
        commentData = new ArrayList<>();
        likeData = new ArrayList<>();
        boolean invite_flag=true,commit_flag=true,like_flag=true;
        try {
            noti_list = Textile.instance().notifications.list("", 500).getItemsList();
            for (Notification n : noti_list) {
                switch (n.getType()) {
                    case INVITE_RECEIVED:
                        if (invite_flag == true) {
                            gData.add(new NotificationGroup("邀请"));
                            invite_flag = false;
                        }
                        inviteData.add(new NotificationItem(n.getId(), n.getUser().getAvatar(), n.getUser().getName(),
                                "邀请你", n.getDate().getSeconds(), true));
                        break;
                    case COMMENT_ADDED:
                        if (commit_flag == true) {
                            gData.add(new NotificationGroup("评论"));
                            commit_flag = false;
                        }
                        commentData.add(new NotificationItem(n.getId(),n.getUser().getAvatar(),n.getUser().getName(),
                                "进行了评论",n.getDate().getSeconds(),true));
                        break;
                    case LIKE_ADDED:
                        if (like_flag == true) {
                            gData.add(new NotificationGroup("点赞"));
                            like_flag = false;
                        }
                        likeData.add(new NotificationItem(n.getId(),n.getUser().getAvatar(),n.getUser().getName(),
                                "点赞了",n.getDate().getSeconds(),true));
                        break;
                    default:
                        break;
                }
            }
            //测试用
//            SharedPreferences pref;
//            pref= getSharedPreferences("txtl", Context.MODE_PRIVATE);
//            String path;
//            path=pref.getString("avatarpath","null");
//            gData.add(new NotificationGroup("邀请"));
//            gData.add(new NotificationGroup("评论"));
//            gData.add(new NotificationGroup("点赞"));
//            Date d=new Date();
//            inviteData.add(new NotificationItem("1",path,"YJC", "邀请你", d.getTime(),true));
//            inviteData.add(new NotificationItem("2",path,"YJC", "邀请你", d.getTime(),true));
//            inviteData.add(new NotificationItem("3",path,"YJC", "邀请你", d.getTime(),true));

            if (inviteData != null)
                iData.add(inviteData);
            if (commentData != null)
                iData.add(commentData);
            if (likeData != null)
                iData.add(likeData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
