package com.sjtuopennetwork.shareit.share;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.contact.util.ContactUtil;
import com.sjtuopennetwork.shareit.util.ShareUtil;
import com.sjtuopennetwork.shareit.util.contactlist.MyContactBean;
import com.sjtuopennetwork.shareit.util.contactlist.MyContactView;

import java.util.LinkedList;
import java.util.List;

import sjtu.opennet.hon.Textile;
import sjtu.opennet.textilepb.Model;

public class GroupAddMemberActivity extends AppCompatActivity {

    //UI控件
    Button invite_new_members;
    MyContactView contactView;

    //内存数据
    String threadid;
    List<MyContactBean> contactBeans;
    List<Model.Peer> allMembers;
    List<Model.Peer> myFriends;
    List<Model.Peer> new_peers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add_member);

        initUI();

        initData();

    }

    private void initUI() {
        invite_new_members=findViewById(R.id.invite_new_members);
        contactView=findViewById(R.id.group_invite_others);
    }

    private void initData() {
        new_peers=new LinkedList<>();
        contactBeans=new LinkedList<>();
        allMembers=new LinkedList<>();
        myFriends=new LinkedList<>();
        try {
            threadid=getIntent().getStringExtra("threadid");
            allMembers= Textile.instance().threads.peers(threadid).getItemsList();
            myFriends= ContactUtil.getFriendList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //求好友与群成员的差集
        for(int i=0;i<myFriends.size();i++){
            if(!allMembers.contains(myFriends.get(i))){
                new_peers.add(myFriends.get(i));
            }
        }
        for(Model.Peer p:new_peers){
            MyContactBean contactBean=new MyContactBean(p.getAddress(),p.getName(),p.getAvatar());
            contactBeans.add(contactBean);
        }
        contactView.setData(contactBeans,true);
        contactView.setListener(item -> { });

        invite_new_members.setOnClickListener(v -> {
            List<MyContactBean> selects=contactView.getChoosedContacts();
            for(MyContactBean c:selects){ //逐个发送邀请
                try {
                    Textile.instance().invites.add(threadid,c.id);
                    System.out.println("================通知已发送");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            finish();
        });
    }
}
