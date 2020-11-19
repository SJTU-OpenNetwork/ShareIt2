package com.sjtuopennetwork.shareit.album.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.share.ImageInfoActivity;
import com.sjtuopennetwork.shareit.util.ShareUtil;

import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter {

    Context ctx;
    ArrayList<String> photoHashs;
    String threadid;

    public PhotoAdapter(Context context, ArrayList<String> photoHashs, String threadid) {
        this.ctx=context;
        this.photoHashs = photoHashs;
        this.threadid=threadid;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImage;
        View photoView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView=itemView;
            photoImage=itemView.findViewById(R.id.sync_photo_image);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sync_photo,viewGroup,false);
        ViewHolder viewHolder=new ViewHolder(view);
        viewHolder.photoView.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();
            String[] hashName=photoHashs.get(position).split("##");
            Intent it=new Intent(ctx, ImageInfoActivity.class);
            it.putExtra("imghash",hashName[0]);
            it.putExtra("imgname",hashName[1]);
            ctx.startActivity(it);
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        String[] hashName=photoHashs.get(position).split("##");
        ViewHolder myViewHolder=(ViewHolder)viewHolder;
        ShareUtil.setImageView(ctx,myViewHolder.photoImage,hashName[0], ShareUtil.ImgType.TXTL);
    }

    @Override
    public int getItemCount() {
        return photoHashs.size();
    }
}
