package com.sjtuopennetwork.shareit.setting.util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjtuopennetwork.shareit.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class NotificationAdapter extends BaseExpandableListAdapter {

    private ArrayList<NotificationGroup> gData;
    private ArrayList<ArrayList<NotificationItem>> iData;
    private Context mContext;

    public NotificationAdapter(ArrayList<NotificationGroup> gData,ArrayList<ArrayList<NotificationItem>> iData, Context mContext) {
        this.gData = gData;
        this.iData = iData;
        this.mContext = mContext;
    }

    @Override
    public int getGroupCount() {
        return gData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return iData.get(groupPosition).size();
    }

    @Override
    public NotificationGroup getGroup(int groupPosition) {
        return gData.get(groupPosition);
    }

    @Override
    public NotificationItem getChild(int groupPosition, int childPosition) {
        return iData.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    //取得用于显示给定分组的视图. 这个方法仅返回分组的视图对象
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        ViewHolderGroup groupHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.
                            layout.item_notification_group, parent, false);
            groupHolder = new ViewHolderGroup();
            groupHolder.tv_group_name = (TextView) convertView.findViewById(R.id.tv_group_name);
            convertView.setTag(groupHolder);
        }else{
            groupHolder = (ViewHolderGroup) convertView.getTag();
        }
        groupHolder.tv_group_name.setText(gData.get(groupPosition).getgName());
        return convertView;
    }

    //取得显示给定分组给定子位置的数据用的视图
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolderItem itemHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_notification_item, parent, false);
            itemHolder = new ViewHolderItem();
            itemHolder.noti_time=(TextView) convertView.findViewById(R.id.noti_time);
            itemHolder.img_icon = (ImageView) convertView.findViewById(R.id.img_icon);
            itemHolder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
            convertView.setTag(itemHolder);
        }else{
            itemHolder = (ViewHolderItem) convertView.getTag();
        }
        DateFormat df=new SimpleDateFormat("MM-dd HH:mm:ss");
        itemHolder.noti_time.setText(df.format(iData.get(groupPosition).get(childPosition).sendTime*1000));
        itemHolder.img_icon.setImageBitmap(BitmapFactory.decodeFile(iData.get(groupPosition).get(childPosition).avatarPath));
        itemHolder.tv_content.setText(iData.get(groupPosition).get(childPosition).actor+" "+iData.get(groupPosition).get(childPosition).body);
        return convertView;
    }

    //设置子列表是否可选中
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    private static class ViewHolderGroup{
        private TextView tv_group_name;
    }

    private static class ViewHolderItem{
        private ImageView img_icon;
        private TextView tv_content;
        private TextView noti_time;
    }

}