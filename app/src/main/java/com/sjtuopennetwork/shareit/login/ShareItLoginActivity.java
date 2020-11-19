package com.sjtuopennetwork.shareit.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.share.HomeActivity;
import com.sjtuopennetwork.shareit.util.ShareUtil;

import java.io.File;

import sjtu.opennet.hon.Textile;
import sjtu.opennet.textilepb.Mobile;

public class ShareItLoginActivity extends AppCompatActivity {

    private static final String TAG = "==================";

    //UI控件
    EditText editText;
    Button logInWithPhrase;

    //持久化存储
    SharedPreferences pref;

    //内存数据
    String repoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_it_login);

        pref=getSharedPreferences("txtl",MODE_PRIVATE);

        editText=findViewById(R.id.edt_phrase);
        logInWithPhrase=findViewById(R.id.shareItLoginWithPhrase);

        logInWithPhrase.setOnClickListener(v -> {
            String phrase=editText.getText().toString();
            final File repoDir = new File(ShareUtil.getAppExternalPath(this, "repo"));
            Log.d(TAG, "onCreate: shareit login: "+repoDir.getAbsolutePath());

            boolean loginWrong=false;
            String loginAccount="";
//            final File filesDir = this.getFilesDir();
            try {
                Mobile.MobileWalletAccount m= Textile.walletAccountAt(phrase,Textile.WALLET_ACCOUNT_INDEX,Textile.WALLET_PASSPHRASE);
                loginAccount=m.getAddress();
                repoPath = new File(repoDir, loginAccount).getAbsolutePath();
                Log.d(TAG, "onCreate: shareit repo path: "+repoPath);
                if (!Textile.isInitialized(repoPath)){
                    Textile.initialize(repoPath,m.getSeed() , true, false,true);
                }
            } catch (Exception e) {
                loginWrong=true;
                Log.d(TAG, "onCreate: 助记词错误");
            }

            if(loginWrong){ //如果登录出了问题
                Toast.makeText(this,"助记词错误",Toast.LENGTH_SHORT).show();
                editText.setText("");
            }else { //如果登录成功
                Log.d(TAG, "onCreate: 登录账户"+loginAccount);
                SharedPreferences.Editor editor=pref.edit();
                editor.putString("loginAccount",loginAccount);
                editor.commit();

                Intent toHomeActivity=new Intent(ShareItLoginActivity.this, HomeActivity.class);
                toHomeActivity.putExtra("login",3); //shareit登录
                toHomeActivity.putExtra("repopath",repoPath);
                startActivity(toHomeActivity);
                finish();
            }
        });
    }
}
