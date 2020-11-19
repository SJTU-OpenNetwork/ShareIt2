package com.sjtuopennetwork.shareit.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.qrlibrary.qrcode.utils.QRCodeUtil;
import com.sjtuopennetwork.shareit.R;

public class ShadowCodeActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sahdow_code);

        imageView=findViewById(R.id.shadow_code_back);

        SharedPreferences pref=getSharedPreferences("txtl", Context.MODE_PRIVATE);
        String swAddr=pref.getString("shadowPeer","");
        if(!swAddr.equals("")){
            imageView.setImageBitmap(QRCodeUtil.CreateTwoDCode(swAddr));
        }
    }
}
