package com.sjtuopennetwork.shareit.contact;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.share.ChatActivity;
import com.sjtuopennetwork.shareit.util.ShareUtil;
import com.sjtuopennetwork.shareit.util.RoundImageView;

import java.util.List;

import sjtu.opennet.textilepb.Model;
import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;

public class ContactInfoActivity extends AppCompatActivity {

    //UI控件
    TextView contact_name;
    TextView contact_addr;
    RoundImageView contact_avatar;
//    TextView contact_del;
    TextView contact_send;

    //内存数据
    String address;
    Model.Contact contact;
    Model.Peer peer;
    String threadid;

    //持久化存储
    public SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        address=getIntent().getStringExtra("address");
        try {
            contact= Textile.instance().contacts.get(address);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initUI();

        initData();
    }

    private void initUI() {
        contact_name=findViewById(R.id.contact_info_name);
        contact_addr=findViewById(R.id.contact_info_addr);
//        contact_del=findViewById(R.id.contact_info_del);
        contact_send=findViewById(R.id.contact_info_send);

        contact_name.setText(contact.getName());
        contact_addr.setText("公钥："+contact.getAddress().substring(0,10)+"...");

        contact_avatar=findViewById(R.id.contact_info_avatar);
        ShareUtil.setImageView(this,contact_avatar,contact.getAvatar(), ShareUtil.ImgType.AVATAR);
    }
    private void initData(){

        pref=getSharedPreferences("txtl", Context.MODE_PRIVATE);

        List<Model.Thread> devicethreads= null;
        try {
            devicethreads = Textile.instance().threads.list().getItemsList(); //所有的threads
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(Model.Thread t:devicethreads){
            if(t.getWhitelistCount()==2){ //如果
                if(t.getWhitelist(0).equals(address) || t.getWhitelist(1).equals(address)){
                    threadid=t.getId();
                }
            }
        }

        //从address得到peerid
        try {
            List<Model.Peer> peers=Textile.instance().threads.peers(threadid).getItemsList();
            for(Model.Peer p:peers){
                if(p.getAddress().equals(address)){
                    peer=p;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        contact_del.setOnClickListener(v -> {
//            //先弹出对话框
//            AlertDialog.Builder delFriend=new AlertDialog.Builder(ContactInfoActivity.this);
//            delFriend.setTitle("删除好友");
//            delFriend.setMessage("确定删除 "+peer.getName()+" 吗？");
//            delFriend.setPositiveButton("确定", (dialog, which) -> {
//                //删除好友,removeThread，并在自己的数据库中删除记录
//                try {
//                    Textile.instance().threads.removePeer(threadid, peer.getId()); //将对方移出peer
//                    Textile.instance().threads.remove(threadid); //将自己的thread删除
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                DBoperator.deleteDialogByThreadID(appdb,threadid); //将自己的对话数据库中的记录删除
//
//                finish();
//            });
//            delFriend.setNegativeButton("取消", (dialog, which) -> Toast.makeText(ContactInfoActivity.this,"已取消",Toast.LENGTH_SHORT).show());
//            delFriend.show();
//        });

        contact_send.setOnClickListener(v -> {
            //发消息，直接找到threadid，跳转到相应的ChatActivity就行
            Intent it=new Intent(this, ChatActivity.class);
            it.putExtra("threadid",threadid);
            startActivity(it);
        });
    }
}
