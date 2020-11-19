package com.sjtuopennetwork.shareit.util;


import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.sjtuopennetwork.shareit.share.util.TMsg;

import org.greenrobot.eventbus.EventBus;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.MulticastFile;

import com.sjtuopennetwork.shareit.share.util.MsgType;
import com.sjtuopennetwork.shareit.share.util.TRecord;

public class MulticastHandlerImpl implements Handlers.MultiFileHandler {
    private static final String TAG = "===MulticastHandlerImpl";

    private Context ctx;
    private String loginAccount;

    public MulticastHandlerImpl(Context ctx, String loginAccount) {
        this.ctx = ctx;
        this.loginAccount = loginAccount;
    }

    @Override
    public void onGetMulticastFile(MulticastFile multicastFile) {
        TMsg tMsg = null;
        Log.d(TAG, "onGetMulticastFile: get msg type: " + multicastFile.getType());
        switch (multicastFile.getType()) {
            case TEXT:
                tMsg = DBHelper.getInstance(ctx, loginAccount).insertMsg(
                        multicastFile.getThreadId(), MsgType.MSG_MULTI_TEXT,
                        String.valueOf(System.currentTimeMillis()),
                        multicastFile.getSenderAddress(),
                        multicastFile.getFilePath(), //存放文字的内容
                        multicastFile.getSendTime(),
                        0);
                break;
            case IMG:
                tMsg = DBHelper.getInstance(ctx, loginAccount).insertMsg(
                        multicastFile.getThreadId(), MsgType.MSG_MULTI_PICTURE,
                        String.valueOf(System.currentTimeMillis()),
                        multicastFile.getSenderAddress(),
                        multicastFile.getFilePath(), //存放图片的 filePath,useTime,fileSize
                        multicastFile.getSendTime(),
                        0);
                break;
            case FILE:
                tMsg = DBHelper.getInstance(ctx, loginAccount).insertMsg(
                        multicastFile.getThreadId(),
                        MsgType.MSG_MULTI_FILE,
                        multicastFile.getFileId(),
                        multicastFile.getSenderAddress(),
                        multicastFile.getFilePath(), // body用json存放,filePath,useTime,fleSize
                        multicastFile.getSendTime(),
                        0);
                break;
            case VIDEO:
                tMsg = DBHelper.getInstance(ctx, loginAccount).insertMsg(
                        multicastFile.getThreadId(), MsgType.MSG_MULTI_VIDEO,
                        String.valueOf(System.currentTimeMillis()),
                        multicastFile.getSenderAddress(),
                        multicastFile.getFilePath() + "##" + multicastFile.getFileId(),
                        multicastFile.getSendTime(),
                        0);
                break;
            case STAT:
                //收到文件反馈信息，存到数据库
                long nowTime=System.currentTimeMillis();
                TRecord tRecord=new TRecord(multicastFile.getFileId(),multicastFile.getSenderAddress(),multicastFile.getSendTime(),0,nowTime,1,"");
                DBHelper.getInstance(ctx, loginAccount).recordGet(multicastFile.getFileId(),multicastFile.getSenderAddress(),multicastFile.getSendTime(),0,nowTime,"");
                EventBus.getDefault().post(tRecord);
                return;
        }

        EventBus.getDefault().post(tMsg);
    }

    @Override
    public void onReceivingMulticastFile(MulticastFile multicastFile) {
        TMsg tMsg = null;
        Log.d(TAG, "onGetMulticastFile: get msg type: " + multicastFile.getType());
        switch (multicastFile.getType()) {
            case IMG:
                tMsg = new TMsg(
                        multicastFile.getFileId(),
                        multicastFile.getThreadId(),
                        MsgType.MSG_MULTI_PICTURE_START,
                        multicastFile.getSenderAddress(),
                        multicastFile.getFilePath(),
                        multicastFile.getSendTime(),false);
                break;
            case FILE:
                tMsg = new TMsg(
                        multicastFile.getFileId(),
                        multicastFile.getThreadId(),
                        MsgType.MSG_MULTI_FILE_START,
                        multicastFile.getSenderAddress(),
                        multicastFile.getFilePath(),
                        multicastFile.getSendTime(),false);
                break;
        }
        EventBus.getDefault().post(tMsg);
    }
}
