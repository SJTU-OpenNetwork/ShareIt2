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

public class GroupSetAdminActivity extends AppCompatActivity {

    //UI控件
    Button change_admin;
    MyContactView contactView;

    //内存数据
    String threadid;
    List<MyContactBean> contactBeans;
    List<Model.Peer> nonAdmins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_set_admin);

        initUI();

        initData();

    }

    private void initUI() {
        change_admin=findViewById(R.id.change_admin);
        contactView=findViewById(R.id.group_non_admins);
    }
    private void initData() {
        contactBeans=new LinkedList<>();
        nonAdmins=new LinkedList<>();
        try {
            threadid=getIntent().getStringExtra("threadid");
            nonAdmins= Textile.instance().threads.nonAdmins(threadid).getItemsList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(Model.Peer p:nonAdmins){
            MyContactBean contactBean=new MyContactBean(p.getAddress(),p.getName(),p.getAvatar());
            contactBeans.add(contactBean);
        }
        contactView.setData(contactBeans,true);

        change_admin.setOnClickListener(v -> {
            List<MyContactBean> selects=contactView.getChoosedContacts();
            for(MyContactBean c:selects){ //逐个添加管理员
                try {
                    Textile.instance().threads.addAdmin(threadid,c.id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            finish();
        });
    }
}
