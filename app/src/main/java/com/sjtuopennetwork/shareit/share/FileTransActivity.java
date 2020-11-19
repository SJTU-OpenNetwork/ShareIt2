package com.sjtuopennetwork.shareit.share;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.share.util.TRecord;
import com.sjtuopennetwork.shareit.util.DBHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.util.FileUtil;

public class FileTransActivity extends AppCompatActivity {

    private static final String TAG = "===================FileTransActivity";

    private TextView trans_size; // 文件大小
    private TextView trans_rtt; // 平均rtt
    private TextView trans_rec; // 平均接收时间
    private TextView trans_send; // 平均发送时间

    private ListView recordsLv;
    private Button saveLog;
    private TextView getNum; // 收到反馈的个数
    TextView streamIdTV; // stream的id

    private String fileCid; // 文件id
    private LinkedList<TRecord> records; // 查到的个数
    SharedPreferences pref;
    String loginAccount;
    RecordAdapter adapter;
    DateFormat df = new SimpleDateFormat("HH:mm:ss:SSS");

    long startAdd = 0; // 开始发送的本地时间
    long rttSum = 0; // rtt的和
    long getSum = 0; // 接收时间的和
    long rttT = 0; // 平均rtt
    long getT = 0; // 平均接收时间
    long sendT = 0; // 发送时间
    int filesize = 0; // 文件大小

    long workerNum = 0; // worker数量
    int statType; // 统计的种类，0：ipfs， 1：stream， 2：multicast


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_trans);

        //初始化View
        streamIdTV = findViewById(R.id.stream_id);
        recordsLv = findViewById(R.id.recordd_lv);
        getNum = findViewById(R.id.get_num);
        trans_size = findViewById(R.id.file_trans_size);
        trans_rtt = findViewById(R.id.file_trans_rtt);
        trans_rec = findViewById(R.id.file_trans_rec_t);
        trans_send = findViewById(R.id.file_trans_send_t);
        Button writeTree = findViewById(R.id.write_tree_csv);
//        saveLog = findViewById(R.id.save_log);

        //初始化数据
        fileCid = getIntent().getStringExtra("fileCid");
        pref = getSharedPreferences("txtl", Context.MODE_PRIVATE);
        loginAccount = pref.getString("loginAccount", ""); //当前登录的account，就是address
        records = new LinkedList<>();
        statType = getIntent().getIntExtra("statType", 0);
        switch (statType) {
            case 0: //
                filesize = getIntent().getIntExtra("fileSize",0);
                records = DBHelper.getInstance(getApplicationContext(), loginAccount).listRecords(fileCid);
                writeTree.setVisibility(View.GONE);
                streamIdTV.setVisibility(View.GONE);
                trans_send.setText("发送时间："+(records.get(0).t2-records.get(0).t1)+" ms");
                break;
            case 1: // stream
                String fileSizeCid = getIntent().getStringExtra("fileSizeCid");
                File file = new File(fileSizeCid);
                filesize = (int) file.length();
                long sendTime = getIntent().getLongExtra("streamSendTime", 0);
                records = DBHelper.getInstance(getApplicationContext(), loginAccount).listRecords(fileCid, String.valueOf(sendTime));
                streamIdTV.setText(fileCid);
                workerNum = Textile.instance().streams.getWorker();
                trans_send.setText("发送时间：0 ms");
                writeTree.setOnClickListener(view -> {
                    String outDir = FileUtil.getAppExternalPath(FileTransActivity.this, "trees");
                    String outPath = outDir + File.separator + fileCid + ".csv";
                    Textile.instance().writeTreeCsv(fileCid, outPath);
                    Toast.makeText(this, "保存分发树：" + outPath, Toast.LENGTH_SHORT).show();
                });
                break;
            case 2: // multicast
                long fileSize1 = getIntent().getLongExtra("fileSize",0);
                filesize = (int)fileSize1;
                records = DBHelper.getInstance(getApplicationContext(), loginAccount).listRecords(fileCid);
                writeTree.setVisibility(View.GONE);
                streamIdTV.setVisibility(View.GONE);
                trans_send.setText("发送时间：0 ms");
                break;
        }

        startAdd = records.get(0).t1; //发送端开始发送的时间
        trans_size.setText("文件大小:" + filesize + " B");
        adapter = new RecordAdapter(FileTransActivity.this, R.layout.item_records, records);
        recordsLv.setAdapter(adapter);

        // 统计时间
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        Log.d(TAG, "onCreate: records size 2:" + records.size());
        processData();

        //savelog
//        saveLog.setOnClickListener(view -> {
//            saveLog();
//        });
    }

    private void saveLog() {
        if (statType!=0) {
            sendT = 0;
        }
        DateFormat dfd = new SimpleDateFormat("MM-dd HH:mm");
        String head = "文件大小:" + filesize +
                "\nworker:" + workerNum +
                "\n平均rtt:" + rttT +
                "\n平均接收时间:" + getT +
                "\n发送时间:" + sendT + "\n";
        String dir = FileUtil.getAppExternalPath(this, "txtllog");
        String logDate = dfd.format(System.currentTimeMillis());
        try {
            File logFile = new File(dir + "/" + fileCid + "_" + logDate + ".log.txt");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            Log.d(TAG, "onCreate: logpath: " + logFile.getAbsolutePath());
            FileWriter writer = new FileWriter(logFile);
            writer.write(head);
            writer.flush();

            for (TRecord tRecord : records) {
                String writeStr;
                String user = tRecord.recordFrom;
                String get1Str = df.format(tRecord.t1);
                String get2Str = df.format(tRecord.t2);
                long gap = tRecord.t2 - tRecord.t1;
                long grtt = tRecord.t3 - startAdd;
                if (statType!=1) {
                    gap = 0;
                    get1Str = "0";
                    get2Str = "0";
                }

                if(statType == 0){

                }else if(statType == 1){

                }else{

                }
                if (tRecord.type == 0) {
                    writeStr = "自身节点,开始:" + get1Str + ", 发完:" + get2Str + ", 耗时:" + gap + "ms\n";
                } else {
                    writeStr = "接收节点:" + user + ", 开始:" + get1Str + ", 收完:" + get2Str + ", 耗时:" + gap + "ms, rtt:" + grtt + "ms, " + "parent:" + tRecord.parent + "\n";
                }
                writer.write(writeStr);
                writer.flush();
            }
            writer.close();
            Toast.makeText(this, "保存log：" + logFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 这个是在主线程计算和显示
    private void processData() {
        int recvNum = records.size() - 1;rttSum = 0;
        for (int i = 1; i < records.size(); i++) {
            rttSum += (records.get(i).t3 - startAdd); //发送端从发送到接收的自己的时间，rtt
        }
        switch(statType){
            case 0: //ipfs
                getSum = 0;
                for (int i = 1; i < records.size(); i++) {
                    getSum += (records.get(i).t2 - records.get(i).t1); //接收端自己从get到done的时间
                }
                if (recvNum == 0) {
                    trans_rtt.setText("平均RTT:未收到返回");
                    trans_rec.setText("平均接收时间:未收到返回");
                } else {
                    rttT = rttSum / recvNum;
                    trans_rtt.setText("平均RTT:" + rttT + " ms");
                    getT = getSum / recvNum;
                    trans_rec.setText("平均接收时间:" + getT + " ms");
                }
                getNum.setText("收到 " + (records.size() - 1) + "个");
                break;
            case 1:
                getSum=0;
                for (int i = 1; i < records.size(); i++) {
                    getSum += records.get(i).t1;
                }
                if (recvNum == 0) {
                    trans_rtt.setText("平均RTT:未收到返回");
                    trans_rec.setText("平均接收时间:未收到返回");
                } else {
                    rttT = rttSum / recvNum;
                    trans_rtt.setText("平均RTT:" + rttT + " ms");
                    getT = getSum / recvNum;
                    trans_rec.setText("平均接收时间:" + getT + " ms");
                }
                getNum.setText("收到 " + (records.size() - 1) + "个，worker:" + workerNum);
                break;
            case 2:
                getSum=0;
                for (int i = 1; i < records.size(); i++) {
                    getSum += records.get(i).t1;
                }
                if (recvNum == 0) {
                    trans_rtt.setText("平均RTT:未收到返回");
                    trans_rec.setText("平均接收时间:未收到返回");
                } else {
                    rttT = rttSum / recvNum;
                    trans_rtt.setText("平均RTT:" + rttT + " ms");
                    getT = getSum / recvNum;
                    trans_rec.setText("平均接收时间:" + getT + " ms");
                }
                getNum.setText("收到 " + (records.size() - 1) + "个");
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getNewMsg(TRecord tRecord) {
        if (tRecord.cid.equals(fileCid)) {
            records.add(tRecord);
            adapter.notifyDataSetChanged();
            processData();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    class RecordAdapter extends ArrayAdapter {
        Context context;
        LinkedList<TRecord> arecords;
        int resource;

        public RecordAdapter(@NonNull Context context, int resource, LinkedList<TRecord> records) {
            super(context, resource, records);
            this.context = context;
            this.resource = resource;
            this.arecords = records;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v;
            RecordView recordView;
            TRecord tRecord = arecords.get(position);
            if (convertView == null) {
                v = LayoutInflater.from(context).inflate(resource, parent, false);
                recordView = new RecordView(v);
                v.setTag(recordView);
            } else {
                v = convertView;
                recordView = (RecordView) v.getTag();
            }

            String user = tRecord.recordFrom;
            String get1Str;
            String get2Str = df.format(tRecord.t2);
            if (statType!=0) {
                get1Str = "0";
                get2Str = "0";
            } else {
                get1Str = df.format(tRecord.t1);
            }
            long gap;
            long rttt = tRecord.t3 - startAdd;
            if (tRecord.type == 0) { //自己的信息
                Log.d(TAG, "getView: 显示自己：" + position);
                if (statType!=0) { // 只有simple才显示自己的信息
                    recordView.user.setVisibility(View.GONE);
                    recordView.duration.setVisibility(View.GONE);
                } else {
                    recordView.user.setText("自身信息");
                    gap = tRecord.t2 - tRecord.t1;
                    recordView.duration.setText("开始:" + get1Str + ",  发完:" + get2Str + "\n耗时:" + gap + "ms");
                }
                recordView.tvParent.setVisibility(View.GONE);
            } else { //收到反馈
                Log.d(TAG, "getView: jieshou: " + tRecord.t1);
                if (statType!=0) {
                    gap = tRecord.t1 - 0;
                } else {
                    gap = tRecord.t2 - tRecord.t1;
                }
                if(statType == 1){ //stream才显示parent信息
                    recordView.tvParent.setText("parent: " + tRecord.parent);
                }else{
                    recordView.tvParent.setVisibility(View.GONE);
                }
                if(user.length()<15){
                    recordView.user.setText("接收节点："+user);
                }else {
                    recordView.user.setText("接收节点:" + user.substring(0, 13) + "...");
                }
                recordView.duration.setText("开始:" + get1Str + ",  收完:" + get2Str + "\n耗时:" + gap + "ms,  rtt:" + rttt + "ms");
            }

            return v;
        }

        class RecordView {
            TextView user;
            TextView duration;
            TextView tvParent;

            public RecordView(View v) {
                user = v.findViewById(R.id.time_user);
                duration = v.findViewById(R.id.time_duration);
                tvParent = v.findViewById(R.id.tv_parent);
            }
        }
    }
}
