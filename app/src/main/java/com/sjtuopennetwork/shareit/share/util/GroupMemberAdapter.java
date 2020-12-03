package com.sjtuopennetwork.shareit.share.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.util.RoundImageView;
import com.sjtuopennetwork.shareit.util.ShareUtil;

import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {
    private static final String TAG = "===========================================";
    private List<MemberInfo> members;
    Context context;

    public GroupMemberAdapter(Context context, List<MemberInfo> members) {
        this.context = context;
        this.members = members;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public RoundImageView avatar;
        public TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.group_member_avatar);
            name = itemView.findViewById(R.id.group_member_name);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_group_member, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Log.d(TAG, "onBindViewHolder: "+i);
        viewHolder.name.setText(members.get(i).name);
        Log.d(TAG, "onBindViewHolder: avatar:"+members.get(i).avatar);
        ShareUtil.setImageView(context, viewHolder.avatar, members.get(i).avatar, ShareUtil.ImgType.AVATAR);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

}