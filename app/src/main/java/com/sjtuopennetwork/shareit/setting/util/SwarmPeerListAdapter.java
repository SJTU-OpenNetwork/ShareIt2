package com.sjtuopennetwork.shareit.setting.util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjtuopennetwork.shareit.R;

import java.util.ArrayList;

public class SwarmPeerListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "==================";

    private ArrayList<NotificationGroup> groups;
    private ArrayList<ArrayList<NotificationItem>> childs;

    private Context context;

    public SwarmPeerListAdapter(Context context,ArrayList<NotificationGroup> groups, ArrayList<ArrayList<NotificationItem>> childs) {
        this.context = context;
        this.groups = groups;
        this.childs = childs;
    }


    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childs.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childs.get(groupPosition).get(childPosition);
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

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolderGroup groupHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.
                    layout.item_notification_group, parent, false);
            groupHolder = new ViewHolderGroup();
            groupHolder.tv_group_name = (TextView) convertView.findViewById(R.id.tv_group_name);
            convertView.setTag(groupHolder);
        }else{
            groupHolder = (ViewHolderGroup) convertView.getTag();
        }
        groupHolder.tv_group_name.setText(groups.get(groupPosition).getgName());
        return convertView;

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolderItem itemHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_notification_item, parent, false);
            itemHolder = new ViewHolderItem();
            itemHolder.img_icon = convertView.findViewById(R.id.img_icon);
            itemHolder.tv_content = convertView.findViewById(R.id.tv_content);
            itemHolder.delay_time=convertView.findViewById(R.id.noti_time);
            convertView.setTag(itemHolder);
        }else{
            itemHolder = (ViewHolderItem) convertView.getTag();
        }
        Log.d(TAG, "getChildView: "+childPosition+" "+childs.get(0).get(childPosition).swarmAddress);
        itemHolder.tv_content.setText(childs.get(0).get(childPosition).swarmAddress+"/ipfs/"+childs.get(0).get(childPosition).peerId);
//        itemHolder.tv_content.setText(childs.get(groupPosition).get(childPosition).actor+" "+childs.get(groupPosition).get(childPosition).swarmAddress);
        itemHolder.img_icon.setImageBitmap(BitmapFactory.decodeFile(childs.get(groupPosition).get(childPosition).avatarPath));
        itemHolder.delay_time.setText("delay:"+childs.get(0).get(childPosition).delay_time+"  derection:"+childs.get(0).get(childPosition).direction);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static class ViewHolderGroup{
        private TextView tv_group_name;
    }

    private static class ViewHolderItem{
        private TextView delay_time;
        private ImageView img_icon;
        private TextView tv_content;
    }
}
