package com.sjtuopennetwork.shareit.contact;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.contact.util.ContactUtil;
import com.sjtuopennetwork.shareit.contact.util.ResultAdapter;
import com.sjtuopennetwork.shareit.contact.util.ResultContact;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import sjtu.opennet.textilepb.Model;
import sjtu.opennet.textilepb.QueryOuterClass;
import sjtu.opennet.hon.Textile;

public class SearchContactActivity extends AppCompatActivity {

    //UI控件
    SearchView searchView; //搜索框
    ListView contact_search_result_lv;  //搜索结果列表
    ResultAdapter searchResultAdapter;  //搜索结果适配器

    //内存数据
    List<Model.Peer> myFriends;
    List<Model.Contact> newContacts;  //搜索到的结果
    List<ResultContact> resultContacts;  //存放自定义的搜索结果item对象
    List<String> inviteAddr=new LinkedList<>(); //存放要发送申请的联系人的地址
    String targetAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_contact);

        initUI();

        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

        initData();
    }

    private void initData() {
        //初始化已添加的联系人列表，这里还是应该从threa查出来
        myFriends= ContactUtil.getFriendList();

        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("请输入昵称");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                resultContacts.clear();
                newContacts.clear();
                QueryOuterClass.QueryOptions options = QueryOuterClass.QueryOptions.newBuilder()
                        .setWait(10)
                        .setLimit(1)
                        .build();
                QueryOuterClass.ContactQuery query = QueryOuterClass.ContactQuery.newBuilder()
                        .setName(s)
                        .build();
                try {
                    Textile.instance().contacts.search(query, options);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        searchView.setOnCloseListener(() -> {
            resultContacts.clear();
            newContacts.clear();
            return true;
        });

        contact_search_result_lv.setOnItemClickListener((parent, view, position, id) -> {
            Model.Contact wantToAdd=newContacts.get(position);
            AlertDialog.Builder addContact=new AlertDialog.Builder(SearchContactActivity.this);
            addContact.setTitle("添加联系人");
            addContact.setMessage("确定添加 "+wantToAdd.getName()+" 吗？");
            addContact.setPositiveButton("添加", (dialog, which) -> {
                try {
                    Textile.instance().contacts.add(wantToAdd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                inviteAddr.add(wantToAdd.getAddress()); //添加到申请列表
                targetAddress=wantToAdd.getAddress();
                ContactUtil.createTwoPersonThread(targetAddress); //创建双人thread,key就是那个人的地址
            });
            addContact.setNegativeButton("取消", (dialog, which) -> Toast.makeText(SearchContactActivity.this,"已取消",Toast.LENGTH_SHORT).show());
            addContact.show();
        });
    }

    private void initUI() {
        searchView=findViewById(R.id.contact_search);
        contact_search_result_lv=findViewById(R.id.contact_search_result_lv);

        resultContacts=Collections.synchronizedList(new LinkedList<>());
        newContacts=Collections.synchronizedList(new LinkedList<>());
        searchResultAdapter=new ResultAdapter(SearchContactActivity.this,R.layout.item_contact_search_result,resultContacts);
        searchResultAdapter.notifyDataSetChanged();
        contact_search_result_lv.setAdapter(searchResultAdapter);
    }

    //一次得到一个搜索结果
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getAnResult(Model.Contact c){
        for(Model.Peer p:myFriends){
            if(p.getAddress().equals(c.getAddress())){
                return; //如果已经添加过这个联系人直接返回
            }
        }

        //添加到结果列表
        String addr=c.getAddress();
        String addr_last10="address: "+addr.substring(addr.length()-10);
        newContacts.add(c);
        resultContacts.add(new ResultContact(addr_last10,c.getName(),c.getAvatar(),null,false));
        searchResultAdapter.notifyDataSetChanged();
        searchView.clearFocus();
    }

    //双人thread创建成功后就发送邀请，用户看起来就是好友申请
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void sendInvite(String threadId){
        try{
            Textile.instance().invites.add(threadId,targetAddress); //key就是联系人的address
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }
}
