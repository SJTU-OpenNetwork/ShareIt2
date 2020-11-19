package com.sjtuopennetwork.shareit.setting;

import android.content.Context;
import android.graphics.ColorSpace;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.setting.util.GetIpAddress;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import sjtu.opennet.hon.Textile;
import sjtu.opennet.textilepb.Model;
import sjtu.opennet.textilepb.QueryOuterClass;

public class MyDevicesActivity extends AppCompatActivity {

    ListView lv_devices;
    private List<String> device_list;
    private List<String> getDevice_list;
    private List<Model.Thread> threads;
    sjtu.opennet.textilepb.View.TextList textList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_devices);
        QueryOuterClass.QueryOptions options = QueryOuterClass.QueryOptions.newBuilder().build();
        try {
            Textile.instance().account.sync(options);
        } catch (Exception e) {
            e.printStackTrace();
        }


        initUI();
        initData();
        drawUI();
    }

    private void initUI() {
        lv_devices = findViewById(R.id.devices_list);
    }

    private void initData() {

        try {
            threads=Textile.instance().threads.list().getItemsList();
            device_list=new ArrayList<>();
            device_list.clear();
            getDevice_list=new ArrayList<>();
            for(Model.Thread t:threads) {
                if (t.getName().equals("mydevice1219")) {//找到mydevice1219这个thread
                    textList = Textile.instance().messages.list("", 100, t.getId());
                    for (int i = 0; i < textList.getItemsCount(); i++) {
                        String s = textList.getItems(i).getBody();
                        System.out.println("=============="+s);
                        if(s.contains("%"))
                        s= s.substring(0, s.indexOf("%"));
                        System.out.println("====处理后===="+s);
                        getDevice_list.add(s);
                    }
                }
            }
            noRepeat(getDevice_list);
//            Context context=this.getApplicationContext();
//            String self= GetIpAddress.getIPAddress(context)+"/tcp/40601/ipfs/"+ Textile.instance().ipfs.peerId();
            for(String str:getDevice_list)
            {
//                String addr=str.substring(str.indexOf("%")+1);
//                if (str.indexOf(self)!=-1||Textile.instance().ipfs.swarmConnect(addr)) {//swarmConnect方法需测试
//                    str = str.substring(0, str.indexOf("#")) + " 在线";
//                    device_list.add(str);
//                } else {
                    str = str.substring(0, str.indexOf("#"));
//                    System.out.println("=============="+str);
                    device_list.add(str);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void noRepeat(List<String> al) {
        if (al.size() > 0) {
            for (int i = 0; i < al.size(); i++) {
                for (int j = i + 1; j < al.size(); j++) {
                    if (al.get(i).equals(al.get(j))) {
                        System.out.println("=====删除重复记录=="+al.get(j));
                        al.remove(j);
                        j--;
                        continue;
                    } else {
                    }
                }
            }
        }
    }


    private void drawUI() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                MyDevicesActivity.this, android.R.layout.simple_list_item_1, device_list);
        lv_devices.setAdapter(adapter);
    }

}
