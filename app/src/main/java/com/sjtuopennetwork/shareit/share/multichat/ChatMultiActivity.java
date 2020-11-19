package com.sjtuopennetwork.shareit.share.multichat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.share.FileTransActivity;
import com.sjtuopennetwork.shareit.share.util.MsgType;
import com.sjtuopennetwork.shareit.share.util.TMsg;
import com.sjtuopennetwork.shareit.share.util.TMsgAdapter;
import com.sjtuopennetwork.shareit.util.DBHelper;
import com.sjtuopennetwork.shareit.util.ShareUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import sjtu.opennet.hon.MulticastFile;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.multicastfile.HONMulticaster;
import sjtu.opennet.multicastfile.MulticastConfig;
import sjtu.opennet.multicastfile.video.MultiVideoSender;
import sjtu.opennet.util.FileUtil;

public class ChatMultiActivity extends AppCompatActivity {
    private static final String TAG = "============ChatMultiActivity";

    LinearLayout adding_multi_file;
    ImageView bt_add_file;
    Button chat_multi_send_text;
    TextView bt_multi_send_img;
    TextView bt_multi_send__file;
    TextView bt_multi_send_video;
    ListView chat_multi_lv;
    EditText chat_multi_text_edt;

    public SharedPreferences pref;

    boolean addingFile = false;
    static final int MULTI_FILE=4785;
    static final int MULTI_PIC=4378;
    static final int MULTI_VIDEO=8764;
    static final String MULTI_THREAD_ID ="20200729multicast";
    String loginAccount;
    String myName;
    String myAvatar;
    List<TMsg> msgList;
    TMsgAdapter msgAdapter;
    float xiansuTime;
    int packetSize;
    int speed;
    List<String> chooseFilePath;
    List<LocalMedia> choosePic;
    boolean textileOn=false;
    List<LocalMedia> chooseVideo;
    String rs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_multi);

        initMulti();
    }

    @Override
    protected void onStart() {
        super.onStart();

        msgList= DBHelper.getInstance(getApplicationContext(),loginAccount).list3000Msg("20200729multicast");
        msgAdapter=new TMsgAdapter(this,msgList,"20200729multicast");
        chat_multi_lv.setAdapter(msgAdapter);
        chat_multi_lv.setSelection(msgList.size());

        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    private void initMulti() {
        pref=getSharedPreferences("txtl",MODE_PRIVATE);
        loginAccount=pref.getString("loginAccount",""); //当前登录的account，就是address
        textileOn=pref.getBoolean("textileon",false);

        if(textileOn) {
            try {
                myName = Textile.instance().profile.name();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            myName=pref.getString("myname","null");
            myAvatar=pref.getString("avatarpath","null");
        }

        adding_multi_file=findViewById(R.id.file_multi_layout);
        bt_add_file=findViewById(R.id.bt_multi_add_file);
        chat_multi_send_text=findViewById(R.id.chat_multi_send_text);
        bt_multi_send_img=findViewById(R.id.bt_multi_send_img);
        bt_multi_send__file=findViewById(R.id.bt_multi_send__file);
        bt_multi_send_video=findViewById(R.id.bt_multi_send__video);
        chat_multi_lv=findViewById(R.id.chat_multi_lv);
        chat_multi_text_edt=findViewById(R.id.chat_multi_text_edt);

        adding_multi_file.setVisibility(View.GONE);
        bt_add_file.setOnClickListener(view -> {
            if(addingFile){
                addingFile=false;
                adding_multi_file.setVisibility(View.GONE);
            }else{
                addingFile=true;
                adding_multi_file.setVisibility(View.VISIBLE);
            }
        });

        chat_multi_send_text.setOnClickListener(view -> {
            String msgTxt=chat_multi_text_edt.getText().toString();
            if(msgTxt.equals("")){
                Toast.makeText(ChatMultiActivity.this, "消息不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            chat_multi_text_edt.setText("");
            TMsg tMsg= null;
            long nowTime=System.currentTimeMillis();
            try {
                tMsg= DBHelper.getInstance(getApplicationContext(),loginAccount).insertMsg(
                        MULTI_THREAD_ID,10,String.valueOf(nowTime),myName,msgTxt,nowTime/1000,1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            msgList.add(tMsg);
            msgAdapter.notifyDataSetChanged();
            chat_multi_lv.setSelection(msgList.size());

            MulticastFile multicastFile=new MulticastFile(MULTI_THREAD_ID,String.valueOf(nowTime),myName,"",msgTxt,nowTime, MulticastFile.MulticastFileType.TEXT);
            setAndSend(multicastFile);
        });

        bt_multi_send_img.setOnClickListener(view -> PictureSelector.create(ChatMultiActivity.this)
                .openGallery(PictureMimeType.ofImage())
                .maxSelectNum(1)
                .compress(false)
                .forResult(MULTI_PIC));

        bt_multi_send__file.setOnClickListener(view -> {
            PopupMenu file_select_menu = new PopupMenu(ChatMultiActivity.this, view);
            file_select_menu.getMenuInflater().inflate(R.menu.file_select, file_select_menu.getMenu());
            file_select_menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch(menuItem.getItemId()){
                        case R.id.file_select_selector:
                            new LFilePicker()
                                    .withActivity(ChatMultiActivity.this)
                                    .withRequestCode(MULTI_FILE)
                                    .withMutilyMode(false)//false为单选
                                    .withTitle("文件选择")//标题
                                    .start();
                            break;
                        case R.id.file_select_generate:
                            EditText inputs = new EditText(ChatMultiActivity.this);
                            inputs.setInputType(InputType.TYPE_CLASS_NUMBER);
                            //inputs.setText("aa");
                            AlertDialog.Builder builder = new AlertDialog.Builder(ChatMultiActivity.this);
                            builder.setTitle("文件大小(KB)").setView(inputs)
                                    .setNegativeButton("取消", (dialogInterface, i) -> dialogInterface.dismiss());
                            builder.setPositiveButton("确定", (dialogInterface, i) -> {
                                String tmp=inputs.getText().toString();
                                if(tmp.equals("")){
                                    Toast.makeText(ChatMultiActivity.this, "不能为空", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                int inputSize = Integer.parseInt(tmp);
                                if (inputSize > 0) {
                                    String outDir = FileUtil.getAppExternalPath(ChatMultiActivity.this, "generatedFile");
                                    String testFilePath = FileUtil.generateTestFile(outDir, inputSize);
                                    Log.d(TAG, "onActivityResult: get file path: "+testFilePath);

                                    sendFile(testFilePath, MsgType.MSG_MULTI_FILE, MulticastFile.MulticastFileType.FILE);
                                }
                            });
                            builder.show();
                    }

                    return false;
                }
            });

            file_select_menu.show();
        });

        bt_multi_send_video.setOnClickListener(v->{
            PictureSelector.create(ChatMultiActivity.this)
                    .openGallery(PictureMimeType.ofVideo())
                    .maxSelectNum(1)
                    .compress(false)
                    .forResult(MULTI_VIDEO);
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateChat(TMsg tMsg) {
        if (tMsg.threadid.equals(MULTI_THREAD_ID)) {
            Log.d(TAG, "updateChat: " + tMsg.msgType + " " + tMsg.body+" "+System.currentTimeMillis());

            switch (tMsg.msgType) {
                case MsgType.MSG_MULTI_VIDEO:
                    String[] posterVideo = tMsg.body.split("##");
                    Log.d(TAG, "updateChat: posetrVideo: " + posterVideo[0] + " " + posterVideo[1]);

                    Intent it=new Intent(this, MulticastVideoPlayActivity.class);
                    it.putExtra("videoId",posterVideo[1]);
                    startActivity(it);
                    msgList.add(tMsg);
                    break;
                case MsgType.MSG_MULTI_FILE:
                    int i=0;
                    int k=msgList.size();
                    for (;i<k;i++){
                        if(msgList.get(i).blockid.equals(tMsg.blockid)){
                            break;
                        }
                    }
                    if(i<k) {
                        msgList.remove(i);
                    }
                    msgList.add(i,tMsg);
                    break;
                case MsgType.MSG_MULTI_TEXT:
                case MsgType.MSG_MULTI_FILE_START:
                case MsgType.MSG_MULTI_PICTURE:
//                case MsgType.MSG_MULTI_PICTURE_START:
                    msgList.add(tMsg);
                    break;
            }

            msgAdapter.notifyDataSetChanged();
            chat_multi_lv.setSelection(msgList.size());
        }
    }

    public void setAndSend(MulticastFile multicastFile){
        speed = pref.getInt("speed",2000);
        rs=pref.getString("rs","200,56");
        String[] dp=rs.split(",");

        MulticastConfig config=new MulticastConfig(Integer.parseInt(dp[0]),Integer.parseInt(dp[1]),speed);
        HONMulticaster.sendMulticastFile(multicastFile,config);
    }

    private void sendFile(String filePath, int fileType, MulticastFile.MulticastFileType multicastFileType){
        String fileName=FileUtil.getFileNameFromPath(filePath);
        TMsg tMsg= null;
        long nowTime=System.currentTimeMillis();
        String nowTimeStr=String.valueOf(nowTime);
        long fileLength=new File(filePath).length();

        JSONObject jsonObject=new JSONObject();
        jsonObject.put("fileCid",String.valueOf(nowTime));
        jsonObject.put("fileSize",fileLength);
        jsonObject.put("filePath",filePath);

        try {
            tMsg=DBHelper.getInstance(getApplicationContext(),loginAccount).insertMsg(
                    MULTI_THREAD_ID,fileType,nowTimeStr,myName,jsonObject.toJSONString(),nowTime/1000,1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        msgList.add(tMsg);
//        msgAdapter.notifyDataSetChanged();
//        chat_multi_lv.setSelection(msgList.size());

        MulticastFile multicastFile=new MulticastFile(MULTI_THREAD_ID,nowTimeStr,myName,fileName,filePath,nowTime, multicastFileType);
        setAndSend(multicastFile);
        long addT1=System.currentTimeMillis();
        DBHelper.getInstance(getApplicationContext(),loginAccount).recordLocalStartAdd(nowTimeStr,addT1,0);

        Intent itToFileTrans = new Intent(this, FileTransActivity.class);
        itToFileTrans.putExtra("fileCid", nowTimeStr);
        itToFileTrans.putExtra("fileSize", fileLength);
        itToFileTrans.putExtra("statType",2);
        startActivity(itToFileTrans);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == MULTI_FILE && resultCode==RESULT_OK){
            chooseFilePath = data.getStringArrayListExtra("paths");
            String filePath = chooseFilePath.get(0);
            sendFile(filePath, MsgType.MSG_MULTI_FILE, MulticastFile.MulticastFileType.FILE);
        }else if(requestCode == MULTI_PIC && resultCode==RESULT_OK){
            choosePic=PictureSelector.obtainMultipleResult(data);
            String filePath=choosePic.get(0).getPath();
            sendFile(filePath,MsgType.MSG_MULTI_PICTURE, MulticastFile.MulticastFileType.IMG);
        }else if(requestCode == MULTI_VIDEO && resultCode==RESULT_OK){
            chooseVideo=PictureSelector.obtainMultipleResult(data);
            String filePath=chooseVideo.get(0).getPath();
            Log.d(TAG, "onActivityResult: "+filePath);

            long nowTime=System.currentTimeMillis()/1000;

            MultiVideoSender sender=new MultiVideoSender(this,MULTI_THREAD_ID,filePath);

            // 发送端显示
            String videoId= sender.getVideoId();
            Bitmap tmpBmap = sender.getPosterBitmap(); //拿到缩略图
            String tmpdir = ShareUtil.getAppExternalPath(this, "temp");
            String posterPath=tmpdir+"/"+videoId; //随机给一个名字
            ShareUtil.saveBitmap(posterPath,tmpBmap);
            String posterAndFile=posterPath+"##"+filePath;
            Log.d(TAG, "onActivityResult: poster_file"+posterAndFile);
            TMsg tMsg= null;
            try {
                tMsg=DBHelper.getInstance(getApplicationContext(),loginAccount).insertMsg(
                        MULTI_THREAD_ID,4,String.valueOf(nowTime),myName,posterAndFile,nowTime,1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            msgList.add(tMsg);
            msgAdapter.notifyDataSetChanged();
            chat_multi_lv.setSelection(msgList.size());

            // 发送
            sender.startSend(posterPath);
        }
    }
}
