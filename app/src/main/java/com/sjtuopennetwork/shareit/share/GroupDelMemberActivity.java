package com.sjtuopennetwork.shareit.share;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.util.ShareUtil;
import com.sjtuopennetwork.shareit.util.contactlist.MyContactBean;
import com.sjtuopennetwork.shareit.util.contactlist.MyContactView;

import java.util.LinkedList;
import java.util.List;

import sjtu.opennet.hon.Textile;
import sjtu.opennet.textilepb.Model;

public class GroupDelMemberActivity extends AppCompatActivity {

    //UI控件
    Button delete_members;
    MyContactView contactView;

    //内存数据
    String threadid;
    List<MyContactBean> contactBeans;
    List<Model.Peer> allMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_del_member);

        initUI();

        initData();

    }

    private void initUI() {
        delete_members=findViewById(R.id.delete_members);
        contactView=findViewById(R.id.group_delete_members);
    }

    private void initData() {
        contactBeans=new LinkedList<>();
        allMembers=new LinkedList<>();
        try {
            threadid=getIntent().getStringExtra("threadid");
            allMembers= Textile.instance().threads.nonAdmins(threadid).getItemsList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(Model.Peer p:allMembers){
            MyContactBean contactBean=new MyContactBean(p.getId(),p.getName(),p.getAvatar());
            contactBeans.add(contactBean);
        }
        contactView.setData(contactBeans,true);
        contactView.setListener(item -> {});

        delete_members.setOnClickListener(v -> {
            List<MyContactBean> selects=contactView.getChoosedContacts();
            for(MyContactBean c:selects){ //逐个删除
                try {
                    Textile.instance().threads.removePeer(threadid,c.id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            finish();
        });
    }
}
