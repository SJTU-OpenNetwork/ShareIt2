package com.sjtuopennetwork.shareit.share.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.util.ShareUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import sjtu.opennet.hon.Textile;

public class DialogAdapter extends ArrayAdapter {
    private static final String TAG = "==================";

    private List<TDialog> datas;
    private Context context;
    private int resource;

    public DialogAdapter(Context context, int resource, List<TDialog> datas) {
        super(context, resource, datas);
        this.datas = datas;
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d(TAG, "getView: Dialog: "+ position);
        View v;
        ViewHolder vh;
        TDialog tDialog = datas.get(position);
        if (convertView == null) {
            v = LayoutInflater.from(context).inflate(resource, parent, false);
            vh = new ViewHolder(v);
            v.setTag(vh);
        } else {
            v = convertView;
            vh = (ViewHolder) v.getTag();
        }

        String avatarHash="";
        String dialogname="通知";
        if (tDialog.add_or_img.equals("tongzhi")) {
            vh.headImg.setImageResource(R.drawable.ic_notification_img);
        } else if(tDialog.add_or_img.equals("multicast")){
            vh.headImg.setImageResource(R.drawable.ic_notification);
            dialogname="多播组";
        }else { //单人根据addr显示头像，多人根据threadid得到threadname
            try {
                if(tDialog.isSingle){
                    avatarHash=Textile.instance().contacts.get(datas.get(position).add_or_img).getAvatar();
                    dialogname=Textile.instance().contacts.get(datas.get(position).add_or_img).getName();
                }else{
                    avatarHash=datas.get(position).add_or_img;
                  //  dialogname=Textile.instance().threads.get(datas.get(position).threadid).getName();
                    dialogname=Textile.instance().threads2.groupName(datas.get(position).threadid);
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }
            ShareUtil.setImageView(context,vh.headImg,avatarHash, ShareUtil.ImgType.TXTL);
        }
        vh.threadName.setText(dialogname);

        if (tDialog.isRead) {
            vh.isRead.setVisibility(View.GONE);
        } else {
            vh.isRead.setVisibility(View.VISIBLE);
        }
        DateFormat df = new SimpleDateFormat("MM-dd HH:mm");

        vh.lastMsg.setText(tDialog.lastmsg);
        vh.lastMsgDate.setText(df.format(tDialog.lastmsgdate * 1000));

        return v;
    }

    class ViewHolder {
        private TextView threadName, lastMsg, isRead, lastMsgDate;
        private ImageView headImg;

        public ViewHolder(View v) {
            threadName = v.findViewById(R.id.item_thread_name);
            lastMsg = v.findViewById(R.id.item_thread_lastmsg);
            lastMsgDate = v.findViewById(R.id.item_thread_lastmsgdate);
            headImg = v.findViewById(R.id.item_thread_img);
            isRead = v.findViewById(R.id.item_thread_badge);
        }
    }

}
