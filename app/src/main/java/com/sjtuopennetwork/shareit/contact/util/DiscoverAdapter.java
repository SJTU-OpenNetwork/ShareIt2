package com.sjtuopennetwork.shareit.contact.util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.util.ShareUtil;
import com.sjtuopennetwork.shareit.util.RoundImageView;

import java.util.ArrayList;
import java.util.List;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;

public class DiscoverAdapter extends BaseAdapter {

    private static final String TAG = "======================";

    List<ResultContact> datas;
    private LayoutInflater mInflater;
    private MyClickListener mListener;
    byte[] img;
    public List<Boolean> mChecked;
    Context context;

    public DiscoverAdapter(Context context,List<ResultContact> datas, MyClickListener mListener) {
        this.datas = datas;
        this.context=context;
        this.mInflater = LayoutInflater.from(context);
        this.mListener = mListener;

        mChecked = new ArrayList<>();
    }

    public void selectAll(boolean all){
        if(all){
            for(int i=0;i<mChecked.size();i++){
                mChecked.set(i,true);
            }
        }else{
            for(int i=0;i<mChecked.size();i++){
                mChecked.set(i,false);
            }
        }
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int i) {
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder vh;
        if(view==null){
            view=mInflater.inflate(R.layout.item_contact_discovery,null);
            vh=new ViewHolder(view);
            view.setTag(vh);
        }else{
            vh = (ViewHolder) view.getTag();
        }

        vh.name.setText(datas.get(position).name);
        vh.addr.setText(datas.get(position).address.substring(0,10)+"...");
        if(datas.get(position).avatarhash.equals("")){ //如果没有设置头像
            vh.avatar.setImageResource(R.drawable.ic_people);
        }else{
            img=datas.get(position).avatar;
            if(img==null){
                ShareUtil.setImageView(context,vh.avatar,datas.get(position).avatarhash, ShareUtil.ImgType.AVATAR);
            }else{
                vh.avatar.setImageBitmap(BitmapFactory.decodeByteArray(img,0,img.length));
            }
        }

        //添加好友按钮
        if(datas.get(position).isMyFriend){ //如果是我的好友，就不显示
            vh.addFriend.setVisibility(View.GONE);
        }else{ //不是好友就设置按钮监听器
            vh.addFriend.setTag(position);
            vh.addFriend.setOnClickListener(mListener);
        }

        //复选框
        vh.createGp.setChecked(mChecked.get(position));
        vh.createGp.setOnClickListener(view1 -> mChecked.set(position,((CheckBox)view1).isChecked()));

        return view;
    }


    class ViewHolder{
        public RoundImageView avatar;
        public TextView name;
        public TextView addr;
        public Button addFriend;
        public CheckBox createGp;

        public ViewHolder(View v){
            avatar=v.findViewById(R.id.discover_avatar);
            name=v.findViewById(R.id.discover_name);
            addr=v.findViewById(R.id.discover_address);
            addFriend=v.findViewById(R.id.bt_add_discover_friend);
            createGp=v.findViewById(R.id.discover_create_group);
        }
    }

    public static abstract class MyClickListener implements View.OnClickListener {

        public abstract void myOnClick(int position, View v); //自定义抽象方法

        @Override
        public void onClick(View view) {
            Integer integer=(Integer) view.getTag();
            myOnClick(integer, view);
        }
    }

}
