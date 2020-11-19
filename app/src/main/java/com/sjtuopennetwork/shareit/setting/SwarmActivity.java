package com.sjtuopennetwork.shareit.setting;

import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.contact.util.ContactUtil;
import com.sjtuopennetwork.shareit.setting.util.NotificationGroup;
import com.sjtuopennetwork.shareit.setting.util.NotificationItem;
import com.sjtuopennetwork.shareit.setting.util.SwarmPeerListAdapter;

import java.util.ArrayList;
import java.util.List;

import sjtu.opennet.hon.Textile;
import sjtu.opennet.textilepb.Model;

public class SwarmActivity extends AppCompatActivity {


    private LinearLayout info_swarm_layout;   //swarm地址板块
    private ExpandableListView swarm_peer_listView;
    private LinearLayout info_swarm_peer_layout; //swarmPeer列表板块
    private TextView info_swarm_address;


    private ArrayList<NotificationItem> swarm_peer_list = null;
    private Model.SwarmPeerList swarmPeerList;
    private List<Model.SwarmPeer> swarmPeers;
    private SwarmPeerListAdapter swarmPeerListAdapter=null;
    private String swarm_address;
    private ArrayList<NotificationGroup> gData = null;
    private ArrayList<ArrayList<NotificationItem>> iData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swarm);

        info_swarm_layout=findViewById(R.id.setting_personal_info_swarm_address);
        info_swarm_address=findViewById(R.id.info_swarm_address);
        swarm_peer_listView=findViewById(R.id.swarm_peer_list);
        info_swarm_peer_layout=findViewById(R.id.setting_personal_info_swarm_peer_list);

        try {//获取自己的swarm地址
            String peerId = Textile.instance().profile.get().getId();
            swarm_address=Textile.instance().ipfs.getSwarmAddress(peerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        info_swarm_address.setText(swarm_address);

        initSwarmPeerList();

        info_swarm_layout.setOnClickListener(view -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 将文本内容放到系统剪贴板里。
            cm.setText(info_swarm_address.getText());
            Toast.makeText(this,"已将swarm地址复制到剪贴板："+info_swarm_address.getText(),Toast.LENGTH_LONG).show();
        });
    }


    //获取swarm列表
    private void initSwarmPeerList() {
        swarm_peer_list=new ArrayList<>();
        gData=new ArrayList<>();
        iData=new ArrayList<>();
        List<Model.Peer> friendList;
        friendList= ContactUtil.getFriendList();
        try {
            swarmPeerList= Textile.instance().ipfs.connectedAddresses();
            swarmPeers=swarmPeerList.getItemsList();
            gData.add(new NotificationGroup("  swarm地址列表"+"  total: "+swarmPeers.size()));
            for(Model.SwarmPeer peer:swarmPeers){
                String avatar="";
                String name="";
                for(Model.Peer p:friendList){//如果是好友则显示头像和姓名
                    if(p.getId().equals(peer.getId())){
                        avatar=p.getAvatar();
                        name=p.getName();
                    }
                }
                swarm_peer_list.add(new NotificationItem(peer.getId(),peer.getLatency(),avatar,name,peer.getAddr(),peer.getDirection()));
            }
            iData.add(swarm_peer_list);
        } catch (Exception e) {
            e.printStackTrace();
        }

        swarmPeerListAdapter = new SwarmPeerListAdapter(SwarmActivity.this,gData,iData);
        swarm_peer_listView.setAdapter(swarmPeerListAdapter);

        swarm_peer_listView.collapseGroup(0);
        swarm_peer_listView.setOnChildClickListener((expandableListView, view, i, i1, l) -> {
            String swarm_url=iData.get(0).get(i1).swarmAddress+"/ipfs/"+iData.get(0).get(i1).peerId;
            Toast.makeText(SwarmActivity.this,"已将swarm地址复制到剪贴板："+swarm_url,Toast.LENGTH_LONG).show();
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 将文本内容放到系统剪贴板里。
            cm.setText(swarm_url);
            return false;
        });
    }

}
