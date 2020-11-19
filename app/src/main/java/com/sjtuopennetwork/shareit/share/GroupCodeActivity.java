package com.sjtuopennetwork.shareit.share;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.example.qrlibrary.qrcode.utils.QRCodeUtil;
import com.sjtuopennetwork.shareit.R;

import java.net.InetAddress;

import sjtu.opennet.hon.Textile;
import sjtu.opennet.textilepb.View;

public class GroupCodeActivity extends AppCompatActivity {

    private static final String TAG = "=====================";

    String threadID;
    String codeSource;
    String key;
    String inviteID;
    String Addr;
    String localIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_code);

        threadID=getIntent().getStringExtra("threadid");
        System.out.println("================得到threadid："+threadID);

//        try {
//            View.ExternalInvite externalInvite = Textile.instance().invites.addExternal(threadID);
//            key=externalInvite.getKey();
//            inviteID=externalInvite.getId();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try{
            View.ExternalInvite externalInvite = Textile.instance().threads2.GetThreadAddrKey(threadID.toString());
            key=externalInvite.getKey();
            Addr=externalInvite.getId();
        }catch (Exception e) {
            e.printStackTrace();
        }
//        try{
//            localIp = InetAddress.getLocalHost().toString();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//        Log.d(TAG, "local host ip:"+localIp);
        codeSource=threadID+"##"+Addr+"##"+key+"##thread2";
        Log.d(TAG, "onCreate: code:"+ threadID + "   "+ Addr +"   "+key);
        ImageView imageView=findViewById(R.id.group_code);
        imageView.setImageBitmap(QRCodeUtil.CreateTwoDCode(codeSource));


    }
}
