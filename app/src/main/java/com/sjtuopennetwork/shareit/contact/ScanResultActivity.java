package com.sjtuopennetwork.shareit.contact;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.contact.util.ContactUtil;
import com.sjtuopennetwork.shareit.share.ChatActivity;
import com.sjtuopennetwork.shareit.util.RoundImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import sjtu.opennet.textilepb.Model;
import sjtu.opennet.textilepb.QueryOuterClass;
import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;

public class ScanResultActivity extends AppCompatActivity {

    private static final String TAG = "======================";
    //UI控件
    TextView add_friend;
    TextView send_msg;
    RoundImageView res_avatar;
    TextView res_name;
    TextView res_address;

    //内存数据
    String name;
    String avatar;
    private String address;
    private String peerId;
    private Model.Contact resultContact;
    String threadid;
    String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null){
            Bundle bundle = getIntent().getExtras();
            result = bundle.getString("result");
            String[] tmp=result.split("/");
            address=tmp[0];
            peerId=tmp[1];
        }

        trySwarmConnect(peerId);

        //注册监听器
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

        setContentView(R.layout.activity_scan_result);
        initUI();

        sendQuery(); //有可能结果很快找到，UI还未初始化，所以要放在UI初始化后面
    }

    private void trySwarmConnect(String peerId) {
        try {
            Log.d(TAG, "trySwarmConnect: 尝试swarmConnect："+peerId);
            Textile.instance().ipfs.swarmConnect("/p2p-circuit/ipfs/"+peerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        add_friend=findViewById(R.id.scan_result_add);
        res_avatar=findViewById(R.id.scan_result_avatar);
        res_name=findViewById(R.id.scan_result_name);
        res_address=findViewById(R.id.scan_result_addr);
        send_msg=findViewById(R.id.scan_result_send);
    }

    //发送查询请求
    private void sendQuery() {
        QueryOuterClass.QueryOptions options = QueryOuterClass.QueryOptions.newBuilder()
                .setWait(10)
                .setLimit(1)
                .build();
        QueryOuterClass.ContactQuery query = QueryOuterClass.ContactQuery.newBuilder()
                .setAddress(address)
//                .setName("规划局")
                .build();


        try {
            Textile.instance().contacts.search(query, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //得到搜索结果
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getResult(Model.Contact c){
        Log.d(TAG, "getResult: 得到结果："+c.getAddress());

        resultContact=c;
        //这里再设置
        drawUI();
    }

    private void drawUI() {
        name=resultContact.getName();
        avatar=resultContact.getAvatar();
        address=resultContact.getAddress();
        res_name.setText(name);
        res_address.setText(address);
        String getAvatar="/ipfs/" + avatar + "/0/small/content";
        Textile.instance().ipfs.dataAtPath(getAvatar, new Handlers.DataHandler() {
            @Override
            public void onComplete(byte[] data, String media) {
                res_avatar.setImageBitmap(BitmapFactory.decodeByteArray(data,0,data.length));
            }
            @Override
            public void onError(Exception e) {
//                        vh.avatar.setImageResource(R.drawable.ic_people);
            }
        });


        //如果已经是好友就显示发消息
        if(ContactUtil.isMyFriend(address)){
            add_friend.setVisibility(View.GONE);
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
            send_msg.setOnClickListener(view -> { //点击进入对话
                Intent it=new Intent(ScanResultActivity.this, ChatActivity.class);
                it.putExtra("threadid",threadid);
                startActivity(it);
            });
        }else{
            send_msg.setVisibility(View.GONE);
            add_friend.setOnClickListener(v -> {
                //点击就申请添加好友，逻辑与搜索结果界面的添加是相同的
//            createTwoPersonThread(resultContact.getName());
                ContactUtil.createTwoPersonThread(address);
            });
        }
    }

    //双人thread创建成功后就发送邀请，用户看起来就是好友申请
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void sendInvite(String threadId){
        try {
//            Model.Thread t=Textile.instance().threads.get(threadId);
            Textile.instance().contacts.add(resultContact);
            Textile.instance().invites.add(threadId,address); //key就是联系人的address
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }
}
