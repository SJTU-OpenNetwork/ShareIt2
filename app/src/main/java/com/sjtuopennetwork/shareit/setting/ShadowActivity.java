package com.sjtuopennetwork.shareit.setting;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qrlibrary.qrcode.utils.PermissionUtils;
import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.util.QRCodeActivity;

import java.util.ArrayList;
import java.util.List;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.textilepb.Model;

public class ShadowActivity extends AppCompatActivity {
    private static final String TAG = "==============ShadowActivity";

    //UI控件
    LinearLayout shadow_scan_code;
    LinearLayout shadow_twod_code;
    TextView shadow_connect_state;

    // 内存数据
    boolean isConnect = false;
    String shadowPeer;

    //持久化存储
    SharedPreferences pref;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: handler get res");
            drwaUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shadow);

        pref = getSharedPreferences("txtl", Context.MODE_PRIVATE);

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        drwaUI();

        getWindow().getDecorView().post(() -> checkClipboard());
    }

    void checkClipboard() {
        if(isConnect){
            return;
        }
        ClipboardManager clipboardManager = (ClipboardManager)getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        Log.d(TAG, "checkClipboard: 获取剪贴板："+clipboardManager.hasPrimaryClip());
        if (null != clipboardManager) {
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (null != clipData && clipData.getItemCount() > 0) {
                ClipData.Item item = clipData.getItemAt(0);
                if (null != item) {
                    String content = item.getText().toString();

                    //handle content to connect to the shadow node
                    if(content.length()<10){
                        return;
                    }
                    Log.d(TAG, "checkClipboard: 0-8: "+content.substring(0,8));
                    if(content.substring(0,8).equals("12D3KooW")){
                        Log.d(TAG, "checkClipboard: 这是一个ID："+content);
                        Textile.instance().connectShadowRelay(content);

                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("shadowPeer", content);
                        editor.commit();

                        handler.sendMessage(handler.obtainMessage());
                    }else{
                        Log.d(TAG, "checkClipboard: 剪贴板不是peer id:"+content);
                    }
                }
            }
        }
    }

//    private void checkClipBoard() {
//        new Thread(){
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
////        ClipData data = cm.getPrimaryClip();
//                Log.d(TAG, "checkClipBoard: data长度："+ cm.hasPrimaryClip());
////        ClipData.Item item = data.getItemAt(0);
////        String content = item.getText().toString();
////        if(!isConnect) {
////            if (content.substring(0, 8).equals("12D3KooW")) { //如果是peer id
////                Log.d(TAG, "checkClipBoard: 这是剪贴板的内容：" + content);
////            }
////        }
//            }
//        };
//    }

    public void initUI() {
        shadow_scan_code = findViewById(R.id.shadow_scan_code);
        shadow_twod_code = findViewById(R.id.shadow_twod_code);
        shadow_connect_state = findViewById(R.id.shadow_connect_state);

        shadow_scan_code.setOnClickListener(v -> { //跳转到扫码连接
            PermissionUtils.getInstance().requestPermission(ShadowActivity.this);
            Intent it = new Intent(ShadowActivity.this, QRCodeActivity.class);
            startActivity(it);
        });
    }

    public void drwaUI() {
        isConnect=false;
        String tmp=pref.getString("shadowPeer","");
        Log.d(TAG, "drwaUI: swarm peer: "+tmp);
        if(!tmp.equals("")){
            String[] addrs=tmp.split("/");
            shadowPeer=addrs[addrs.length-1];
        }
        try {
            List<Model.SwarmPeer> peers = Textile.instance().ipfs.connectedAddresses().getItemsList();
            for (Model.SwarmPeer s : peers) {
                Log.d(TAG, "drwaUI: swarm peer: "+s.getId());
                if(s.getId().equals(shadowPeer)){
                    isConnect=true;
                    break;
                }
//                String[] ss = s.getId().split("/");
//                if (ss[ss.length-1].equals(shadowPeer)) {
//                    isConnect = true;
//                    break;
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isConnect) { //连接成功
            Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show();
            shadow_connect_state.setText("连接成功"); //要求任何地方连接成功或失败都要同时更新两个状态。
            shadow_twod_code.setOnClickListener(v -> { //跳转到二维码
                Intent it = new Intent(ShadowActivity.this, ShadowCodeActivity.class);
                startActivity(it);
            });
        } else {
            shadow_connect_state.setText("未连接");
            shadow_twod_code.setOnClickListener(v -> { //弹出提示未连接
                Toast.makeText(this, "未连接影子节点", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
