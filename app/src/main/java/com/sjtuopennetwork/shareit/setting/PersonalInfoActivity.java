package com.sjtuopennetwork.shareit.setting;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qrlibrary.qrcode.utils.PermissionUtils;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.sjtuopennetwork.shareit.util.CafeUtil;
import com.sjtuopennetwork.shareit.util.QRCodeActivity;
import com.sjtuopennetwork.shareit.util.RoundImageView;
import com.sjtuopennetwork.shareit.util.ShareUtil;
import com.syd.oden.circleprogressdialog.core.CircleProgressDialog;
import com.sjtuopennetwork.shareit.R;

import java.util.List;

import sjtu.opennet.textilepb.Model;
import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;

public class PersonalInfoActivity extends AppCompatActivity {

    private static final String TAG = "=========================";

    //UI控件
    private TextView info_name; //昵称
    private TextView info_addr; //公钥
    private TextView info_phrase; //助记词
    private TextView delay_time;
    private RoundImageView avatar_img;//头像
    private LinearLayout info_avatar_layout;   //头像板块
    private LinearLayout info_name_layout;  //昵称板块
    private LinearLayout info_addr_layout;  //公钥地址板块
    private LinearLayout info_phrase_layout;    //助记词板块
    private LinearLayout swarm_layout;
    private LinearLayout info_cafe;
    private TextView cafeCaption;
    private TextView cafeUrl;
    CircleProgressDialog circleProgressDialog;
//    private TextView shadowPid;
    private TextView ipfsPeerId;
//    private LinearLayout shadowInfoLayout;
//    private LinearLayout shadowBack;

    //持久化
    private SharedPreferences pref;

    //内存
    private String username;
    private String useravatar;
    private String myaddr;
    private String phrase;

    private Context mContext;
    private boolean connectCafe;
    boolean ok131;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        initUI();
        initData();
        drawUI();
    }
    private void drawUI() {
        info_name.setText(username);
        info_addr.setText(myaddr);
        info_phrase.setText(phrase);
        ShareUtil.setImageView(this,avatar_img,useravatar, ShareUtil.ImgType.AVATAR);
    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try{
                circleProgressDialog.dismiss();
            }catch (Exception e){
            }
            drawCafeInfo();
        }
    };

    public void drawCafeInfo(){
        Log.d(TAG, "drawCafeInfo, ok131:"+ok131+" connectCafe:"+connectCafe);
        ok131=pref.getBoolean("ok131",false);
        connectCafe=pref.getBoolean("connectCafe",false);
        if(ok131){
            cafeUrl.setVisibility(View.VISIBLE);
            cafeCaption.setText("cafe连接成功（点击停止）");
            cafeUrl.setText("http://202.120.38.131:40601");
        }else{
            cafeCaption.setText("未连接cafe（点击开始连接）");
            cafeUrl.setVisibility(View.GONE);
        }
    }

    private void initData() {
        pref=getSharedPreferences("txtl", Context.MODE_PRIVATE);

        drawCafeInfo();

        info_cafe.setOnClickListener(view -> {
            final android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(PersonalInfoActivity.this);
            dialog.setTitle("Cafe连接");
            dialog.setPositiveButton("连接Cafe", (dialogInterface, i) -> {
                if(ok131==true){
                    Toast.makeText(PersonalInfoActivity.this, "已经连接了cafe", Toast.LENGTH_SHORT).show();
                }else{
                    circleProgressDialog=new CircleProgressDialog(PersonalInfoActivity.this);
                    circleProgressDialog.setText("连接cafe...");
                    circleProgressDialog.showDialog();

                    CafeUtil.connectCafe(new Handlers.ErrorHandler() {
                        @Override
                        public void onComplete() {
                            Log.d(TAG, "onComplete: cafe131成功");
                            SharedPreferences.Editor editor=pref.edit();
                            editor.putBoolean("ok131",true);
                            editor.putBoolean("connectCafe",true);
                            editor.commit();

                            Message msg=new Message(); msg.what=1; handler.sendMessage(msg);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }
            });
            dialog.setNegativeButton("停止连接", (dialogInterface, i) -> {
                if(ok131==false){
                    Toast.makeText(PersonalInfoActivity.this, "已经停止", Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        circleProgressDialog=new CircleProgressDialog(PersonalInfoActivity.this);
                        circleProgressDialog.setText("连接cafe...");
                        circleProgressDialog.showDialog();

                        CafeUtil.stopConnect(new Handlers.ErrorHandler() {
                            @Override
                            public void onComplete() {
                                Log.d(TAG, "onComplete: 停止cafe连接");
                                connectCafe=false;
                                ok131=false;
                                SharedPreferences.Editor editor=pref.edit();
                                editor.putBoolean("ok131",false);
                                editor.putBoolean("connectCafe",false);
                                editor.commit();

                                Message msg=new Message(); msg.what=1; handler.sendMessage(msg);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            AlertDialog d = dialog.create();
            d.show();
        });

        username= ShareUtil.getMyName();
        useravatar=ShareUtil.getMyAvatar();
        Log.d(TAG, "initData: name avatar: "+username+" "+useravatar);

        phrase=pref.getString("phrase","");
        try {
            myaddr=Textile.instance().profile.get().getAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        avatar_img = findViewById((R.id.setting_avatar_niceview));
        info_name = findViewById(R.id.info_name);
        info_addr = findViewById(R.id.info_addr);
        info_avatar_layout = findViewById(R.id.setting_personal_info_avatar);
        info_name_layout = findViewById(R.id.setting_personal_info_name);
        info_addr_layout = findViewById(R.id.setting_personal_info_address);
        info_phrase_layout=findViewById(R.id.setting_personal_info_phrase);
        info_phrase=findViewById(R.id.info_phrase);
        swarm_layout=findViewById(R.id.swarm_layout);
        delay_time=findViewById(R.id.noti_time);

        cafeCaption=findViewById(R.id.cafe_caption);
        cafeUrl=findViewById(R.id.personal_cafe_url);
        info_cafe=findViewById(R.id.personal_info_cafe);

        ipfsPeerId=findViewById(R.id.ipfsPeerId);
        try {
            ipfsPeerId.setText(Textile.instance().ipfs.peerId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        info_avatar_layout.setOnClickListener(v -> {
            com.luck.picture.lib.PictureSelector.create(PersonalInfoActivity.this)
                .openGallery(PictureMimeType.ofImage())
                .maxSelectNum(1)
                .compress(true)
                .enableCrop(true).withAspectRatio(1,1)
                .forResult(PictureConfig.TYPE_IMAGE);
        });

        info_name_layout.setOnClickListener(view -> {
            Intent it=new Intent(PersonalInfoActivity.this,InfoNameActivity.class);
            startActivity(it);
        });
        info_addr_layout.setOnClickListener(view -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 将文本内容放到系统剪贴板里。
            cm.setText(info_addr.getText());
            Toast.makeText(this,"已将地址复制到剪贴板："+info_addr.getText(),Toast.LENGTH_LONG).show();
        });
        info_phrase_layout.setOnClickListener(view -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 将文本内容放到系统剪贴板里。
            cm.setText(info_phrase.getText());
            Toast.makeText(this,"已将助记词复制到剪贴板："+info_phrase.getText(),Toast.LENGTH_LONG).show();
        });
        swarm_layout.setOnClickListener(view -> {
            Intent it=new Intent(this,SwarmActivity.class);
            startActivity(it);
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initUI();
        initData();
        drawUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*结果回调*/
        if (requestCode == PictureConfig.TYPE_IMAGE) {
            if (data != null) {
                List<LocalMedia> choosePic= com.luck.picture.lib.PictureSelector.obtainMultipleResult(data);
                String picturePath=choosePic.get(0).getCompressPath();

                Textile.instance().profile.setAvatar(picturePath, new Handlers.BlockHandler() {
                    @Override
                    public void onComplete(Model.Block block) {
                        System.out.println("头像设置成功！");
                    }

                    @Override
                    public void onError(Exception e) {
                        System.out.println("头像设置失败！");
                    }
                });
                avatar_img.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                avatar_img.setCornerRadius(5);
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }
}
