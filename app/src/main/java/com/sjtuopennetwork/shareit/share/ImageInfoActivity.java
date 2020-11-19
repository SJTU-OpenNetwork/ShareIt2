package com.sjtuopennetwork.shareit.share;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.luck.picture.lib.photoview.PhotoView;
import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.util.ShareUtil;

import java.util.UUID;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;

public class ImageInfoActivity extends AppCompatActivity {

    private static final String TAG = "===========ImageInfoActivity";
    //UI控件
//    PhotoView photoView;
    ImageView showImg;

    //内存数据
    String imghash;
    byte[] photoData;
    String threadid;
    String imgName;
    boolean isSimple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_info);
        showImg = findViewById(R.id.show_img);

        Intent it = getIntent();

        boolean isMulti=it.getBooleanExtra("isMulti",false);
        if(isMulti){
           String path=it.getStringExtra("multiPath");
           Glide.with(ImageInfoActivity.this).load(path).into(showImg);
        }else{

            imgName = it.getStringExtra("imgname");
            isSimple = it.getBooleanExtra("isSimple",false);
//        Bundle b=it.getExtras();
//        photoData=b.getByteArray("photoData");
//        if(photoData!=null){
//            Glide.with(ImageInfoActivity.this).load(photoData).into(showImg);
//        }else{
            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    photoData = msg.getData().getByteArray("img");
                    Glide.with(ImageInfoActivity.this).load(photoData).into(showImg);
                }
            };

            imghash = it.getStringExtra("imghash");
            Log.d(TAG, "onCreate: " + imghash);
            if(isSimple){
                Textile.instance().ipfs.dataAtPath(imghash, new Handlers.DataHandler() {
                    @Override
                    public void onComplete(byte[] data, String media) {
                        Log.d(TAG, "onComplete: 查看大图：" + data.length + " " + imghash);
                        Bundle b = new Bundle();
                        b.putByteArray("img", data);
                        Message msg = new Message();
                        msg.what = 1;
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }else{
                Textile.instance().files.content(imghash, new Handlers.DataHandler() {
                    @Override
                    public void onComplete(byte[] data, String media) {
                        Log.d(TAG, "onComplete: 查看大图：" + data.length + " " + imghash);
                        Bundle b = new Bundle();
                        b.putByteArray("img", data);
                        Message msg = new Message();
                        msg.what = 1;
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
            }
        }







//        }
        showImg.setOnClickListener(view -> finish());
//        showImg.setOnLongClickListener(v -> {//弹出对话框提示是否要保存到本地
//            AlertDialog.Builder storeImg = new AlertDialog.Builder(ImageInfoActivity.this);
//            storeImg.setTitle("保存图片到本地");
//            storeImg.setPositiveButton("保存", (dialog, which) -> {
//                ShareUtil.storeSyncFile(photoData, imgName);
//            });
//            storeImg.setNegativeButton("取消", (dialog, which) -> Toast.makeText(ImageInfoActivity.this, "已取消", Toast.LENGTH_SHORT).show());
//            storeImg.show();
//            return true;
//        });

    }
}
