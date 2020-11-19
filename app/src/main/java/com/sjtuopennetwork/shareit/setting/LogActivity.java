package com.sjtuopennetwork.shareit.setting;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.util.ShareUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mobile.HlogHandler;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.util.FileUtil;

public class LogActivity extends AppCompatActivity {

//    TextView showLog;
    Button bt;
    ListView log_list;

//    String log="";
    String pid;

    DateFormat dfd=new SimpleDateFormat("MM-dd-HH:mm");

//    String dir= ShareUtil.getLogDir();
    String dir= FileUtil.TXTL_LOG_DIR;

    LinkedList<String> logs=new LinkedList<>();

    LogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

//        showLog=findViewById(R.id.showlog);
        log_list=findViewById(R.id.log_listt);
        adapter=new LogAdapter(LogActivity.this,R.layout.item_logstr,logs);
        log_list.setAdapter(adapter);

        try {
            pid=Textile.instance().profile.get().getId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Textile.instance().getLog(new HlogHandler() {
            @Override
            public void handleLog(String s) {
                logs.add(s);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void logEnd() {

            }
        });

        bt=findViewById(R.id.save_txtl_log);
        bt.setOnClickListener(v->{
            String logDate=dfd.format(System.currentTimeMillis());
            File f=new File(dir+"/"+pid+"_"+logDate+".txt");
            try {
                FileWriter fr=new FileWriter(f);
                for(String s:logs){
                    fr.write(s+"\n");
                    fr.flush();
                }
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "保存成功："+f.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        });
    }

    class LogAdapter extends ArrayAdapter{
        Context context;
        LinkedList<String> logStrs;
        int resource;

        public LogAdapter (Context context, int resource, LinkedList<String> logStrs){
            super(context,resource,logStrs);
            this.context=context;
            this.resource=resource;
            this.logStrs=logStrs;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v;
            LogHolder logHolder;
            if(convertView==null){
                v= LayoutInflater.from(context).inflate(resource,parent,false);
                logHolder=new LogHolder(v);
                v.setTag(logHolder);
            }else{
                v=convertView;
                logHolder=(LogHolder) v.getTag();
            }

            logHolder.logStr.setText(logStrs.get(position));

            return v;
        }

        class LogHolder{
            TextView logStr;

            public LogHolder(View view) {
                this.logStr = view.findViewById(R.id.log_str);
            }
        }

    }

}
