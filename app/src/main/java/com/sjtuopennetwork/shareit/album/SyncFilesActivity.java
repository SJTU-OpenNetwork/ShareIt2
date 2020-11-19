package com.sjtuopennetwork.shareit.album;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;

import com.leon.lfilepickerlibrary.LFilePicker;
import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.album.util.FilesAdapter;
import com.sjtuopennetwork.shareit.album.util.PhotoAdapter;
import com.sjtuopennetwork.shareit.util.ShareUtil;

import java.util.ArrayList;
import java.util.List;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.textilepb.Model;
import sjtu.opennet.textilepb.View;

public class SyncFilesActivity extends AppCompatActivity {
    private static final String TAG = "==============";

    ImageView addFile;
    ImageView syncFile;
    RecyclerView recyclerView;
    FilesAdapter filesAdapter;

    String threadName="file1219";
    Model.Thread fileThread;
    List<String> chooseFilePath;
    ArrayList<String> fileNames;
    ArrayList<String> fileHashs;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1: //add to list
                    String name=msg.getData().getString("fileName");
                    String hash=msg.getData().getString("fileHash");
                    fileNames.add(name);
                    fileHashs.add(hash);
                    filesAdapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_files);

        fileThread= ShareUtil.getThreadByName(threadName);

        fileNames=new ArrayList<>();
        fileHashs=new ArrayList<>();
        getAllFiles();

        initUI();
    }

    private void initUI() {
        addFile=findViewById(R.id.sync_file_add);
        syncFile=findViewById(R.id.sync_file_sync);

        addFile.setOnClickListener(v -> {
            new LFilePicker()
                    .withActivity(SyncFilesActivity.this)
                    .withRequestCode(293)
                    .withMutilyMode(false)//false为单选
//                    .withFileFilter(new String[]{".txt",".png",".jpeg",".jpg" })//设置可选文件类型
                    .withTitle("文件选择")//标题
                    .start();
        });

        //设置adapter
        recyclerView=findViewById(R.id.sync_file_rv);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        filesAdapter=new FilesAdapter(this,fileNames,fileHashs);
        recyclerView.setAdapter(filesAdapter);
    }

    private void getAllFiles() {
        try {
            List<View.Files> files= Textile.instance().files.list(fileThread.getId(),"",1000).getItemsList();
            Log.d(TAG, "getAllFiles: "+files.size());
            for(View.Files fs:files){
                String name=fs.getFiles(0).getFile().getName();
                String hash=fs.getFiles(0).getFile().getHash();
                fileNames.add(name);
                fileHashs.add(hash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 293 &&resultCode == RESULT_OK) {

            chooseFilePath = data.getStringArrayListExtra("paths");
            for (int i = 0; i < chooseFilePath.size(); i++) {
                String path = chooseFilePath.get(i);
                String chooseFileName=ShareUtil.getFileNameWithSuffix(path);

                //发送照片
                Textile.instance().files.addFiles(chooseFilePath.get(i), fileThread.getId(), chooseFileName, new Handlers.BlockHandler() {
                    @Override
                    public void onComplete(Model.Block block) {
                        try {
                            System.out.println("===comp:"+chooseFileName);
                            String hash = Textile.instance().files.list(fileThread.getId(), "", 1).getItemsList().get(0).getFiles(0).getFile().getHash();
                            String newFileName=Textile.instance().files.list(fileThread.getId(), "", 1).getItemsList().get(0).getFiles(0).getFile().getName();

                            Message msg=new Message();
                            msg.what=1;
                            Bundle b=new Bundle();
                            b.putString("fileName",newFileName);
                            b.putString("fileHash",hash);
                            msg.setData(b);
                            handler.sendMessage(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });

            }

        }
    }
}
