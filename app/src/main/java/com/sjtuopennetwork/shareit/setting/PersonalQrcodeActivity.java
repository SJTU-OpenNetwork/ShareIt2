package com.sjtuopennetwork.shareit.setting;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.qrlibrary.qrcode.utils.QRCodeUtil;
import com.sjtuopennetwork.shareit.R;

import sjtu.opennet.hon.Textile;

public class PersonalQrcodeActivity extends AppCompatActivity {

    private ImageView imageView;
    private String addr;
    private String peerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_qrcode);
        init();
    }

    private void init() {
        imageView=findViewById(R.id.imageView);
        try {
            addr=Textile.instance().profile.get().getAddress();
            peerID=Textile.instance().profile.get().getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(QRCodeUtil.CreateTwoDCode(addr+"##"+peerID+"##p"));
    }
}
