package com.sjtuopennetwork.shareit.setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.login.MainActivity;
import com.sjtuopennetwork.shareit.util.DBHelper;
import com.sjtuopennetwork.shareit.util.RoundImageView;
import com.sjtuopennetwork.shareit.util.ShareUtil;

import org.greenrobot.eventbus.EventBus;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import sjtu.opennet.hon.Textile;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {

    private static final String TAG = "=====================";
    //UI控件
    RelativeLayout info_layout;
    LinearLayout qrcode_layout;
    LinearLayout notification_layout;
    LinearLayout devices_layout;
    LinearLayout shadow_layout;
    TextView tv_name;
    RoundImageView avatar_layout;
    TextView logout_layout;
    Button lookLog;

    Button setDegree;
    EditText degree;
    TextView show_worker;

    Button xiansu_bt;
    EditText xiansu;
    TextView xiansu_now;

    Button packet_bt;
    EditText packet_edit;
    TextView packet_now;

    Button stream_bt;
    EditText stream_edit;
    TextView stream_now;

    Button textileon_bt;
    Button multicast_res;

    Button sendUDP;

    Button speed_bt;
    EditText speed_edit;
    TextView speed_now;

    EditText data_edit;
    EditText parity_edit;
    TextView rs_now;
    Button rs_bt;

    //持久化
    private SharedPreferences pref;

    //内存
    private String username;
    private String useravatar;
    boolean textileOn;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        pref = getActivity().getSharedPreferences("txtl", Context.MODE_PRIVATE);
        textileOn=pref.getBoolean("textileon",false);

        initUI();
        initData();
        drawUI();
    }
    private static void sendPacket(byte[] packetContent) {
        try {
            DatagramPacket datagramPacket = new DatagramPacket(
                    packetContent,
                    packetContent.length,
                    InetAddress.getByName("192.168.2.146"),
                    9000
            );
            DatagramSocket sendSocket=new DatagramSocket();
            sendSocket.send(datagramPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initUI() {
        info_layout = getActivity().findViewById(R.id.setting_info_layout);
        qrcode_layout = getActivity().findViewById(R.id.setting_qrcode_layout);
        notification_layout = getActivity().findViewById(R.id.setting_notification_layout);
        avatar_layout = getActivity().findViewById(R.id.setting_avatar_photo);
        tv_name = getActivity().findViewById(R.id.myname);
        devices_layout = getActivity().findViewById(R.id.setting_devices_layout);
        logout_layout = getActivity().findViewById(R.id.logout);
        textileon_bt=getActivity().findViewById(R.id.textileon_bt);
        if(textileOn){
            textileon_bt.setText("关闭IPFS");
        }else {
            textileon_bt.setText("启动IPFS");
        }

        textileon_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(textileOn){
                    Toast.makeText(getActivity(), "关闭IPFS，需要重启应用", Toast.LENGTH_SHORT).show();

                    SharedPreferences.Editor editor=pref.edit();
                    editor.putBoolean("textileon",false);
                    editor.commit();
                }else{
                    EventBus.getDefault().post(Integer.valueOf(2562));

                    SharedPreferences.Editor editor=pref.edit();
                    editor.putBoolean("textileon",true);
                    editor.commit();
                }
            }
        });

        shadow_layout=getActivity().findViewById(R.id.shadow_layout);
        if(textileOn) {
            shadow_layout.setOnClickListener(v -> {
                Intent itToShadow = new Intent(getActivity(), ShadowActivity.class);
                getActivity().startActivity(itToShadow);
            });
        }

        setDegree=getActivity().findViewById(R.id.setDegree);
        degree=getActivity().findViewById(R.id.degree);
        show_worker=getActivity().findViewById(R.id.show_worker);
        if(textileOn) {
            long tmpworker = Textile.instance().streams.getWorker();
            show_worker.setText("(当前:" + tmpworker + ")");
            setDegree.setOnClickListener(view -> {
                int deg = Integer.parseInt(degree.getText().toString());
                Log.d(TAG, "initUI: degree: " + deg);
                Textile.instance().streams.setDegree(deg);
                long tmpworker1 = Textile.instance().streams.getWorker();
                show_worker.setText("(当前:" + tmpworker1 + ")");
                Toast.makeText(getActivity(), "设置成功", Toast.LENGTH_SHORT).show();
            });
        }

        stream_edit=getActivity().findViewById(R.id.stream_edit);
        stream_now=getActivity().findViewById(R.id.stream_now);
        stream_bt=getActivity().findViewById(R.id.stream_bt);
        long newXiansuTime=pref.getLong("streamxiansu",0);
        stream_now.setText("(当前:"+newXiansuTime+")");
        if(textileOn){
            stream_bt.setOnClickListener(v->{
                long xiansuTime=Long.parseLong(stream_edit.getText().toString());
                SharedPreferences.Editor editor = pref.edit();
                editor.putLong("streamxiansu", xiansuTime );
                editor.commit();
                long newXiansuTime1=pref.getLong("streamxiansu",0);
                stream_now.setText("(当前:"+newXiansuTime1+")");
                Textile.instance().streams.setSpeedIntervalMillis(newXiansuTime1);
                Toast.makeText(getActivity(), "设置成功", Toast.LENGTH_SHORT).show();
            });
        }

//        xiansu=getActivity().findViewById(R.id.xiansu);
//        xiansu_bt=getActivity().findViewById(R.id.xiansu_bt);
//        xiansu_now=getActivity().findViewById(R.id.xiansu_now);
//        float newXiansuTime=pref.getFloat("xiansu",1.0f);
//        xiansu_now.setText("(当前:"+newXiansuTime+")");
//        xiansu_bt.setOnClickListener(view -> {
//            float xiansuTime=Float.parseFloat(xiansu.getText().toString());
//            SharedPreferences.Editor editor = pref.edit();
//            editor.putFloat("xiansu", xiansuTime );
//            editor.commit();
//            float newXiansuTime1=pref.getFloat("xiansu",1.0f);
//            xiansu_now.setText("(当前:"+newXiansuTime1+")");
//            Toast.makeText(getActivity(), "设置成功", Toast.LENGTH_SHORT).show();
//        });

//        packet_edit=getActivity().findViewById(R.id.packet_edit);
//        packet_bt=getActivity().findViewById(R.id.packet_bt);
//        packet_now=getActivity().findViewById(R.id.packet_now);
//        int packetSize=pref.getInt("packet",1024);
//        packet_now.setText("(当前:"+packetSize+")");
//        packet_bt.setOnClickListener(view -> {
//            int xiansuTime=Integer.parseInt(packet_edit.getText().toString());
//            SharedPreferences.Editor editor = pref.edit();
//            editor.putInt("packet", xiansuTime );
//            editor.commit();
//            int newSize=pref.getInt("packet",1024);
//            packet_now.setText("(当前:"+newSize+")");
//            Toast.makeText(getActivity(), "设置成功", Toast.LENGTH_SHORT).show();
//        });

        speed_edit=getActivity().findViewById(R.id.speed_edit);
        speed_bt=getActivity().findViewById(R.id.speed_bt);
        speed_now=getActivity().findViewById(R.id.speed_now);
        int speed=pref.getInt("speed",2000);
        speed_now.setText("(当前:"+speed+")");
        speed_bt.setOnClickListener(view -> {
            int speedS=Integer.parseInt(speed_edit.getText().toString());
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("speed", speedS );
            editor.commit();
            int newSize=pref.getInt("speed",2000);
            speed_now.setText("(当前:"+newSize+")");
            Toast.makeText(getActivity(), "设置成功", Toast.LENGTH_SHORT).show();
        });

//        data_edit=getActivity().findViewById(R.id.data_edit);
//        parity_edit=getActivity().findViewById(R.id.parity_edit);
//        rs_now=getActivity().findViewById(R.id.rs_now);
//        rs_bt=getActivity().findViewById(R.id.rs_bt);
//        String dp=pref.getString("rs","200,56");
//        rs_now.setText("(当前:"+dp+")");
//        rs_bt.setOnClickListener(v->{
//            int dataS=Integer.parseInt(data_edit.getText().toString());
//            int parityS=Integer.parseInt(parity_edit.getText().toString());
//            SharedPreferences.Editor editor = pref.edit();
//            editor.putString("rs", dataS+","+parityS);
//            editor.commit();
//            String newRs=pref.getString("rs","200,56");
//            rs_now.setText(newRs);
//            Toast.makeText(getActivity(), "设置成功", Toast.LENGTH_SHORT).show();
//        });

        lookLog=getActivity().findViewById(R.id.looklog);
        if(textileOn) {
            lookLog.setOnClickListener(v -> {
                Intent itt = new Intent(getActivity(), LogActivity.class);
                startActivity(itt);
            });
        }

        multicast_res=getActivity().findViewById(R.id.multicast_res);
        multicast_res.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                SharedPreferences.Editor editor = pref.edit();
//                editor.putBoolean("huifu", true );
//                editor.commit();
//                Toast.makeText(getActivity(), "开启回复", Toast.LENGTH_SHORT).show();
//                MulticastHelper.setRes(true);
            }
        });

        sendUDP=getActivity().findViewById(R.id.sendUDP);
        sendUDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    @Override
                    public void run() {
                        for(int i=0;i<100;i++){
                            String s=String.valueOf(i);
                            byte[] meta = s.getBytes();
                            sendPacket(meta);
//                            try {
//                                Thread.sleep(1);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }
                }.start();
            }
        });

//        uploadLog=getActivity().findViewById(R.id.uploadlog);
//        uploadLog.setOnClickListener(view -> {
////            try {
////                final String logPath= FileUtil.getAppExternalPath(getActivity(),"repo")+"/"+Textile.instance().profile.get().getAddress()+"/logs/textile.log";
////
////                Handler handler=new Handler(){
////                    @Override
////                    public void handleMessage(Message msg) {
////                        switch (msg.what){
////                            case 0:
////                                Toast.makeText(getActivity(), "上传成功", Toast.LENGTH_SHORT).show(); break;
////                            case 1:
////                                Toast.makeText(getActivity(), "上传失败", Toast.LENGTH_SHORT).show(); break;
////                        }
////                    }
////                };
////
////                new Thread(){
////                    @Override
////                    public void run() {
////                        Log.d(TAG, "initUI: log路径："+logPath);
////                        String response=LogToHTTP.uploadLog(logPath);
////                        Log.d(TAG, "run: 上传结果："+response);
////                        if(response.equals("success")){
////                            Message msg=new Message(); msg.what=0;
////                            handler.sendMessage(msg);
////                        }else{
////                            Message msg=new Message(); msg.what=1;
////                            handler.sendMessage(msg);
////                        }
////                    }
////                }.start();
////
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
//        });

        if(textileOn) {
            info_layout.setOnClickListener(v -> {
                Intent it = new Intent(getActivity(), PersonalInfoActivity.class);
                startActivity(it);
            });
            qrcode_layout.setOnClickListener(v -> {
                Intent it = new Intent(getActivity(), PersonalQrcodeActivity.class);
                startActivity(it);
            });
            notification_layout.setOnClickListener(v -> {
                Intent it = new Intent(getActivity(), NotificationActivity.class);
                startActivity(it);
            });
            devices_layout.setOnClickListener(v -> {
                Intent it = new Intent(getActivity(), MyDevicesActivity.class);
                startActivity(it);
            });
            logout_layout.setOnClickListener(v -> {
                logout();
            });
        }
    }

    private void logout() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("退出登录");
        dialog.setMessage("确定要退出登录吗？");
        dialog.setPositiveButton("确定", (dialogInterface, i) -> {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isLogin", false);
            editor.commit();

            //销毁Texile及数据库
            EventBus.getDefault().post(new Integer(943));

            DBHelper.setNull();

            Intent it = new Intent(getActivity(), MainActivity.class);
            startActivity(it);

            getActivity().finish();
        });
        dialog.setNegativeButton("取消", (dialogInterface, i) -> { });
        AlertDialog d = dialog.create();
        d.show();
    }

    private void initData() {
        if(textileOn) {
            username = ShareUtil.getMyName();
            useravatar = ShareUtil.getMyAvatar();
        }else{
            username=pref.getString("myname","null");
            useravatar=pref.getString("avatarpath","null");
        }
        Log.d(TAG, "initData: name avatar: "+username+" "+useravatar);

        //得到设备信息
//        try {
//            Model.Thread t= ShareUtil.getThreadByName("mydevice1219");
//            Context context = this.getActivity().getApplicationContext();
//            String IP= GetIpAddress.getIPAddress(context);
//            String addr="/ip4/"+IP+"/tcp/40601/ipfs/"+ Textile.instance().ipfs.peerId();
//            System.out.println("=========="+addr);
//            String androidId = Settings.System.getString(context.getContentResolver(),
//                    Settings.Secure.ANDROID_ID);
//
//            String device = "厂商：" + android.os.Build.BRAND + "  型号：" + Build.MODEL + "#" + androidId + "%" + addr;//获取设备厂商、型号、系统id
//            Boolean flag = true;
//            sjtu.opennet.textilepb.View.TextList textList = Textile.instance().messages.list("", 100, t.getId());
//            for (int i = 0; i < textList.getItemsCount(); i++) {
//                if (textList.getItems(i).getBody().equals(device)) {
//                    flag = false;
//                    break;
//                }
//            }
//            if (flag == true) {
//                Textile.instance().messages.add(t.getId(), device);
//            }
//        } catch (Exception e) {
////            e.printStackTrace();
//        }
    }

    private void drawUI() {
        tv_name.setText(username);
        if(textileOn){
            ShareUtil.setImageView(getContext(), avatar_layout, useravatar, ShareUtil.ImgType.AVATAR);
        }else {
//            ShareUtil.setImageView(getContext(), avatar_layout, useravatar, 3);
        }
    }

}