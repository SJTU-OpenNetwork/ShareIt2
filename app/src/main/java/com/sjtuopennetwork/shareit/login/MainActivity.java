package com.sjtuopennetwork.shareit.login;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.auth.api.signin.HuaweiIdSignIn;
import com.huawei.hms.auth.api.signin.HuaweiIdSignInClient;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.api.hwid.HuaweiIdSignInOptions;
import com.huawei.hms.support.api.hwid.HuaweiIdStatusCodes;
import com.huawei.hms.support.api.hwid.SignInHuaweiId;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.share.HomeActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "===============";

    //UI控件
    Button huaweiLogin;
    Button shareItLogin;
    Button shareItRegister;
    ImageView registerAvatar;
    EditText editText;

    //持久化存储
    SharedPreferences pref;

    //内存数据
    boolean isLogin; //是否已经登录，从pref中读出
    String avatarpath; //存放登录时头像图片的路径

    //华为ID
    private HuaweiIdSignInClient mSignInClient;
    private HuaweiIdSignInOptions mSignInOptions;
    String myclientid = "218779032643175488";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref=getSharedPreferences("txtl",MODE_PRIVATE);

        getPermission();

        //查SharedPreference中"isLogin"判断登录状态，如果未登录则进入登录界面。如果已登录则跳转到HomeActivity
        isLogin=pref.getBoolean("isLogin",false); //如果没有这个字段就是首次打开
        if(isLogin){ //如果已经登录直接跳转到主界面，默认从pref中读取启动数据
            Intent toHomeActivity=new Intent(this, HomeActivity.class);
            toHomeActivity.putExtra("login",0); //已经处于登录状态，0
            startActivity(toHomeActivity);
            finish();
        }else{ //如果未登录
            setContentView(R.layout.activity_main); //进入登录界面
            huaweiLogin=findViewById(R.id.huaweiLogin);
            shareItLogin=findViewById(R.id.shareItLogin);
            shareItRegister=findViewById(R.id.shareItRegister);
            registerAvatar=findViewById(R.id.register_avatar);
            editText=findViewById(R.id.edt_name);

            registerAvatar.setOnClickListener(v -> {
                com.luck.picture.lib.PictureSelector.create(MainActivity.this)
                        .openGallery(PictureMimeType.ofImage())
                        .maxSelectNum(1)
                        .compress(true)
                        .enableCrop(true).withAspectRatio(1,1)
                        .forResult(PictureConfig.TYPE_IMAGE);
            });

            shareItRegister.setOnClickListener(v -> {
                SharedPreferences.Editor editor=pref.edit();
                String myname=editText.getText().toString();
                if(myname.equals("")){
//                    Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                    Intent toHomeActivity=new Intent(this, HomeActivity.class);
                    toHomeActivity.putExtra("login",1); //shareit注册新账号，1
                    myname=String.valueOf(System.currentTimeMillis());
//                    toHomeActivity.putExtra("myname",myname); //到这里必然不为空
//                    toHomeActivity.putExtra("avatarpath",avatarpath);
                    editor.putString("myname",myname);
                    editor.putString("avatarpath",avatarpath);
                    editor.commit();
                    startActivity(toHomeActivity);
                    finish();
                }else{
                    //跳转到HomeActivity
                    Intent toHomeActivity=new Intent(this, HomeActivity.class);
                    toHomeActivity.putExtra("login",1); //shareit注册新账号，1
//                    toHomeActivity.putExtra("myname",myname); //到这里必然不为空
//                    toHomeActivity.putExtra("avatarpath",avatarpath);
                    editor.putString("myname",myname);
                    editor.putString("avatarpath",avatarpath);
                    editor.commit();
                    startActivity(toHomeActivity);
                    finish();
                }
            });

            huaweiLogin.setOnClickListener(v -> {
                //使用华为账号登录
                mSignInOptions = new HuaweiIdSignInOptions.Builder(HuaweiIdSignInOptions.DEFAULT_SIGN_IN).build();
                mSignInClient= HuaweiIdSignIn.getClient(MainActivity.this,mSignInOptions);
                startActivityForResult(mSignInClient.getSignInIntent(), 8888);
            });

            shareItLogin.setOnClickListener(v -> {
                //跳转到shareit登录界面
                Intent toShareIt=new Intent(MainActivity.this,ShareItLoginActivity.class);
                startActivity(toShareIt);
            });
        }
    }

    //从华为ID返回到结果之后，将结果写入到SharedPreference
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 8888) {
            Task<SignInHuaweiId> signInHuaweiIdTask = HuaweiIdSignIn.getSignedInAccountFromIntent(data);
            if (signInHuaweiIdTask.isSuccessful()) {
                //登录成功，获取用户的华为帐号信息和ID Token
                SignInHuaweiId huaweiAccount = signInHuaweiIdTask.getResult();

                //暂时不验证
//                //已经调用华为授权，使用方法：requestIdToken(String clientId)返回IDtoken，验证其有效性；
//                String clientId = myclientid;
//                String id_token =huaweiAccount.getIdToken();
//                JWT jwt= new JWT(id_token);
//                String issuer = jwt.getIssuer();

                //IDToken有效性验证完成，调用requestId() 返回OpenId，与textile账号关联

                String openid = huaweiAccount.getOpenId();
                String avatarUri=huaweiAccount.getPhotoUrl().toString();
                String myname=huaweiAccount.getDisplayName();
                SharedPreferences.Editor editor=pref.edit();
                editor.putString("openid",openid);
                editor.putString("avatarUri",avatarUri);
                editor.commit();

                Intent toHomeActivity=new Intent(this, HomeActivity.class);
                toHomeActivity.putExtra("login",2); //华为账号登录，2
                toHomeActivity.putExtra("myname",myname);
                startActivity(toHomeActivity);
                finish();
            } else{ //先不处理登录失败
                int status = ((ApiException) signInHuaweiIdTask.getException()).getStatusCode();
                //  Log.i(TAG, "signIn failed: " + status);
                if (status == HuaweiIdStatusCodes.SIGN_IN_UNLOGIN) {
                    //      Log.i(TAG, "Account not logged in");
                    //Account not logged in , try to sign in with getSignInIntent()
                } else if (status == HuaweiIdStatusCodes.SIGN_IN_AUTH) {
                    //      Log.i(TAG, "Account not authorized");
                    //Account not authorized  , try to authorize with getSignInIntent()
                } else if (status == HuaweiIdStatusCodes.SIGN_IN_CHECK_PASSWORD) {
                    //Huawei Account Need Password Check
                } else if (status == HuaweiIdStatusCodes.SIGN_IN_NETWORK_ERROR) {
                    //Network exception, please CP handle it by itsel
                } else {
                    //other exception, please CP handle it by itsel
                }
            }
        }

        if(requestCode==PictureConfig.TYPE_IMAGE){
            List<LocalMedia> choosePic= com.luck.picture.lib.PictureSelector.obtainMultipleResult(data);
            avatarpath=choosePic.get(0).getCompressPath();
            registerAvatar.setImageBitmap(BitmapFactory.decodeFile(avatarpath));
        }
  }

    private void getPermission() {
        if(PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==PermissionChecker.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            "android.permission.WRITE_EXTERNAL_STORAGE",
                            "android.permission.READ_EXTERNAL_STORAGE",
                            "android.permission.CAMERA"
                    },100);
        }
    }


    /**
     * 发送Get请求到服务器
     * @param strUrlPath:接口地址（带参数）
     * @return
     */
    public static String getServiceInfo(String strUrlPath){
        String strResult = "";
        try {
            URL url = null;
            try {
                url = new URL(strUrlPath);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection)url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null){
                buffer.append(line);
            }
            strResult = buffer.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }
}



