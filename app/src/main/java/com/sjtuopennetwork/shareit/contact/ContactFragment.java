package com.sjtuopennetwork.shareit.contact;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.qrlibrary.qrcode.utils.PermissionUtils;
import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.contact.util.ContactUtil;
import com.sjtuopennetwork.shareit.util.ShareUtil;
import com.sjtuopennetwork.shareit.util.QRCodeActivity;
import com.sjtuopennetwork.shareit.util.contactlist.ContactListAdapter;
import com.sjtuopennetwork.shareit.util.contactlist.MyContactBean;
import com.sjtuopennetwork.shareit.util.contactlist.MyContactView;

import java.util.LinkedList;
import java.util.List;

import sjtu.opennet.textilepb.Model;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment {

    //UI控件
    private MyContactView contactView;
    LinearLayout contact_discover_layout;
    LinearLayout new_friend_layout;
    ImageView bt_contact_search;
    ImageView bt_contact_scan;
    TextView application_badge;

    //内存数据
    List<MyContactBean> contactBeans;
    List<Model.Peer> myFriends;
    boolean textileOn;

    //持久化存储
    public SharedPreferences pref;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        initUI();

        initData();
    }

    private void initData(){
        pref=getActivity().getSharedPreferences("txtl", Context.MODE_PRIVATE);
        textileOn=pref.getBoolean("textileon",false);

        if(textileOn) {
            //显示好友列表
            myFriends = ContactUtil.getFriendList();

            contactBeans = new LinkedList<>();

            for (Model.Peer p : myFriends) {
                MyContactBean contactBean = new MyContactBean(p.getAddress(), p.getName(), p.getAvatar());
                contactBeans.add(contactBean);
            }
            contactView.setData(contactBeans, false);

            contactView.setListener(item -> {
                Intent it = new Intent(getActivity(), ContactInfoActivity.class);
                it.putExtra("address", item.id);
                startActivity(it);
            });


            contact_discover_layout.setOnClickListener(v -> {
                Intent it=new Intent(getActivity(),ContactDiscoverActivity.class);
                startActivity(it);
            });
            new_friend_layout.setOnClickListener(v -> {
                Intent it=new Intent(getActivity(),NewFriendActivity.class);
                startActivity(it);
            });
            bt_contact_search.setOnClickListener(v -> {
                Intent it=new Intent(getActivity(),SearchContactActivity.class);
                startActivity(it);
            });
            bt_contact_scan.setOnClickListener(v -> {
                PermissionUtils.getInstance().requestPermission(getActivity());

                Intent it=new Intent(getActivity(), QRCodeActivity.class);
                startActivity(it);
            });

            if(ContactUtil.getApplication().second.size()==0){
                application_badge.setVisibility(View.GONE);
            }
        }
    }

    private void initUI() {
        contact_discover_layout=getActivity().findViewById(R.id.contact_discover_layout);
        new_friend_layout=getActivity().findViewById(R.id.contact_new_friend_layout);
        bt_contact_search=getActivity().findViewById(R.id.bt_contact_search);
        contactView=getActivity().findViewById(R.id.contact_list);
        bt_contact_scan=getActivity().findViewById(R.id.bt_contact_scan);
        application_badge=getActivity().findViewById(R.id.application_badge);
    }

}
