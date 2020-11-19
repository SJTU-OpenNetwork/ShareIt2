package com.sjtuopennetwork.shareit.util.contactlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.util.ShareUtil;

import java.util.List;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactAdapterHolder> {

    private List<MyContactBean> mData;
    public boolean isChoose;
    public MyContactListener<MyContactBean> listener;
    Context context;

    public ContactListAdapter(Context context, List<MyContactBean> mData) {
        this.context=context;
        this.mData = mData;
    }

    @NonNull
    @Override
    public ContactAdapterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_contact_list, viewGroup, false);
        ContactAdapterHolder contactAdapterHolder = new ContactAdapterHolder(view);
        return contactAdapterHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull final ContactAdapterHolder contactAdapterHolder, int position) {
        final MyContactBean contactBean=mData.get(position);
        contactAdapterHolder.tvName.setText(contactBean.name);
        contactAdapterHolder.itemView.setOnClickListener(v -> {
            if (isChoose) {
                if (contactBean.checkEnable){
                    boolean result = !contactBean.isChoose;
                    contactAdapterHolder.cbCheck.setChecked(result);
                    contactBean.isChoose=result;
                }
            } else {
                if (listener != null) {
                    listener.onClick(contactBean);
                }
            }
        });
        ShareUtil.setImageView(context, contactAdapterHolder.ivAvatar,contactBean.avatar, ShareUtil.ImgType.AVATAR);

        //checkbox
        contactAdapterHolder.cbCheck.setVisibility(isChoose ? View.VISIBLE : View.GONE);
        contactAdapterHolder.cbCheck.setChecked(contactBean.isChoose);
        contactAdapterHolder.cbCheck.setEnabled(contactBean.checkEnable);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class ContactAdapterHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivAvatar;
        CheckBox cbCheck;

        ContactAdapterHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.contact_list_name);
            ivAvatar = itemView.findViewById(R.id.contact_list_avatar);
            cbCheck = itemView.findViewById(R.id.contact_list_check);
        }
    }
    public interface MyContactListener<T> {
        void onClick(T item);
    }
}
