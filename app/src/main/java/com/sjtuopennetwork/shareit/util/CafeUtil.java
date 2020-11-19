package com.sjtuopennetwork.shareit.util;

import android.util.Log;

import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.textilepb.Model;

public class CafeUtil {
    private static final String TAG = "=====================";
    private static boolean stopConnect=false;

    public static boolean isConnecting(){
        return !stopConnect;
    }

    public static void connectCafe(Handlers.ErrorHandler handler){
        new Thread(){
            @Override
            public void run() {
                while(!stopConnect){
                    Log.d(TAG, "run: 开始连接cafe");
                    Textile.instance().cafes.register(
//                    "http://159.138.58.61:40601",
                            "http://202.120.38.131:40601",
//                    "http://192.168.1.109:40601",
//                    "http://202.120.40.60:40601"
                            "3ZBEg21LcVFgHzjFBRJsrjWmxMngpcAK1jMhx8AbuTo5HdmDAC54UXw7eTXQ", //131
                            handler);
                    try {
                        Thread.sleep(180000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public static void stopConnect(Handlers.ErrorHandler handler){
        new Thread(){
            @Override
            public void run() {
                stopConnect=true;
                try {
                    Model.CafeSessionList cafeSessionList = Textile.instance().cafes.sessions();
                    for(Model.CafeSession c:cafeSessionList.getItemsList()){
                        Textile.instance().cafes.deregister(c.getId(), handler);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
