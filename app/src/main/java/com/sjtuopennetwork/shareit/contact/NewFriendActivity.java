package com.sjtuopennetwork.shareit.contact;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.ListView;
import android.widget.Toast;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.contact.util.ContactUtil;
import com.sjtuopennetwork.shareit.contact.util.ResultAdapter;
import com.sjtuopennetwork.shareit.contact.util.ResultContact;

import java.util.List;

import sjtu.opennet.textilepb.View;
import sjtu.opennet.hon.Textile;


public class NewFriendActivity extends AppCompatActivity {

    private static final String TAG = "================";

    //UI控件
    ListView new_friend_lv;

    //内存数据
    List<View.InviteView> friendApplications;
    List<ResultContact> applications;
    Pair<List<View.InviteView>,List<ResultContact>> result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);
        initUI();

        //将通知显示到ListView中
        showApplications();

    }

    private void showApplications() {
        result= ContactUtil.getApplication();
        friendApplications=result.first;
        applications=result.second;

        ResultAdapter resultAdapter=new ResultAdapter(NewFriendActivity.this,R.layout.item_contact_search_result,applications);
        new_friend_lv.setAdapter(resultAdapter);
        new_friend_lv.setOnItemClickListener((parent, view, position, id) -> {
            View.InviteView inviteView=friendApplications.get(position);
            ResultContact resultContact=applications.get(position);
            AlertDialog.Builder accept=new AlertDialog.Builder(NewFriendActivity.this);
            accept.setTitle("同意好友申请");
            accept.setMessage("确定接收 "+resultContact.name+" 的好友申请吗？");
            accept.setPositiveButton("接受", (dialog, which) -> {
                try {
                    Log.d(TAG, "showApplications: 同意申请："+inviteView.getId());
                    Textile.instance().invites.accept(inviteView.getId());

                    //忽略所有其他的同一个人来的申请
                    ContactUtil.ignoreOtherApplications(inviteView.getInviter().getAddress());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            accept.setNegativeButton("忽略", (dialog, which) -> {
                Toast.makeText(NewFriendActivity.this, "已取消", Toast.LENGTH_SHORT).show();
                try {
                    //忽略当前所有这个人的申请
                    ContactUtil.ignoreOtherApplications(inviteView.getInviter().getAddress());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            accept.show();
        });
    }

    private void initUI() {
        new_friend_lv=findViewById(R.id.new_friend_lv);
    }


}
