package com.sjtuopennetwork.shareit.share;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alibaba.fastjson.JSONObject;
import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.share.util.DialogAdapter;
import com.sjtuopennetwork.shareit.share.util.MsgType;
import com.sjtuopennetwork.shareit.share.util.TDialog;
import com.sjtuopennetwork.shareit.share.util.TMsg;
import com.sjtuopennetwork.shareit.util.DBHelper;

import java.util.LinkedList;
import java.util.List;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.textilepb.Model;

public class ForwardActivity extends AppCompatActivity {
    private static final String TAG = "========ForwardActivity";

    private SharedPreferences pref;
    private String loginAccount;
    private List<TDialog> dialogs;
    private DialogAdapter dialogAdapter;
    ListView dialoglistView; //对话列表

    Intent it;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward);

        it=getIntent();

        pref=getSharedPreferences("txtl", Context.MODE_PRIVATE);
        loginAccount=pref.getString("loginAccount",""); //当前登录的account，就是address

        //从数据库中查出对话
        dialogs=new LinkedList<>();
        dialogs = DBHelper.getInstance(getApplicationContext(),loginAccount).queryAllDIalogs();
        Log.d(TAG, "initData: 对话数："+dialogs.size());

        //显示对话
        dialoglistView=findViewById(R.id.forward_dialogs);
        dialogAdapter=new DialogAdapter(this,R.layout.item_share_dialog,dialogs);
        dialoglistView.setAdapter(dialogAdapter);

        //转发
        int msgType=it.getIntExtra("msgType",5);
        dialoglistView.setOnItemClickListener((adapterView, view, position, l) -> {
            String threadId=dialogs.get(position).threadid;
            AlertDialog.Builder forwardMsg=new AlertDialog.Builder(ForwardActivity.this);
            forwardMsg.setTitle("确定转发消息？");
            forwardMsg.setPositiveButton("确定", (dialogInterface, i) -> {
                String body=it.getStringExtra("body");
                switch (msgType){
                    case MsgType.MSG_TEXT: // 文本消息
                        forwardText(threadId,body);
                        break;
                    case MsgType.MSG_PICTURE: //图片
                        forwardPhoto(threadId,body);
                        break;
                    case MsgType.MSG_STREAM_VIDEO: //stream视频
                        boolean isMine=it.getBooleanExtra("streamIsMine",false);
                        forwardStream(threadId,body,isMine);
                        break;
//                    case 3: //file
//                        String fileHashName=it.getStringExtra("fileHashName");
//                        forwardFile(threadId,fileHashName);
//                        break;
//                    case 4: //ticket视频
//                        break;
                }
            });
            forwardMsg.show();
        });
    }

    public void forwardFile(String threadId,String hashName){
        String[] hashNames=hashName.split("##");
        Log.d(TAG, "forwardFile: 转发文件："+hashNames[0]);

        //发送文件
        Textile.instance().files.addFiles(hashNames[0], threadId, hashNames[1], new Handlers.BlockHandler() {
            @Override
            public void onComplete(Model.Block block) { finish(); }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void forwardText(String threadId, String textBody){
        try {
            Textile.instance().messages.add(threadId, textBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    public void forwardPhoto(String threadId,String body){
        JSONObject jsonObject = JSONObject.parseObject(body);
        Textile.instance().files.addPicture(jsonObject.getString("fileHash"), threadId,jsonObject.getString("fileName") , new Handlers.BlockHandler() {
            @Override
            public void onComplete(Model.Block block) {
                finish();
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void forwardStream(String threadId,String streamBody, boolean isMine){
        String[] tmp=streamBody.split("##");
        String streamId="";
        String myName="";
        if(isMine){
            streamId=tmp[2];
        }else{
            streamId=tmp[1];
        }

        // add stream to thread
        try {
            myName=Textile.instance().profile.name();
            Textile.instance().streams.threadAddStream(threadId,streamId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // add msg to db
        try {
            long l=System.currentTimeMillis()/1000;
            DBHelper.getInstance(getApplicationContext(),loginAccount).insertMsg(
                    threadId,2,String.valueOf(l),myName,streamBody,l,1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
