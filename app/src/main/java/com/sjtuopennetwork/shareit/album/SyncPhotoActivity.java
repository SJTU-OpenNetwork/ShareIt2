package com.sjtuopennetwork.shareit.album;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.album.util.PhotoAdapter;
import com.sjtuopennetwork.shareit.share.ChatActivity;
import com.sjtuopennetwork.shareit.share.util.TMsg;
import com.sjtuopennetwork.shareit.util.ShareUtil;

import java.util.ArrayList;
import java.util.List;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.textilepb.Model;
import sjtu.opennet.textilepb.View;

public class SyncPhotoActivity extends AppCompatActivity {

    private static final String TAG = "==============";

    ImageView addPhoto;
    ImageView syncPhoto;
    RecyclerView recyclerView;
    PhotoAdapter photoAdapter;

    String threadname="photo1219";
    Model.Thread photoThread;
    List<LocalMedia> choosePic;
    ArrayList<String> photoHashs;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String newHash=msg.getData().getString("newHash");
            String newName=msg.getData().getString("newName");
            photoHashs.add(newHash+"##"+newHash);
            photoAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_photo);

        photoThread= ShareUtil.getThreadByName(threadname);

        photoHashs=new ArrayList();
        getAllPhotoHashs();

        initUI();
    }

    private void initUI() {
        addPhoto=findViewById(R.id.sync_photo_add);
        syncPhoto=findViewById(R.id.sync_photo_sync);

        addPhoto.setOnClickListener(v -> {
            PictureSelector.create(SyncPhotoActivity.this)
                    .openGallery(PictureMimeType.ofImage())
                    .maxSelectNum(1)
                    .compress(false)
                    .forResult(PictureConfig.TYPE_IMAGE);
        });

        //设置adapter
        recyclerView=findViewById(R.id.sync_photo_rv);
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,4);
        recyclerView.setLayoutManager(gridLayoutManager);
        photoAdapter=new PhotoAdapter(this,photoHashs,photoThread.getId());
        recyclerView.setAdapter(photoAdapter);
        photoAdapter.notifyDataSetChanged();
    }

    private void getAllPhotoHashs() {
        try{
            List<View.Files> photos=Textile.instance().files.list(photoThread.getId(),"",1000).getItemsList();
            for(View.Files fs:photos) {
                String hash = fs.getFiles(0).getFile().getHash();
                String name = fs.getFiles(0).getFile().getName();
                photoHashs.add(hash+"##"+name);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PictureConfig.TYPE_IMAGE && resultCode == RESULT_OK) {
            choosePic = PictureSelector.obtainMultipleResult(data);
            String filePath = choosePic.get(0).getPath();
            String fileName=ShareUtil.getFileNameWithSuffix(filePath);
            //发送照片
            Textile.instance().files.addFiles(filePath, photoThread.getId(), fileName, new Handlers.BlockHandler() {
                @Override
                public void onComplete(Model.Block block) {
                    try {
                        String hash=Textile.instance().files.list(photoThread.getId(),"",1).getItemsList().get(0).getFiles(0).getFile().getHash();
                        String name=Textile.instance().files.list(photoThread.getId(),"",1).getItemsList().get(0).getFiles(0).getFile().getName();
                        Log.d(TAG, "onComplete: add pic success : "+hash);
                        Message msg=new Message();
                        msg.what=1;
                        Bundle b=new Bundle();
                        b.putString("newHash",hash);
                        b.putString("newName",name);
                        msg.setData(b);
                        handler.sendMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.d(TAG, "onError: syncphotofailed");
                    e.printStackTrace();
                }
            });

        }
    }
}
