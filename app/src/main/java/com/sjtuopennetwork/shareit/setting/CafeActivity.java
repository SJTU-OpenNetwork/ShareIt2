package com.sjtuopennetwork.shareit.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.sjtuopennetwork.shareit.R;

import sjtu.opennet.hon.Textile;

public class CafeActivity extends AppCompatActivity {

    //UI控件
    private ListView list;
    //持久化
    private SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe);

        initUI();
        initData();
    }



    private void initUI() {
        list=findViewById(R.id.listView_cafe);
    }
    private void initData() {
        pref=getSharedPreferences("txtl", Context.MODE_PRIVATE);

       // list= Textile.instance().cafes  目前底层还未提供获取cafe节点列表的功能
    }
}
