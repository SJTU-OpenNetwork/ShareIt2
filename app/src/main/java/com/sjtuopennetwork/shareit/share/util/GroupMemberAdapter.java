package com.sjtuopennetwork.shareit.share.util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.util.ShareUtil;
import com.sjtuopennetwork.shareit.util.RoundImageView;

import java.util.List;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.textilepb.Model;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {
    private static final String TAG = "===========================================";
    private List<Model.Peer> members;
    Context context;

    public GroupMemberAdapter(Context context, List<Model.Peer> members) {
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
        viewHolder.name.setText(members.get(i).getName());
        Log.d(TAG, "onBindViewHolder: avatar:"+members.get(i).getAvatar());
        ShareUtil.setImageView(context, viewHolder.avatar, members.get(i).getAvatar(), ShareUtil.ImgType.AVATAR);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

}