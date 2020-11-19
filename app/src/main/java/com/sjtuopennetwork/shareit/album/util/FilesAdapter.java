package com.sjtuopennetwork.shareit.album.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.util.ShareUtil;

import java.util.ArrayList;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;

public class FilesAdapter extends RecyclerView.Adapter {

    Context ctx;
    ArrayList<String> fileNames;
    ArrayList<String> fileHashs;

    public FilesAdapter(Context context, ArrayList<String> fileNames, ArrayList<String> fileHashs) {
        this.ctx=context;
        this.fileNames = fileNames;
        this.fileHashs=fileHashs;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        View fileView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileView=itemView;
            fileName=itemView.findViewById(R.id.sync_file_name);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sync_file,viewGroup,false);
        FilesAdapter.ViewHolder viewHolder=new FilesAdapter.ViewHolder(view);
        viewHolder.fileView.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();
            AlertDialog.Builder downFile=new AlertDialog.Builder(ctx);
            downFile.setTitle("下载文件");
            downFile.setMessage("确定下载 "+ fileNames.get(position) +" 吗？");
            downFile.setPositiveButton("下载", (dialog, which) -> {
                System.out.println("=======hash:"+fileHashs.get(position));
                Textile.instance().files.content(fileHashs.get(position), new Handlers.DataHandler() {
                    @Override
                    public void onComplete(byte[] data, String media) {
                        String res= ShareUtil.storeSyncFile(data,fileNames.get(position));
                        System.out.println("======下载成功"+res);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            });
            downFile.setNegativeButton("取消", (dialog, which) -> Toast.makeText(ctx,"已取消",Toast.LENGTH_SHORT).show());
            downFile.show();
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        String theFileName= fileNames.get(position);
        FilesAdapter.ViewHolder myViewHolder=(FilesAdapter.ViewHolder)viewHolder;
        myViewHolder.fileName.setText(theFileName);
    }

    @Override
    public int getItemCount() {
        return fileNames.size();
    }
}
