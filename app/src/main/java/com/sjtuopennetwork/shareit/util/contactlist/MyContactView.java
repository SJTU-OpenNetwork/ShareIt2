package com.sjtuopennetwork.shareit.util.contactlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.sjtuopennetwork.shareit.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyContactView extends FrameLayout {

    private RecyclerView rvList;
    private List<MyContactBean> mData=new ArrayList<>();
    private ContactListAdapter mAdapter = new ContactListAdapter(getContext(),mData);


    public MyContactView(@NonNull Context context) {
        this(context, null);
    }

    public MyContactView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.contact_view, this);
        initView();
    }

    private void initView() {
        rvList=findViewById(R.id.myrvList);
        rvList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvList.setAdapter(mAdapter);
    }

    public List<MyContactBean> getChoosedContacts() {
        List<MyContactBean> list = new ArrayList<>();
        for (MyContactBean contactBean :
                mData) {
            if (contactBean.isChoose) {
                list.add(contactBean);
            }
        }
        return list;
    }

    public void setData(List<MyContactBean> data, boolean isChoose) {
        if (isChoose) {
            List<MyContactBean> tempList = new ArrayList<>();
            for (MyContactBean contactBean : data) {
                if (contactBean.index<0){
                    tempList.add(contactBean);
                }
            }
            data = tempList;
        }

        mData.clear();
        mData.addAll(data);
        //先进行排序
        Collections.sort(mData, new LetterCompare());
        mAdapter.notifyDataSetChanged();
        mAdapter.isChoose=isChoose;
    }

    public void setListener(ContactListAdapter.MyContactListener<MyContactBean> contactListener){
        mAdapter.listener=contactListener;
    }
}
