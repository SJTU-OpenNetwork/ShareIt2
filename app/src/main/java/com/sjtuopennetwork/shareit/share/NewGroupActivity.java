package com.sjtuopennetwork.shareit.share;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.contact.util.ContactUtil;
import com.sjtuopennetwork.shareit.util.ShareUtil;
import com.sjtuopennetwork.shareit.util.contactlist.MyContactBean;
import com.sjtuopennetwork.shareit.util.contactlist.MyContactView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import sjtu.opennet.textilepb.Model;
import sjtu.opennet.hon.Textile;

public class NewGroupActivity extends AppCompatActivity {

    //UI控件
    MyContactView contactView;
    Button bt_create;
    EditText editText;

    //内存数据
    List<MyContactBean> contactBeans;
    List<Model.Peer> myFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        initUI();

        initData();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    private void initData() {
        myFriends= ContactUtil.getFriendList();
        contactBeans=new LinkedList<>();

        for(Model.Peer p:myFriends){
            MyContactBean contactBean=new MyContactBean(p.getAddress(),p.getName(),p.getAvatar());
            contactBeans.add(contactBean);
        }

        contactView.setData(contactBeans,true);

        bt_create.setOnClickListener(v -> {
            System.out.println("===============选中的联系人数："+contactView.getChoosedContacts().size());
            String threadname=editText.getText().toString();

            //创建一个多人的群组：
            addNewThreads2(threadname);

        });
    }

    private void initUI() {
        contactView=findViewById(R.id.new_group_list);
        bt_create=findViewById(R.id.new_group_create);
        editText=findViewById(R.id.new_group_name);
    }

    private void addNewThreads(String threadName){
        String key= UUID.randomUUID().toString();
        sjtu.opennet.textilepb.View.AddThreadConfig.Schema schema= sjtu.opennet.textilepb.View.AddThreadConfig.Schema.newBuilder()
                .setPreset(sjtu.opennet.textilepb.View.AddThreadConfig.Schema.Preset.BLOB)
                .build();
        sjtu.opennet.textilepb.View.AddThreadConfig config=sjtu.opennet.textilepb.View.AddThreadConfig.newBuilder()
                .setSharing(Model.Thread.Sharing.SHARED)
                .setType(Model.Thread.Type.OPEN)
                .setKey(key).setName(threadName)
                .setSchema(schema)
                .build();
        try {
            Textile.instance().threads.add(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addNewThreads2(String threadName){
        try {
            Textile.instance().threads2.addGroup(threadName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void sendInvite(String addThreadID){
        try {
            for(MyContactBean c:contactView.getChoosedContacts()){
                Textile.instance().invites.add(addThreadID,c.id);
                System.out.println("===============发送了邀请");
            }

            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }
}
