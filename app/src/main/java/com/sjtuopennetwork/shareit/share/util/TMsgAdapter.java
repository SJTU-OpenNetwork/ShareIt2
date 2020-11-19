package com.sjtuopennetwork.shareit.share.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.share.FileTransActivity;
import com.sjtuopennetwork.shareit.share.ImageInfoActivity;
import com.sjtuopennetwork.shareit.share.PlayVideoActivity;
import com.sjtuopennetwork.shareit.share.multichat.MulticastVideoPlayActivity;
import com.sjtuopennetwork.shareit.util.RoundImageView;
import com.sjtuopennetwork.shareit.util.ShareUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.util.FileUtil;

import com.sjtuopennetwork.shareit.share.util.MsgType;

public class TMsgAdapter extends BaseAdapter {

    private static final String TAG = "====TMsgAdapter";

    private List<TMsg> msgList;
    DateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss");
    Context context;
    String threadid;

    public TMsgAdapter(Context context, List<TMsg> msgList, String threadid) {
        this.msgList = msgList;
        this.context = context;
        this.threadid = threadid;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        switch (getItemViewType(i)) {
            case MsgType.MSG_TEXT: //是文本
                return handleTextView(i, view, viewGroup);
            case MsgType.MSG_PICTURE: //普通图片
                return handlePhotoView(i, view, viewGroup);
            case MsgType.MSG_STREAM_VIDEO: //stream视频
                return handleStreamVideoView(i, view, viewGroup);
            case MsgType.MSG_FILE:
                return handleFileView(i, view, viewGroup, 0);
            case MsgType.MSG_TICKET_VIDEO: //ticket视频
                return handleTicketVideoView(i, view, viewGroup);
            case MsgType.MSG_SIMPLE_FILE: // simple file
                return handleFileView(i, view, viewGroup, 1);
            case MsgType.MSG_SIMPLE_PICTURE: // simple picture
                return handleSimplePictureView(i, view, viewGroup);
            case MsgType.MSG_STREAM_PICTURE:
                return handleStreamPictureView(i, view, viewGroup);
            case MsgType.MSG_STREAM_FILE:
                return handleStreamFileView(i, view, viewGroup);
            case MsgType.MSG_MULTI_FILE:
                return handleMultiFileView(i, view, viewGroup);
            case MsgType.MSG_MULTI_TEXT: //广播文字
                return handleMultiTextView(i, view, viewGroup);
            case MsgType.MSG_MULTI_PICTURE: //广播图片
                return handleMultiPhotoView(i, view, viewGroup);
            case MsgType.MSG_MULTI_VIDEO:
                return handleMultiVideoView(i, view, viewGroup);
            case MsgType.MSG_MULTI_FILE_START:
                return handleMultiFileStartView(i, view, viewGroup);
            case MsgType.MSG_FILE_START:
                return handleFileStartView(i,view,viewGroup);
            default:
                return null;
        }
    }


    private View handleMultiTextView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_msg_text, viewGroup, false);
            view.setTag(new TextVH(view));
        }
        if (view.getTag() instanceof TextVH) {
            TextVH h = (TextVH) view.getTag();
            String username = "";
            String useravatar = "";
            if (msgList.get(i).ismine) {
                username = msgList.get(i).author;
                useravatar = msgList.get(i).blockid;
                h.send_text_right.setVisibility(View.VISIBLE); //右边的显示
                h.send_text_left.setVisibility(View.GONE); //左边的隐藏
                h.msg_name_r.setText(username);
                h.msg_time_r.setText(df.format(msgList.get(i).sendtime * 1000));
                h.chat_words_r.setText(msgList.get(i).body);
//                ShareUtil.setImageView(context, h.msg_avatar_r, useravatar, 3);
            } else {
//                String addr = msgList.get(i).author;
//                Log.d(TAG, "handleTextView: addr:");
                username = msgList.get(i).author;
//                useravatar = ShareUtil.getOtherAvatar(addr);
                h.send_text_left.setVisibility(View.VISIBLE); //左边的显示
                h.send_text_right.setVisibility(View.GONE); //右边的隐藏
                h.chat_words.setText(msgList.get(i).body);
                h.msg_name.setText(username);
                h.msg_time.setText(df.format(msgList.get(i).sendtime * 1000));
//                ShareUtil.setImageView(context, h.msg_avatar, useravatar, 0);
            }
        }
        return view;
    }

    private View handleMultiPhotoView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_msg_img, viewGroup, false);
            view.setTag(new PhotoVH(view));
        }
        if (view.getTag() instanceof PhotoVH) {
            PhotoVH h = (PhotoVH) view.getTag();
            String username = "";
            String useravatar = "";
//            String[] hashName = msgList.get(i).body.split("##");
            if (msgList.get(i).ismine) {
                username = msgList.get(i).author;
                useravatar = msgList.get(i).blockid;
                h.send_photo_right.setVisibility(View.VISIBLE); //右边的显示
                h.send_photo_left.setVisibility(View.GONE); //左边的隐藏
                h.photo_name_r.setText(username);
                h.photo_time_r.setText(df.format(msgList.get(i).sendtime * 1000));
                h.video_icon_r.setVisibility(View.GONE);

                JSONObject jsonObject=JSONObject.parseObject(msgList.get(i).body);
                ShareUtil.setImageView(context, h.chat_photo_r, jsonObject.getString("filePath"), ShareUtil.ImgType.LOCAL);
                h.chat_photo_r.setOnClickListener(v -> {
//                    Intent it1 = new Intent(context, ImageInfoActivity.class);
//                    it1.putExtra("multiPath", msgList.get(i).body);
//                    it1.putExtra("isMulti", true);
//                    context.startActivity(it1);
                    Intent itToFileTrans = new Intent(context, FileTransActivity.class);
                    itToFileTrans.putExtra("fileCid", jsonObject.getString("fileCid"));
                    itToFileTrans.putExtra("fileSize", jsonObject.getLong("fileSize"));
                    itToFileTrans.putExtra("statType",2);
                    context.startActivity(itToFileTrans);
                });
            } else {
                String addr = msgList.get(i).author;
                username = msgList.get(i).author;
                h.send_photo_left.setVisibility(View.VISIBLE); //左边的显示
                h.send_photo_right.setVisibility(View.GONE); //右边的隐藏
                h.photo_name.setText(username);
                h.photo_time.setText(df.format(msgList.get(i).sendtime * 1000));
//                ShareUtil.setImageView(context, h.photo_avatar, useravatar, 0);
                h.video_icon.setVisibility(View.GONE);

                JSONObject jsonObject=JSONObject.parseObject(msgList.get(i).body);
                long get_time=jsonObject.getLong("useTime");
                int fileSize=jsonObject.getInteger("fileSize");
                String filePath=jsonObject.getString("filePath");
                h.img_get_time.setText("大小:"+fileSize+"B, 耗时:"+get_time+"ms");
                ShareUtil.setImageView(context, h.chat_photo, filePath, ShareUtil.ImgType.LOCAL);
                h.chat_photo.setOnClickListener(v -> {
                    Intent it1 = new Intent(context, ImageInfoActivity.class);
                    it1.putExtra("multiPath", filePath);
                    it1.putExtra("isMulti", true);
                    context.startActivity(it1);
                });
            }
        }
        return view;
    }

    private View handleMultiFileStartView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_msg_file_start, viewGroup, false);
            view.setTag(new FileVH(view));
        }
        if (view.getTag() instanceof FileVH) {
            FileVH h = (FileVH) view.getTag();
            String username = "";

            username = msgList.get(i).author;
            h.send_file_left.setVisibility(View.VISIBLE); //左边的显示
            h.file_user.setText(username);
            h.file_time.setText(df.format(msgList.get(i).sendtime * 1000));
            h.file_name.setText(msgList.get(i).body);
//            h.file_get_time.setVisibility(View.GONE);
            h.send_file_left.setOnClickListener(v -> {

            });
        }
        return view;
    }

    private View handleFileStartView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_msg_file_start, viewGroup, false);
            view.setTag(new FileVH(view));
        }
        if (view.getTag() instanceof FileVH) {
            FileVH h = (FileVH) view.getTag();
            String username = "";

            username = msgList.get(i).author;
            h.send_file_left.setVisibility(View.VISIBLE); //左边的显示
            h.file_user.setText(username);
            h.file_time.setText(df.format(msgList.get(i).sendtime * 1000));
            h.file_name.setText(msgList.get(i).body);
//            h.file_get_time.setVisibility(View.GONE);
            h.send_file_left.setOnClickListener(v -> {

            });
        }
        return view;
    }

    private View handleMultiFileView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_msg_file, viewGroup, false);
            view.setTag(new FileVH(view));
        }
        if (view.getTag() instanceof FileVH) {
            FileVH h = (FileVH) view.getTag();
            String username = "";
            String useravatar = "";
//            Log.d(TAG, "handleFileView: " + hashName[0] + " " + hashName[1]);
            if (msgList.get(i).ismine) {
                username = msgList.get(i).author;
                useravatar = msgList.get(i).blockid;
                h.send_file_right.setVisibility(View.VISIBLE); //右边的显示
                h.send_file_left.setVisibility(View.GONE); //左边的隐藏
                h.file_user_r.setText(username);
                h.file_time_r.setText(df.format(msgList.get(i).sendtime * 1000));
                Log.d(TAG, "handleMultiFileView: "+msgList.get(i).body);

                JSONObject jsonObject=JSONObject.parseObject(msgList.get(i).body);
                h.file_name_r.setText(FileUtil.getFileNameFromPath(jsonObject.getString("filePath")));
//                ShareUtil.setImageView(context, h.file_avatar_r, useravatar, 3);
//                if (fileType == 0) { // 原生文件
                h.send_file_right.setOnClickListener(view1 -> {
                    Log.d(TAG, "handleMultiFileView: "+msgList.get(i).body);
                    Intent itToFileTrans = new Intent(context, FileTransActivity.class);
                    itToFileTrans.putExtra("fileCid", jsonObject.getString("fileCid"));
                    itToFileTrans.putExtra("fileSize", jsonObject.getLong("fileSize"));
                    itToFileTrans.putExtra("statType",2);
                    context.startActivity(itToFileTrans);
                });
            } else {
                username = msgList.get(i).author;
                h.send_file_left.setVisibility(View.VISIBLE); //左边的显示
                h.send_file_right.setVisibility(View.GONE); //右边的隐藏
                h.file_user.setText(username);

                JSONObject jsonObject=JSONObject.parseObject(msgList.get(i).body);
                long get_time=jsonObject.getLong("useTime");
                int fileSize=jsonObject.getInteger("fileSize");
                String filePath=jsonObject.getString("filePath");
                h.file_get_time.setText("大小:"+fileSize+"B, 耗时:"+(get_time)+"ms");
//                h.file_get_time.setVisibility(View.GONE);
                h.file_time.setText(df.format(msgList.get(i).sendtime * 1000));
                h.file_name.setText(FileUtil.getFileNameFromPath(filePath));
//                h.file_name.setText(msgList.get(i).body);
//                ShareUtil.setImageView(context, h.file_avatar, useravatar, 0);
                h.send_file_left.setOnClickListener(v -> {
                    FileUtil.openFile(context, filePath);
                });
            }
        }
        return view;
    }

    private View handleTextView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_msg_text, viewGroup, false);
            view.setTag(new TextVH(view));
        }
        if (view.getTag() instanceof TextVH) {
            TextVH h = (TextVH) view.getTag();
            String username = "";
            String useravatar = "";
            if (msgList.get(i).ismine) {
                Log.d(TAG, "handleTextView: 得到自己的消息："+msgList.get(i).body);
                username = ShareUtil.getMyName();
                useravatar = ShareUtil.getMyAvatar();
                h.send_text_right.setVisibility(View.VISIBLE); //右边的显示
                h.send_text_left.setVisibility(View.GONE); //左边的隐藏
                h.msg_name_r.setText(username);
                h.msg_time_r.setText(df.format(msgList.get(i).sendtime * 1000));
                h.chat_words_r.setText(msgList.get(i).body);
                ShareUtil.setImageView(context, h.msg_avatar_r, useravatar, ShareUtil.ImgType.AVATAR);
            } else {
                String addr = msgList.get(i).author;
                Log.d(TAG, "handleTextView: 收到别人的消息："+msgList.get(i).body);
                username = ShareUtil.getOtherName(addr);
                useravatar = ShareUtil.getOtherAvatar(addr);
                h.send_text_left.setVisibility(View.VISIBLE); //左边的显示
                h.send_text_right.setVisibility(View.GONE); //右边的隐藏
                h.chat_words.setText(msgList.get(i).body);
                h.msg_name.setText(username);
                h.msg_time.setText(df.format(msgList.get(i).sendtime * 1000));
                ShareUtil.setImageView(context, h.msg_avatar, useravatar, ShareUtil.ImgType.AVATAR);
            }
        }
        return view;
    }

    private View handleStreamVideoView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_msg_img, viewGroup, false);
            view.setTag(new PhotoVH(view));
        }
        if (view.getTag() instanceof PhotoVH) {
            PhotoVH h = (PhotoVH) view.getTag();
            String username = "";
            String useravatar = "";
            if (msgList.get(i).ismine) {
                username = ShareUtil.getMyName();
                useravatar = ShareUtil.getMyAvatar();
                h.send_photo_right.setVisibility(View.VISIBLE); //右边的显示
                h.send_photo_left.setVisibility(View.GONE); //左边的隐藏
                h.photo_name_r.setText(username);
                h.photo_time_r.setText(df.format(msgList.get(i).sendtime * 1000));
                ShareUtil.setImageView(context, h.photo_avatar_r, useravatar, ShareUtil.ImgType.AVATAR);

                String[] posterAndFile_r = msgList.get(i).body.split("##"); //0是poster路径，1是Id，2是视频路径
                Glide.with(context).load(posterAndFile_r[0]).thumbnail(0.3f).into(h.chat_photo_r);
                h.chat_photo_r.setOnClickListener(view1 -> {
                    Intent it12 = new Intent(context, PlayVideoActivity.class);
                    it12.putExtra("ismine", true);
                    it12.putExtra("videopath", posterAndFile_r[1]);
                    context.startActivity(it12);
                });
            } else { //收到stream视频
                String addr = msgList.get(i).author;
                username = ShareUtil.getOtherName(addr);
                useravatar = ShareUtil.getOtherAvatar(addr);
                h.send_photo_left.setVisibility(View.VISIBLE); //左边的显示
                h.send_photo_right.setVisibility(View.GONE); //右边的隐藏
                h.photo_name.setText(username);
                h.photo_time.setText(df.format(msgList.get(i).sendtime * 1000));
                ShareUtil.setImageView(context, h.photo_avatar, useravatar, ShareUtil.ImgType.AVATAR);
                h.img_get_time.setVisibility(View.GONE);

                String[] posterId_streamId = msgList.get(i).body.split("##");
//                    Log.d(TAG, "handlePhotoView: stream video poster: " + posterId_streamId[0]);
                ShareUtil.setImageView(context, h.chat_photo, posterId_streamId[0], ShareUtil.ImgType.IPFS);
                h.chat_photo.setOnClickListener(view1 -> {
                    Intent it11 = new Intent(context, PlayVideoActivity.class);
                    it11.putExtra("videoid", posterId_streamId[1]);
                    it11.putExtra("ismine", false);
                    context.startActivity(it11);
                });
            }
        }
        return view;
    }

    private View handleTicketVideoView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_msg_img, viewGroup, false);
            view.setTag(new PhotoVH(view));
        }
        if (view.getTag() instanceof PhotoVH) {
            PhotoVH h = (PhotoVH) view.getTag();
            String username = "";
            String useravatar = "";
            if (msgList.get(i).ismine) {
                username = ShareUtil.getMyName();
                useravatar = ShareUtil.getMyAvatar();
                h.send_photo_right.setVisibility(View.VISIBLE); //右边的显示
                h.send_photo_left.setVisibility(View.GONE); //左边的隐藏
                h.photo_name_r.setText(username);
                h.photo_time_r.setText(df.format(msgList.get(i).sendtime * 1000));
                ShareUtil.setImageView(context, h.photo_avatar_r, useravatar, ShareUtil.ImgType.AVATAR);

                String[] posterAndFile_r = msgList.get(i).body.split("##"); //0是poster路径，1是Id，2是视频路径
                Glide.with(context).load(posterAndFile_r[0]).thumbnail(0.3f).into(h.chat_photo_r);
                h.chat_photo_r.setOnClickListener(view1 -> {
                    Intent it12 = new Intent(context, PlayVideoActivity.class);
                    it12.putExtra("ismine", true);
                    it12.putExtra("videopath", posterAndFile_r[1]);
                    context.startActivity(it12);
                });
            }else{
                String addr = msgList.get(i).author;
                username = ShareUtil.getOtherName(addr);
                useravatar = ShareUtil.getOtherAvatar(addr);
                h.send_photo_left.setVisibility(View.VISIBLE); //左边的显示
                h.send_photo_right.setVisibility(View.GONE); //右边的隐藏
                h.photo_name.setText(username);
                h.photo_time.setText(df.format(msgList.get(i).sendtime * 1000));
                h.img_get_time.setVisibility(View.GONE);
                ShareUtil.setImageView(context, h.photo_avatar, useravatar, ShareUtil.ImgType.AVATAR);

                String[] poster_videoid = msgList.get(i).body.split("##");
                Log.d(TAG, "handlePhotoView: tkt video poster: " + poster_videoid[0]);
                ShareUtil.setImageView(context, h.chat_photo, poster_videoid[0], ShareUtil.ImgType.IPFS); //缩略图
                h.chat_photo.setOnClickListener(view1 -> {
                    Intent it11 = new Intent(context, PlayVideoActivity.class);
                    it11.putExtra("ticket", true);
                    it11.putExtra("videoid", poster_videoid[1]);
                    it11.putExtra("ismine", false);
                    context.startActivity(it11);
                });
            }
        }
        return view;
    }

    private View handlePhotoView(int i, View view, ViewGroup viewGroup) {
//        Log.d(TAG, "handlePhotoView: pic_hash: " + msgList.get(i).body);
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_msg_img, viewGroup, false);
            view.setTag(new PhotoVH(view));
        }
        if (view.getTag() instanceof PhotoVH) {
            PhotoVH h = (PhotoVH) view.getTag();
            String username = "";
            String useravatar = "";
            if (msgList.get(i).ismine) {
                username = ShareUtil.getMyName();
                useravatar = ShareUtil.getMyAvatar();
                h.send_photo_right.setVisibility(View.VISIBLE); //右边的显示
                h.send_photo_left.setVisibility(View.GONE); //左边的隐藏
                h.photo_name_r.setText(username);
                h.photo_time_r.setText(df.format(msgList.get(i).sendtime * 1000));
                ShareUtil.setImageView(context, h.photo_avatar_r, useravatar, ShareUtil.ImgType.AVATAR);
                JSONObject jsonObject = JSONObject.parseObject(msgList.get(i).body);
                h.video_icon_r.setVisibility(View.GONE);

                ShareUtil.setImageView(context, h.chat_photo_r, jsonObject.getString("fileHash"), ShareUtil.ImgType.IPFS);
                h.chat_photo_r.setOnClickListener(v -> {
                    Intent it1 = new Intent(context, ImageInfoActivity.class);
                    it1.putExtra("imghash", jsonObject.getString("fileHash"));
                    it1.putExtra("imgname", jsonObject.getString("fileName"));
                    it1.putExtra("isSimple", false);
                    context.startActivity(it1);
                });
            } else {
                String addr = msgList.get(i).author;
                username = ShareUtil.getOtherName(addr);
                useravatar = ShareUtil.getOtherAvatar(addr);
                h.send_photo_left.setVisibility(View.VISIBLE); //左边的显示
                h.send_photo_right.setVisibility(View.GONE); //右边的隐藏
                h.photo_name.setText(username);
                h.photo_time.setText(df.format(msgList.get(i).sendtime * 1000));
                ShareUtil.setImageView(context, h.photo_avatar, useravatar, ShareUtil.ImgType.AVATAR);
                JSONObject jsonObject = JSONObject.parseObject(msgList.get(i).body);
                h.video_icon.setVisibility(View.GONE);
                ShareUtil.setImageView(context, h.chat_photo, jsonObject.getString("fileHash"), ShareUtil.ImgType.IPFS);
                h.chat_photo.setOnClickListener(v -> {
//                    Intent it1 = new Intent(context, ImageInfoActivity.class);
//                    it1.putExtra("imghash", hashName[0]);
//                    it1.putExtra("imgname", hashName[1]);
//                    it1.putExtra("isSimple", false);
//                    context.startActivity(it1);
                });
            }
        }
        return view;
    }

    private View handleSimplePictureView(int i, View view, ViewGroup viewGroup) {
//        Log.d(TAG, "handlePhotoView: simple picture: " + msgList.get(i).body);
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_msg_img, viewGroup, false);
            view.setTag(new PhotoVH(view));
        }
        if (view.getTag() instanceof PhotoVH) {
            PhotoVH h = (PhotoVH) view.getTag();
            String username = "";
            String useravatar = "";
//            String[] hashName = msgList.get(i).body.split("##"); // hash name block
            JSONObject jsonObject = JSONObject.parseObject(msgList.get(i).body);
            if (msgList.get(i).ismine) {
                Log.d(TAG, "handleSimplePictureView: "+msgList.get(i).body);
                username = ShareUtil.getMyName();
                useravatar = ShareUtil.getMyAvatar();
                h.send_photo_right.setVisibility(View.VISIBLE); //右边的显示
                h.send_photo_left.setVisibility(View.GONE); //左边的隐藏
                h.photo_name_r.setText(username);
                h.photo_time_r.setText(df.format(msgList.get(i).sendtime * 1000));
                ShareUtil.setImageView(context, h.photo_avatar_r, useravatar, ShareUtil.ImgType.AVATAR);
                h.video_icon_r.setVisibility(View.GONE);
//                ShareUtil.setImageView(context, h.chat_photo_r, hashName[0], ShareUtil.ImgType.IPFS);
                ShareUtil.setImageView(context, h.chat_photo_r, jsonObject.getString("fileHash"), ShareUtil.ImgType.IPFS);
                h.chat_photo_r.setOnClickListener(v -> {
                    Intent itToFileTrans = new Intent(context, FileTransActivity.class);
                    itToFileTrans.putExtra("statType",0);
                    itToFileTrans.putExtra("fileCid", jsonObject.getString("block"));
                    itToFileTrans.putExtra("fileSize", jsonObject.getInteger("dataLength"));
                    context.startActivity(itToFileTrans);
                });
            } else {
                String addr = msgList.get(i).author;
                username = ShareUtil.getOtherName(addr);
                useravatar = ShareUtil.getOtherAvatar(addr);
                h.send_photo_left.setVisibility(View.VISIBLE); //左边的显示
                h.send_photo_right.setVisibility(View.GONE); //右边的隐藏
                h.photo_name.setText(username);
                h.photo_time.setText(df.format(msgList.get(i).sendtime * 1000));
                ShareUtil.setImageView(context, h.photo_avatar, useravatar, ShareUtil.ImgType.AVATAR);
                h.video_icon.setVisibility(View.GONE);
                ShareUtil.setImageView(context, h.chat_photo, jsonObject.get("fileHash").toString(), ShareUtil.ImgType.IPFS);

                h.img_get_time.setText("大小:"+jsonObject.getInteger("dataLength")+"B, 耗时:"+jsonObject.getInteger("duration")+"ms");

                h.chat_photo.setOnClickListener(v -> {
                    Intent it1 = new Intent(context, ImageInfoActivity.class);
                    it1.putExtra("imghash", jsonObject.get("fileHash").toString());
                    it1.putExtra("imgname", jsonObject.get("fileName").toString());
                    it1.putExtra("isSimple", true);
                    context.startActivity(it1);
                });
            }
        }
        return view;
    }

    private View handleStreamPictureView(int i, View view, ViewGroup viewGroup) {
//        Log.d(TAG, "handlePhotoView: simple picture: " + msgList.get(i).body);
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_msg_img, viewGroup, false);
            view.setTag(new PhotoVH(view));
        }
        if (view.getTag() instanceof PhotoVH) {
            PhotoVH h = (PhotoVH) view.getTag();
            String username = "";
            String useravatar = "";
            if (msgList.get(i).ismine) {
                String[] hashName = msgList.get(i).body.split("##");
                username = ShareUtil.getMyName();
                useravatar = ShareUtil.getMyAvatar();
                h.send_photo_right.setVisibility(View.VISIBLE); //右边的显示
                h.send_photo_left.setVisibility(View.GONE); //左边的隐藏
                h.photo_name_r.setText(username);
                h.photo_time_r.setText(df.format(msgList.get(i).sendtime * 1000));
                ShareUtil.setImageView(context, h.photo_avatar_r, useravatar, ShareUtil.ImgType.LOCAL);
                h.video_icon_r.setVisibility(View.GONE);
                Glide.with(context).load(hashName[1]).thumbnail(0.3f).into(h.chat_photo_r);
                h.chat_photo_r.setOnClickListener(v -> {
                    Intent itToFileTrans = new Intent(context, FileTransActivity.class);
                    itToFileTrans.putExtra("fileCid", hashName[0]);
                    itToFileTrans.putExtra("fileSizeCid", hashName[1]);
                    itToFileTrans.putExtra("statType",1);
                    itToFileTrans.putExtra("streamSendTime",Long.parseLong(hashName[2]));
                    context.startActivity(itToFileTrans);
                });
            } else {
                String addr = msgList.get(i).author;
                username = ShareUtil.getOtherName(addr);
                useravatar = ShareUtil.getOtherAvatar(addr);
                h.send_photo_left.setVisibility(View.VISIBLE); //左边的显示
                h.send_photo_right.setVisibility(View.GONE); //右边的隐藏
                h.photo_name.setText(username);
                h.photo_time.setText(df.format(msgList.get(i).sendtime * 1000));
                ShareUtil.setImageView(context, h.photo_avatar, useravatar, ShareUtil.ImgType.LOCAL);
                h.video_icon.setVisibility(View.GONE);
//                Log.d(TAG, "handleStreamPictureView: :"+msgList.get(i).body);

                JSONObject jsonObject=JSONObject.parseObject(msgList.get(i).body);
                String filePath=jsonObject.getString("filePath");
                int duration=jsonObject.getInteger("duration");
                int fileSize=jsonObject.getInteger("fileSize");
                h.img_get_time.setText("大小:"+fileSize+"B, 耗时:"+duration+"ms");

                Glide.with(context).load(filePath).thumbnail(0.3f).into(h.chat_photo);
                h.chat_photo.setOnClickListener(v -> {
//                    Intent it1 = new Intent(context, ImageInfoActivity.class);
//                    context.startActivity(it1);
                    Intent it1 = new Intent(context, ImageInfoActivity.class);
                    it1.putExtra("multiPath", filePath);
                    it1.putExtra("isMulti", true);
                    context.startActivity(it1);
                });
            }
        }
        return view;
    }

    private View handleStreamFileView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_msg_file, viewGroup, false);
            view.setTag(new FileVH(view));
        }

        if (view.getTag() instanceof FileVH) {
            FileVH h = (FileVH) view.getTag();
            String username = "";
            String useravatar = "";
            String[] hashPath = msgList.get(i).body.split("##");

//            Log.d(TAG, "handleFileView: " + hashPath[0] + " " + hashPath[1]);
            if (msgList.get(i).ismine) {
                username = ShareUtil.getMyName();
//                Log.d(TAG, "handleFileView: myname " + username);
                useravatar = ShareUtil.getMyAvatar();
                h.send_file_right.setVisibility(View.VISIBLE); //右边的显示
                h.send_file_left.setVisibility(View.GONE); //左边的隐藏
                h.file_user_r.setText(username);
                h.file_time_r.setText(df.format(msgList.get(i).sendtime * 1000));
                String fileName = ShareUtil.getFileNameWithSuffix(hashPath[1]);
                h.file_name_r.setText(fileName);
                ShareUtil.setImageView(context, h.file_avatar_r, useravatar, ShareUtil.ImgType.AVATAR);
//                if (fileType == 0) { // 原生文件
                h.send_file_right.setOnClickListener(view1 -> {
                    Intent itToFileTrans = new Intent(context, FileTransActivity.class);
                    itToFileTrans.putExtra("fileCid", hashPath[0]);
                    itToFileTrans.putExtra("fileSizeCid", hashPath[1]);
                    itToFileTrans.putExtra("statType",1);
                    itToFileTrans.putExtra("streamSendTime",Long.parseLong(hashPath[2]));
                    context.startActivity(itToFileTrans);
                });
//                } else if (fileType == 1) {
//                    Log.d(TAG, "handleFileView: 收到simple file");
//                }
            } else {
                String addr = msgList.get(i).author;
                username = ShareUtil.getOtherName(addr);
                useravatar = ShareUtil.getOtherAvatar(addr);
                h.send_file_left.setVisibility(View.VISIBLE); //左边的显示
                h.send_file_right.setVisibility(View.GONE); //右边的隐藏
                h.file_time.setText(df.format(msgList.get(i).sendtime * 1000));

//                if (hashPath.length == 1) { //显示灰色
//                    ShareUtil.setImageView(context, h.file_avatar, useravatar, ShareUtil.ImgType.AVATAR);
//                    h.file_name.setText("未下载");
//                    h.file_name.setTextColor(Color.RED);
//                } else { //正常
//                    ShareUtil.setImageView(context, h.file_avatar, useravatar, ShareUtil.ImgType.AVATAR);
//                    h.file_name.setText(hashPath[1]);
//                    h.file_name.setTextColor(Color.BLACK);
//                }

                JSONObject jsonObject=JSONObject.parseObject(msgList.get(i).body);
                String filePath=jsonObject.getString("streamId");
                int duration=jsonObject.getInteger("duration");
                int fileSize=jsonObject.getInteger("fileSize");
                String fileName=jsonObject.getString("fileName");
                h.file_get_time.setText("大小:"+fileSize+"B, 耗时:"+duration+"ms");
                h.file_name.setText(fileName);

                h.send_file_left.setOnClickListener(v -> {
//                    String stateInfo = Textile.instance().streams.getState(hashPath[0]);
//                    Toast.makeText(context, stateInfo, Toast.LENGTH_SHORT).show();

//                    if(hashPath.length==2){
                        String isExist=null;
                        isExist= ShareUtil.isFileExist(fileName);
                        if (isExist != null) {
                            Toast.makeText(context, "文件已下载：" + isExist, Toast.LENGTH_SHORT).show();
                            FileUtil.openFile(context,isExist);
                        } else {
//                            AlertDialog.Builder downFile = new AlertDialog.Builder(context);
//                            downFile.setTitle("下载文件");
//                            downFile.setMessage("确定下载文件吗？");
//                            Handler fileResponse = new Handler() {
//                                @Override
//                                public void handleMessage(Message msg) {
//                                    if (msg.what == 9) {
//                                        Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            };
//                            downFile.setNegativeButton("取消", (dialog, which) -> Toast.makeText(context, "已取消", Toast.LENGTH_SHORT).show());
//                            downFile.show();

                        }
//                    }
                });
            }
        }
        return view;
    }

    private View handleFileView(int i, View view, ViewGroup viewGroup, int fileType) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_msg_file, viewGroup, false);
            view.setTag(new FileVH(view));
        }

        if (view.getTag() instanceof FileVH) {
            FileVH h = (FileVH) view.getTag();
            String username = "";
            String useravatar = "";
            JSONObject jsonObject = JSONObject.parseObject(msgList.get(i).body);
            if (msgList.get(i).ismine) {
                username = ShareUtil.getMyName();
//                Log.d(TAG, "handleFileView: myname " + username);
                useravatar = ShareUtil.getMyAvatar();
                h.send_file_right.setVisibility(View.VISIBLE); //右边的显示
                h.send_file_left.setVisibility(View.GONE); //左边的隐藏
                h.file_user_r.setText(username);
                h.file_time_r.setText(df.format(msgList.get(i).sendtime * 1000));
                h.file_name_r.setText(jsonObject.getString("fileName"));
                ShareUtil.setImageView(context, h.file_avatar_r, useravatar, ShareUtil.ImgType.AVATAR);
                h.send_file_right.setOnClickListener(view1 -> {
                    Intent itToFileTrans = new Intent(context, FileTransActivity.class);
                    itToFileTrans.putExtra("fileCid", jsonObject.getString("block"));
                    itToFileTrans.putExtra("fileSize", jsonObject.getInteger("dataLength"));
                    itToFileTrans.putExtra("statType",0);
                    context.startActivity(itToFileTrans);
                });
            } else {
                String addr = msgList.get(i).author;
                username = ShareUtil.getOtherName(addr);
                useravatar = ShareUtil.getOtherAvatar(addr);
                h.send_file_left.setVisibility(View.VISIBLE); //左边的显示
                h.send_file_right.setVisibility(View.GONE); //右边的隐藏
                h.file_user.setText(username);
                h.file_time.setText(df.format(msgList.get(i).sendtime * 1000));
                h.file_name.setText(jsonObject.getString("fileName"));

                h.file_get_time.setText("大小:"+jsonObject.getInteger("dataLength")+"B, 耗时:"+jsonObject.getInteger("duration")+"ms");

                ShareUtil.setImageView(context, h.file_avatar, useravatar, ShareUtil.ImgType.AVATAR);
                h.send_file_left.setOnClickListener(v -> {
                    String isExist = null;
                    isExist = ShareUtil.isFileExist(jsonObject.getString("fileName"));
                    if (isExist != null) {
                        Toast.makeText(context, "文件已下载：" + isExist, Toast.LENGTH_SHORT).show();
                        FileUtil.openFile(context,isExist);
                    } else {
//                        AlertDialog.Builder downFile = new AlertDialog.Builder(context);
//                        downFile.setTitle("下载文件");
//                        downFile.setMessage("确定下载文件吗？");
//                        Handler fileResponse = new Handler() {
//                            @Override
//                            public void handleMessage(Message msg) {
//                                if (msg.what == 9) {
//                                    Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        };
//                        if (fileType == 0) {
//                            downFile.setPositiveButton("下载", (dialog, which) -> {
//                                Textile.instance().files.content(jsonObject.getString("fileHash"), new Handlers.DataHandler() {
//                                    @Override
//                                    public void onComplete(byte[] data, String media) {
//                                        String res = ShareUtil.storeSyncFile(data, jsonObject.getString("fileName"));
//                                        String resMeg = "下载成功 " + res;
//                                        Message msg = fileResponse.obtainMessage();
//                                        msg.what = 9;
//                                        msg.obj = resMeg;
//                                        fileResponse.sendMessage(msg);
//                                    }
//
//                                    @Override
//                                    public void onError(Exception e) {
//                                    }
//                                });
//                            });
//                        } else if (fileType == 1) { //按照ipfsgetdata来取文件
//                            downFile.setPositiveButton("下载", (dialog, which) -> {
//                                Textile.instance().ipfs.dataAtPath(jsonObject.getString("fileHash"), new Handlers.DataHandler() {
//                                    @Override
//                                    public void onComplete(byte[] data, String media) {
//                                        String res = ShareUtil.storeSyncFile(data, );
//                                        String resMeg = "下载成功 " + res;
////                                        Log.d(TAG, "onComplete: simple file: "+resMeg);
//                                        Message msg = fileResponse.obtainMessage();
//                                        msg.what = 9;
//                                        msg.obj = resMeg;
//                                        fileResponse.sendMessage(msg);
//                                    }
//
//                                    @Override
//                                    public void onError(Exception e) {
//
//                                    }
//                                });
//                            });
//                        }
//                        downFile.setNegativeButton("取消", (dialog, which) -> Toast.makeText(context, "已取消", Toast.LENGTH_SHORT).show());
//                        downFile.show();
                    }
                });
            }
        }
        return view;
    }

    private View handleMultiVideoView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_msg_img, viewGroup, false);
            view.setTag(new PhotoVH(view));
        }
        if (view.getTag() instanceof PhotoVH) {
            String[] posterVideo = msgList.get(i).body.split("##");
            PhotoVH h = (PhotoVH) view.getTag();
            String username = msgList.get(i).author;
            h.send_photo_left.setVisibility(View.VISIBLE); //左边的显示
            h.send_photo_right.setVisibility(View.GONE); //右边的隐藏
            h.photo_name.setText(username);
            h.photo_time.setText(df.format(msgList.get(i).sendtime * 1000));
            h.img_get_time.setVisibility(View.GONE);
            ShareUtil.setImageView(context, h.chat_photo, posterVideo[0], ShareUtil.ImgType.LOCAL);
            h.chat_photo.setOnClickListener(v -> {
                Log.d(TAG, "updateChat: posetrVideo: " + posterVideo[0] + " " + posterVideo[1]);

                Intent it=new Intent(context, MulticastVideoPlayActivity.class);
                it.putExtra("videoId",posterVideo[1]);
                context.startActivity(it);
            });
        }

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return msgList.get(position).msgType;
    }

    @Override
    public int getCount() {
        return msgList.size();
    }

    @Override
    public int getViewTypeCount() {
        return 15;
    }

    @Override
    public Object getItem(int position) {
        return msgList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
//    @Override
//    public boolean isEnabled(int position) {
//        return false;
//    }

    public static class TextVH {
        public TextView msg_name, msg_time, chat_words;
        public TextView msg_name_r, msg_time_r, chat_words_r;
        public RoundImageView msg_avatar, msg_avatar_r;
        public LinearLayout send_text_left, send_text_right;

        public TextVH(View v) {
            msg_name = v.findViewById(R.id.msg_name);
            msg_time = v.findViewById(R.id.msg_time);
            chat_words = v.findViewById(R.id.chat_words);
            msg_avatar = v.findViewById(R.id.msg_avatar);
            msg_name_r = v.findViewById(R.id.msg_name_r);
            msg_time_r = v.findViewById(R.id.msg_time_r);
            chat_words_r = v.findViewById(R.id.chat_words_r);
            msg_avatar_r = v.findViewById(R.id.msg_avatar_r);
            send_text_left = v.findViewById(R.id.send_msg_left);
            send_text_right = v.findViewById(R.id.send_msg_right);
        }
    }

    public static class PhotoVH {
        public TextView photo_name, photo_time;
        public TextView photo_name_r, photo_time_r;
        public RoundImageView photo_avatar, photo_avatar_r;
        public ImageView chat_photo, chat_photo_r, video_icon, video_icon_r;
        public LinearLayout send_photo_left, send_photo_right;

        public TextView img_get_time;

        public PhotoVH(View v) {
            photo_name = v.findViewById(R.id.photo_name);
            photo_time = v.findViewById(R.id.photo_time);
            photo_avatar = v.findViewById(R.id.photo_avatar);
            chat_photo = v.findViewById(R.id.chat_photo);
            photo_name_r = v.findViewById(R.id.photo_name_r);
            photo_time_r = v.findViewById(R.id.photo_time_r);
            photo_avatar_r = v.findViewById(R.id.photo_avatar_r);
            chat_photo_r = v.findViewById(R.id.chat_photo_r);
            send_photo_left = v.findViewById(R.id.send_photo_left);
            send_photo_right = v.findViewById(R.id.send_photo_right);
            video_icon = v.findViewById(R.id.video_icon);
            video_icon_r = v.findViewById(R.id.video_icon_r);

            img_get_time = v.findViewById(R.id.img_get_time);
        }
    }

    public static class FileVH {
        public TextView file_user, file_time, file_name;
        public TextView file_user_r, file_time_r, file_name_r;
        public RoundImageView file_avatar, file_avatar_r;
        public LinearLayout send_file_left, send_file_right;

        public TextView file_get_time;

        public FileVH(View v) {
            file_user = v.findViewById(R.id.send_file_user);
            file_time = v.findViewById(R.id.send_file_time);
            file_name = v.findViewById(R.id.send_file_name);
            file_avatar = v.findViewById(R.id.send_file_avatar);
            file_user_r = v.findViewById(R.id.send_file_user_r);
            file_time_r = v.findViewById(R.id.send_file_time_r);
            file_name_r = v.findViewById(R.id.send_file_name_r);
            file_avatar_r = v.findViewById(R.id.send_file_avatar_r);
            send_file_left = v.findViewById(R.id.send_file_left);
            send_file_right = v.findViewById(R.id.send_file_right);

            file_get_time = v.findViewById(R.id.file_get_time);
        }
    }
}
