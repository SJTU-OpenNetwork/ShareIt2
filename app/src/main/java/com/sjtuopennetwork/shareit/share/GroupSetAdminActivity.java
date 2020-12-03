package com.sjtuopennetwork.shareit.share;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.share.util.MemberInfo;
import com.sjtuopennetwork.shareit.util.contactlist.MyContactBean;
import com.sjtuopennetwork.shareit.util.contactlist.MyContactView;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import sjtu.opennet.hon.Textile;

public class GroupSetAdminActivity extends AppCompatActivity {

    //UI控件
    Button change_admin;
    MyContactView contactView;

    //内存数据
    String threadid;
    List<MyContactBean> contactBeans;
  //  List<Model.Peer> nonAdmins;
    List<MemberInfo>nonAdmin ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_set_admin);

        initUI();

        initData();

    }

    private void initUI() {
        change_admin=findViewById(R.id.change_admin);
        contactView=findViewById(R.id.group_non_admins);
    }
    private void initData() {
        contactBeans=new LinkedList<>();
       // nonAdmins=new LinkedList<>();
        nonAdmin=new LinkedList<>();
        try {
            threadid=getIntent().getStringExtra("threadid");
         //   nonAdmins= Textile.instance().threads.nonAdmins(threadid).getItemsList();
            String generalMembers = Textile.instance().threads2.thread2PeerBySort(threadid,"GENERAL_MEMBER");
            getMemberInfo(generalMembers);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(MemberInfo p:nonAdmin){
            MyContactBean contactBean=new MyContactBean(p.address,p.name,p.avatar);
            contactBeans.add(contactBean);
        }
        contactView.setData(contactBeans,true);

        change_admin.setOnClickListener(v -> {
            List<MyContactBean> selects=contactView.getChoosedContacts();
            for(MyContactBean c:selects){ //逐个添加管理员
                try {
                   // Textile.instance().threads.addAdmin(threadid,c.id);
                    Textile.instance().threads2.thread2SetAdmin(threadid,c.id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            finish();
        });
    }

    public void getMemberInfo(String xmlString){
        try{
            Document doc2;
            doc2 = DocumentHelper.parseText(xmlString);
            Element rootElt = doc2.getRootElement();
            Iterator iterator = rootElt.elementIterator();
            while (iterator.hasNext()){
                Element stu = (Element) iterator.next();
                System.out.println("======遍历子节点======");
                String memberName = stu.elementText("name");
                System.out.println("======name:======"+memberName);
                String memberAvatar = stu.elementText("avatar");//TODO: avatar 之后处理
                String memberAddress = stu.elementText("address");
                MemberInfo newMember = new MemberInfo(memberName,"",memberAddress);
                nonAdmin.add(newMember);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
