package com.sjtuopennetwork.shareit.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.sjtuopennetwork.shareit.R;
import com.sjtuopennetwork.shareit.share.util.MsgType;
import com.sjtuopennetwork.shareit.share.util.TDialog;
import com.sjtuopennetwork.shareit.share.util.TMsg;
import com.sjtuopennetwork.shareit.share.util.TRecord;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import sjtu.opennet.hon.BaseTextileEventListener;
import sjtu.opennet.hon.FeedItemData;
import sjtu.opennet.hon.FeedItemType;
import sjtu.opennet.hon.Handlers;
import sjtu.opennet.hon.Textile;
import sjtu.opennet.hon.Thread2Data;
import sjtu.opennet.multicastfile.HONMulticaster;
import sjtu.opennet.textilepb.Mobile;
import sjtu.opennet.textilepb.Model;
import sjtu.opennet.textilepb.View;
import sjtu.opennet.util.FileUtil;

public class ShareService extends Service {
    public ShareService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static final String TAG = "========ShareService";

    private String loginAccount;
    private int login;
    private String repoPath;
    private SharedPreferences pref;
    private boolean connectCafe;
    private String myname;
    private String avatarpath;
    private String lastBlock = "0";
    private boolean serviceOn = true;
    private final Object LOCK = new Object();
    private final static Object SHOW_LOCK = new Object();
    boolean textileOn = false;

    static SimpleExoPlayer player;

    private HashMap<String, LinkedList<FileTransInfo>> recordTmp = new HashMap<>();
    private HashMap<String, MetaAndNotification> metaNotiMap = new HashMap<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pref = getSharedPreferences("txtl", MODE_PRIVATE);

        login = intent.getIntExtra("login", 0);
        myname = pref.getString("myname", "null");
        avatarpath = pref.getString("avatarpath", "null");

//        connectCafe = pref.getBoolean("connectCafe", false);
        textileOn = pref.getBoolean("textileon", false);
        connectCafe = pref.getBoolean("connectCafe", true);

        new Thread() {
            @Override
            public void run() {
                super.run();
                Log.d(TAG, "run: 启动前台服务");
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                NotificationChannel notificationChannel = new NotificationChannel("12", "前台服务", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(notificationChannel);
                Notification notification = new Notification.Builder(ShareService.this, "12")
                        .setContentText("为正常接收消息，请保持ShareIt在后台运行")
                        .setContentTitle("ShareIt正在运行")
                        .setSmallIcon(R.drawable.ic_share_launch)
                        .build();
                startForeground(108, notification);

                initTextile(login);

                if (textileOn) {
                    launchTextile();
                }
            }
        }.start();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        FileUtil.createDir();

        return super.onStartCommand(intent, flags, startId);
    }

    public void initTextile(int login) {
        final File repoDir = new File(ShareUtil.getAppExternalPath(this, "repo"));
        String phrase = "";
        //初始化repo
        switch (login) {
            case 0: //已经登录，找到repo，初始化textile
                loginAccount = pref.getString("loginAccount", "null"); //当前登录的account，就是address
                final File repo0 = new File(repoDir, loginAccount);
                repoPath = repo0.getAbsolutePath();
                break;
            case 1: //shareit注册，新建repo，初始化textile
                try {
                    phrase = Textile.newWallet(12); //助记词
                    Mobile.MobileWalletAccount m = Textile.walletAccountAt(phrase, Textile.WALLET_ACCOUNT_INDEX, Textile.WALLET_PASSPHRASE);
                    loginAccount = m.getAddress(); //获得公钥
                    final File repo1 = new File(repoDir, loginAccount);
                    repoPath = repo1.getAbsolutePath();
                    String sk = m.getSeed(); //获得私钥
                    Textile.initialize(repoPath, sk, true, false, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2: //华为账号登录，找到repo，初始化textile
                String openid = pref.getString("openid", ""); //?测试一下是否需要截断，应该并不需要
                String avatarUri = pref.getString("avatarUri", ""); //先判断一下是否已经存储过
                new Thread() {
                    @Override
                    public void run() {
                        avatarpath = ShareUtil.getHuaweiAvatar(avatarUri);
                    }
                }.start();
                try {
                    phrase = Textile.newWalletFromHuaweiOpenId(openid);
                    Mobile.MobileWalletAccount m = Textile.walletAccountAt(phrase, Textile.WALLET_ACCOUNT_INDEX, Textile.WALLET_PASSPHRASE);
                    loginAccount = m.getAddress();
                    final File repo1 = new File(repoDir, loginAccount);
                    repoPath = repo1.getAbsolutePath();
                    if (!Textile.isInitialized(repoPath)) {
                        Textile.initialize(repoPath, m.getSeed(), true, false, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3: //shareit助记词登录,已经初始化了，只需要设置一些变量
                phrase = pref.getString("phrase", "");
                loginAccount = pref.getString("loginAccount", "");
                break;
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isLogin", true);
        if (login == 1 || login == 2) { //1,2都需要修改助记词和登录账户,3不需要
            editor.putString("phrase", phrase);
            editor.putString("loginAccount", loginAccount);
        }
        editor.commit();

        launchMulticast();
    }

    private int getRand(int num, int peerCount) {
        int DEGREE = 3;
        int GAP = 2000; //毫秒
        Random r = new Random();
//        int rand = r.nextInt(peerCount); //Npeers 是群组人数
        int rand = num;
        int height = (int) (Math.log(peerCount) / Math.log(DEGREE));
        int level = 0;
        int index = DEGREE;
        while (level < height) {
            if (index > rand) {
                break;
            }
            level++;
            index = index + index * DEGREE;
        }
        return level * GAP;
    }

    class ThreadUpdateWork {
        Model.Thread thread;
        FeedItemData feedItemData;

        public ThreadUpdateWork(Model.Thread t, FeedItemData f) {
            thread = t;
            feedItemData = f;
        }
    }

    class Thread2UpdateWork {
       // Model.Thread thread;
        Thread2Data thread2Data;

        public Thread2UpdateWork(Thread2Data f) {
            thread2Data = f;
        }
    }

    public void launchTextile() {
        //启动Textile节点
        try {
            Textile.launch(ShareService.this, repoPath, true);
            Textile.instance().addEventListener(new ShareListener());
//            Textile.instance().setForegroundHandler(foregroundHandler);
            sjtu.opennet.textilepb.View.LogLevel logLevel = sjtu.opennet.textilepb.View.LogLevel.newBuilder()
//                    .putSystems("hon.engine", sjtu.opennet.textilepb.View.LogLevel.Level.DEBUG)
//                    .putSystems("hon.bitswap", sjtu.opennet.textilepb.View.LogLevel.Level.DEBUG)
//                    .putSystems("hon.peermanager", sjtu.opennet.textilepb.View.LogLevel.Level.DEBUG)
                    .putSystems("tex-core", sjtu.opennet.textilepb.View.LogLevel.Level.DEBUG)
                    .putSystems("tex-mobile", sjtu.opennet.textilepb.View.LogLevel.Level.DEBUG)
                    .putSystems("tex-service", sjtu.opennet.textilepb.View.LogLevel.Level.DEBUG)
                    .putSystems("stream", sjtu.opennet.textilepb.View.LogLevel.Level.DEBUG)
                    .putSystems("record", sjtu.opennet.textilepb.View.LogLevel.Level.DEBUG)
                    .build();
            Textile.instance().logs.setLevel(logLevel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void launchMulticast() {
        if (DBHelper.getInstance(getApplicationContext(), loginAccount).queryDialogByThreadID("20200729multicast") == null) {
            DBHelper.getInstance(getApplicationContext(), loginAccount).insertDialog(
                    "20200729multicast",
                    "组播聊天室",
                    System.currentTimeMillis() / 1000,
                    1, //后台收到默认是未读的
                    "multicast",
                    false,
                    1);
            Log.d(TAG, "nodeOnline: create multi dialog");
        } else {
            Log.d(TAG, "nodeOnline: has multi dialog");
        }
        try {
            HONMulticaster.setSelfName(myname);
            HONMulticaster.start(ShareService.this, new MulticastHandlerImpl(ShareService.this.getApplicationContext(), loginAccount));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!textileOn) { //如果没启动才广播
            EventBus.getDefault().post(Integer.valueOf(0));
        }
    }

    class MetaAndNotification {
        //        View.FeedStreamMeta feedStreamMeta;
        ThreadUpdateWork threadUpdateWork;
        Model.Notification notification;

        public MetaAndNotification(ThreadUpdateWork threadUpdateWork, Model.Notification notification) {
            this.threadUpdateWork = threadUpdateWork;
            this.notification = notification;
        }
    }

    Handler threadUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ThreadUpdateWork threadUpdateWork = (ThreadUpdateWork) msg.obj;
            handleThreadUpdate(threadUpdateWork);
        }
    };

    Handler thread2UpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Thread2UpdateWork thread2UpdateWork = (Thread2UpdateWork) msg.obj;
            handleThread2Update(thread2UpdateWork);
        }
    };
    private void sleepRandTime(){
//            final int peerCount = thread.getPeerCount();
//            try {
//                List<Model.Peer> l = Textile.instance().threads.peers(threadId).getItemsList();
//                int k = 0;
//                for (; k < l.size(); k++) {
//                    if (feedItemData.feedSimpleFile.getUser().getAddress().equals(l.get(k).getAddress())) {
//                        break;
//                    }
//                }
//                l.remove(k);
//                int v = 0;
//                for (; v < l.size(); v++) {
//                    if (myAddr.equals(l.get(v).getAddress())) {
//                        break;
//                    }
//                }
//                int delay = getRand(v, peerCount);
//                Log.d(TAG, "handleThreadUpdate: wait time:" + delay);
//                Thread.sleep(delay);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            Random r=new Random();
//            int delay=r.nextInt(120*peerCount);
    }

    public void handleThreadUpdate(ThreadUpdateWork threadUpdateWork) {
//        Log.d(TAG, "处理消息：" + threadUpdateWork.feedItemData.type + " " + threadUpdateWork.feedItemData.block);
        Model.Thread thread = threadUpdateWork.thread;
        FeedItemData feedItemData = threadUpdateWork.feedItemData;

        String myAddr = Textile.instance().account.address();
        String threadId = thread.getId();

        TDialog tDialog = DBHelper.getInstance(getApplicationContext(), loginAccount).queryDialogByThreadID(threadId); //必然能够查出来对话

        if (feedItemData.type.equals(FeedItemType.JOIN)) { //如何根据JOIN类消息创建对话？
            if (tDialog != null) { //如果已经有了就不要再插入了
                return; //如数据库已经记录了这个对话，为了简化逻辑，就不再加入
            }
            //如果首次收到这个thread的join，无论是对方还是我方，还是群组，都直接加入。
            int whiteListCount = thread.getWhitelistCount();
            boolean authorIsMe = feedItemData.join.getUser().getAddress().equals(myAddr); //表明是否是自己的JOIN
            boolean flag = false; //我的

            int isSingle = 0;
            String add_or_img = "";
            if (whiteListCount == 2) {
                if (!authorIsMe) {//双人，不是我，则是他人的好友同意
                    isSingle = 1;
                    add_or_img = feedItemData.join.getUser().getAddress();
                    Log.d(TAG, "threadUpdateReceived: get friend agree: " + add_or_img);
                    flag = true;
                }
            } else { //群组
                isSingle = 0;
                flag = true;
                Log.d(TAG, "threadUpdateReceived: get group");
            }

            if (flag) { //群组，或者双人接收才创建
                TDialog newDialog = DBHelper.getInstance(getApplicationContext(), loginAccount).insertDialog(
                        threadId,
                        "你好啊，现在我们已经成为好友了",
                        feedItemData.join.getDate().getSeconds(),
                        0, //后台收到默认是未读的
                        add_or_img,
                        isSingle == 1, 1);
                EventBus.getDefault().post(newDialog);
            }
        }

        if (feedItemData.type.equals(FeedItemType.TEXT)) { //如果是文本消息
            int ismine = 0;
            if (feedItemData.text.getUser().getAddress().equals(myAddr)) { // 是我自己的消息
                if (feedItemData.text.getBody().equals("ack")) { //自己的ack直接忽略
                    return;
                }
                ismine = 1;
            } else { // 别人的消息
                if (feedItemData.text.getBody().equals("ack")) {
//                    EventBus.getDefault().post(new Long(System.currentTimeMillis()));
                    Log.d(TAG, "handleThreadUpdate: get other's ack");
                    return;
                } // 回复别人的消息
                try {
                    Textile.instance().messages.add(threadId, "ack");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String[] msgwords = feedItemData.text.getBody().split(" ");
                if (msgwords[0].equals("set")) {
                    if (msgwords[1].equals("worker")) {
                        int deg = Integer.parseInt(msgwords[2]);
                        Textile.instance().streams.setDegree(deg);
//                        try {
//                            Textile.instance().messages.add(threadId, "response: set worker "+deg); // set worker 30
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                    }
                }
            }
            //插入msgs表
            TMsg tMsg = DBHelper.getInstance(getApplicationContext(), loginAccount).insertMsg(
                    threadId, MsgType.MSG_TEXT, feedItemData.text.getBlock(),
                    feedItemData.text.getUser().getAddress(),
                    feedItemData.text.getBody(),
                    feedItemData.text.getDate().getSeconds(), ismine);

            //更新dialogs表
            TDialog updateDialog = DBHelper.getInstance(getApplicationContext(), loginAccount).dialogGetMsg(tDialog, threadId,
                    feedItemData.text.getBody(), feedItemData.text.getDate().getSeconds(),
                    tDialog.add_or_img);
            tDialog.isRead = false;

            EventBus.getDefault().post(updateDialog);
            EventBus.getDefault().post(tMsg); //我的消息也要广播，所有的消息的显示都不要从本地来，而是后发送，本地其实还是很快的。

        }

        if (feedItemData.type.equals(FeedItemType.PICTURE)) {
            boolean isSingle = thread.getWhitelistCount() == 2;
            final String hash = feedItemData.files.getFiles(0).getFile().getHash(); //可取数据的ipfs路径
            String fileName = feedItemData.files.getFiles(0).getFile().getName();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fileHash", hash);
            jsonObject.put("fileName", fileName);
            String body = jsonObject.toJSONString();
            Log.d(TAG, "handleThreadUpdate: PICTURE: " + hash);
            String dialogimg = "";
            if (isSingle) { //单人的thread,图片就是对方的头像，不改
                dialogimg = tDialog.add_or_img;
            } else { //多人的要更新
                dialogimg = hash;
            }
            TDialog updateDialog = DBHelper.getInstance(getApplicationContext(), loginAccount).dialogGetMsg(tDialog, threadId,
                    feedItemData.files.getUser().getName() + "分享了图片", feedItemData.files.getDate().getSeconds(),
                    dialogimg);
            updateDialog.isRead = false;
            //插入msgs表
            int ismine = 0;
            if (feedItemData.files.getUser().getAddress().equals(myAddr)) {
                ismine = 1;
            }
            TMsg tMsg = DBHelper.getInstance(getApplicationContext(), loginAccount).insertMsg(
                    threadId, MsgType.MSG_PICTURE, feedItemData.files.getBlock(),
                    feedItemData.files.getUser().getAddress(),
                    body,
                    feedItemData.files.getDate().getSeconds(), ismine);
            EventBus.getDefault().post(updateDialog);
            EventBus.getDefault().post(tMsg);
        }

        if (feedItemData.type.equals(FeedItemType.FILES)) {
            String fileHash = feedItemData.files.getFiles(0).getFile().getHash();
            String fileName = feedItemData.files.getFiles(0).getFile().getName();
            Log.d(TAG, "handleThreadUpdate: 1:" + feedItemData.block);
            Log.d(TAG, "handleThreadUpdate: 2: " + feedItemData.files.getBlock());
            int ismine = 0;
            if (feedItemData.files.getUser().getAddress().equals(myAddr)) {
                ismine = 1;
            } else {
                Textile.instance().files.content(fileHash, new Handlers.DataHandler() {
                    @Override
                    public void onComplete(byte[] data, String media) {
                        ShareUtil.storeSyncFile(data, fileName);
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
            }
            Log.d(TAG, "handleThreadUpdate: fileHash: " + fileHash);
            String body = fileHash + "##" + fileName + "##" + feedItemData.block;
            TMsg tMsg = DBHelper.getInstance(getApplicationContext(), loginAccount).insertMsg(
                    threadId, MsgType.MSG_FILE, feedItemData.files.getBlock(),
                    feedItemData.files.getUser().getAddress(),
                    body,
                    feedItemData.files.getDate().getSeconds(), ismine);
            TDialog updateDialog = DBHelper.getInstance(getApplicationContext(), loginAccount).dialogGetMsg(
                    tDialog, threadId, feedItemData.files.getUser().getName() + "分享了文件",
                    feedItemData.files.getDate().getSeconds(), tDialog.add_or_img);
            EventBus.getDefault().post(updateDialog);
            EventBus.getDefault().post(tMsg);
        }

        if (feedItemData.type.equals(FeedItemType.STREAMMETA)) { //得到stream
            Model.StreamMeta meta = feedItemData.feedStreamMeta.getStreammeta();
            Log.d(TAG, "handleThreadUpdates: =====收到stream: type:" + meta.getType()+" ===streamId:"+meta.getId());
            int ismine = 0;
            if (feedItemData.feedStreamMeta.getUser().getAddress().equals(myAddr)) {
                ismine = 1;
            }
            if (feedItemData.feedStreamMeta.getStreammeta().getType().equals(Model.StreamMeta.Type.PICTURE)) { //stream图片
                String streamId = feedItemData.feedStreamMeta.getStreammeta().getId();
                // 接收图片就自动订阅，notification中收到实际文件
                if (ismine == 0) {
                    synchronized (SHOW_LOCK) {
                        if (metaNotiMap.containsKey(streamId)) { //如果其中有metaNoti,就是已经存了notification,就取出notification来显示
                            Model.Notification notification = metaNotiMap.get(streamId).notification;
                            Textile.instance().streams.dataAtStreamFile(feedItemData.feedStreamMeta, streamId, new Handlers.DataHandler() {
                                @Override
                                public void onComplete(byte[] data, String media) {
                                    String cachePath = ShareUtil.cacheImg(data, streamId);
                                    Log.d(TAG, "onComplete: stream picture:" + streamId);
                                    ShareUtil.saveImage(data, System.currentTimeMillis() + ".jpg");

                                    JSONObject jsonObject=new JSONObject();
                                    jsonObject.put("filePath",cachePath);
                                    jsonObject.put("fileSize",data.length);
                                   // jsonObject.put("duration",Textile.instance().streams.getStreamDuration(streamId));

                                    TMsg tMsg = DBHelper.getInstance(getApplicationContext(), loginAccount).insertMsg(
                                            threadId, MsgType.MSG_STREAM_PICTURE, feedItemData.block,
                                            feedItemData.feedStreamMeta.getUser().getAddress(),
                                            jsonObject.toJSONString(),
                                            feedItemData.feedStreamMeta.getDate().getSeconds(), 0);
                                    EventBus.getDefault().post(tMsg);
                                    metaNotiMap.remove(streamId);
                                }

                                @Override
                                public void onError(Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            return;
                        } else { //如果没有metaNoti,就存到map
                            metaNotiMap.put(streamId, new MetaAndNotification(threadUpdateWork, null));
                        }
                    }
                }
            }
            if (feedItemData.feedStreamMeta.getStreammeta().getType().equals(Model.StreamMeta.Type.FILE)) {
                String streamId = meta.getId();
                if (ismine == 0) {
                    synchronized (SHOW_LOCK) {
                        String fileNameInit= "receiving";
                        if (metaNotiMap.containsKey(streamId)) { //如果其中有metaNoti,就是已经存了notification, 小文件和旧文件会出现这种情况
                            Model.Notification notification = metaNotiMap.get(streamId).notification;

                            String jsonStr = notification.getSubjectDesc();
                            Log.d(TAG, "notificationReceived: stream file getSubject: " + jsonStr);
                            JSONObject jsonObject = JSON.parseObject(jsonStr);
                            final String msgFileName = jsonObject.getString("fileName");
                            fileNameInit = msgFileName;

                            Textile.instance().streams.tmpFilePathAtStream(feedItemData.feedStreamMeta, streamId, new Handlers.PathHandler() {
                                @Override
                                public void onComplete(String tmpFilePath, String media) {
                                    File file = new File(tmpFilePath);
                                    Log.d(TAG, "onComplete: 收到meta时成功拿到文件：" + file.length() + " " + tmpFilePath);

                                    ShareUtil.storeBigFile(tmpFilePath, msgFileName);

                                    JSONObject jsonObject=new JSONObject();
                                    jsonObject.put("streamId",streamId);
                                    jsonObject.put("fileSize",file.length());
                                   // jsonObject.put("duration",Textile.instance().streams.getStreamDuration(streamId));
                                    jsonObject.put("fileName",msgFileName);

                                    TMsg tMsg = DBHelper.getInstance(getApplicationContext(), loginAccount).insertMsg(
                                            threadId, MsgType.MSG_STREAM_FILE, feedItemData.block,
                                            feedItemData.feedStreamMeta.getUser().getAddress(),
                                            jsonObject.toJSONString(), feedItemData.feedStreamMeta.getDate().getSeconds(), 0);

                                    EventBus.getDefault().post(tMsg);
                                    metaNotiMap.remove(streamId);
                                }
                            });
                            return;
                        } else { //如果没有metaNoti,就存到map,并显示灰色文件meta
                            metaNotiMap.put(streamId, new MetaAndNotification(threadUpdateWork, null));
                        }

                        //反正收到就显示圈圈,不用插入数据库
                        TMsg tMsgStart = new TMsg(feedItemData.block, threadId, MsgType.MSG_FILE_START, feedItemData.feedStreamMeta.getUser().getName(), fileNameInit, feedItemData.feedStreamMeta.getDate().getSeconds(), false);
                        EventBus.getDefault().post(tMsgStart);
                    }
                }
            }

            if (feedItemData.feedStreamMeta.getStreammeta().getType().equals(Model.StreamMeta.Type.VIDEO)) { //stream视频
                if (ismine == 0) {
                    String streamId = feedItemData.feedStreamMeta.getStreammeta().getId();
                    String posterId = feedItemData.feedStreamMeta.getStreammeta().getPosterid();

                    String body = posterId + "##" + streamId;
                    TMsg tMsg = DBHelper.getInstance(getApplicationContext(), loginAccount).insertMsg( // stream视频就是将缩略图hash和streamid放入消息，并设置消息的类型为2
                            threadId, MsgType.MSG_STREAM_VIDEO, feedItemData.feedStreamMeta.getBlock(),
                            feedItemData.feedStreamMeta.getUser().getAddress(), body,
                            feedItemData.feedStreamMeta.getDate().getSeconds(), ismine);
                    Log.d(TAG, "onComplete: postMsg消息");
                    EventBus.getDefault().post(tMsg);
                }
            }
        }

        if (feedItemData.type.equals(FeedItemType.VIDEO)) {
            Model.Video video = feedItemData.feedVideo.getVideo();
            int ismine = 0;
            if (feedItemData.feedVideo.getUser().getAddress().equals(myAddr)) {
                ismine = 1;
            }

            TDialog updateDialog = DBHelper.getInstance(getApplicationContext(), loginAccount).dialogGetMsg(
                    tDialog, threadId, feedItemData.feedVideo.getUser().getName() + "分享了视频",
                    feedItemData.feedVideo.getDate().getSeconds(), tDialog.add_or_img);
            EventBus.getDefault().post(updateDialog);

            if (ismine == 0) {
                String posterHash = video.getPoster();
                String videoId = video.getId();
                String body = posterHash + "##" + videoId;
                Log.d(TAG, "threadUpdateReceived: getVideo: " + videoId + " " + posterHash);
                TMsg tMsg = DBHelper.getInstance(getApplicationContext(), loginAccount).insertMsg( // ticket类型的视频，就将缩略图hash和videoid放入，设置消息类型为4
                        threadId, 4, feedItemData.feedVideo.getBlock(),
                        feedItemData.feedVideo.getUser().getAddress(), body,
                        feedItemData.feedVideo.getDate().getSeconds(), ismine);
                EventBus.getDefault().post(tMsg);
            }
        }

        if (feedItemData.type.equals(FeedItemType.SIMPLEFILE)) { // 本质上就是ipfs文件
            String[] fileHashOrigin = feedItemData.feedSimpleFile.getSimpleFile().getPath().split("/");
            String fileHash = fileHashOrigin[fileHashOrigin.length - 1];
            String fileName = feedItemData.feedSimpleFile.getSimpleFile().getName();

            if (feedItemData.feedSimpleFile.getSimpleFile().getType().equals(Model.SimpleFile.Type.PICTURE)) {
                Textile.instance().ipfs.dataAtFeedSimpleFile(feedItemData.feedSimpleFile, new Handlers.DataHandlerWithTime() {
                    @Override
                    public void onComplete(byte[] data, String media, long duration) {
                        String a = ShareUtil.cacheImg(data, fileHash);
                        int isMine=1;
                        if (!feedItemData.feedSimpleFile.getUser().getAddress().equals(myAddr)) { //收到他人的消息才下载图片
                            ShareUtil.saveImage(data, fileName);
                            isMine=0;
                        }
                        Log.d(TAG, "onComplete simple picture:" + " " + a);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("fileHash", fileHash);
                        jsonObject.put("fileName", fileName);
                        jsonObject.put("block", feedItemData.block);
                        Log.d(TAG, "onComplete: 收到block："+feedItemData.feedSimpleFile.getBlock());
                        jsonObject.put("dataLength", data.length);
                        jsonObject.put("duration",duration);
//                        String body = fileHash + "##" + fileName + "##" + feedItemData.block + "##" +data.length;
                        TMsg tMsg = DBHelper.getInstance(getApplicationContext(), loginAccount).insertMsg(
                                threadId, MsgType.MSG_SIMPLE_PICTURE, feedItemData.feedSimpleFile.getBlock(),
                                feedItemData.feedSimpleFile.getUser().getAddress(),
                                jsonObject.toJSONString(),
                                feedItemData.feedSimpleFile.getDate().getSeconds(), isMine);
                        EventBus.getDefault().post(tMsg);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            } else {
                if (!feedItemData.feedSimpleFile.getUser().getAddress().equals(myAddr)) { // 接收到他人消息先显示圈圈
                    TMsg tMsgStart = new TMsg(feedItemData.feedSimpleFile.getBlock(), threadId, MsgType.MSG_FILE_START, feedItemData.feedSimpleFile.getUser().getName(), fileName, feedItemData.feedSimpleFile.getDate().getSeconds(), false);
                    EventBus.getDefault().post(tMsgStart);
                }
                Textile.instance().ipfs.pathAtSimpleFile(feedItemData.feedSimpleFile, new Handlers.PathHandlerWithTime() {
                    @Override
                    public void onComplete(String tmpFilePath, String media, long duration) {
                        int isMine=1;
                        if (!feedItemData.feedSimpleFile.getUser().getAddress().equals(myAddr)) { //收到他人的消息才下载图片
                            ShareUtil.storeBigFile(tmpFilePath, fileName);
                            isMine=0;
                        }
//                        String body = fileHash + "##" + fileName + "##" + feedItemData.block + "##" +new File(tmpFilePath).length();
                        JSONObject jsonObject=new JSONObject();
                        jsonObject.put("fileHash",fileHash);
                        jsonObject.put("fileName",fileName);
                        jsonObject.put("block",feedItemData.feedSimpleFile.getBlock());
                        jsonObject.put("dataLength", new File(tmpFilePath).length());
                        jsonObject.put("duration",duration);

                        TMsg tMsg = DBHelper.getInstance(getApplicationContext(), loginAccount).insertMsg(
                                threadId, MsgType.MSG_SIMPLE_FILE, feedItemData.feedSimpleFile.getBlock(),
                                feedItemData.feedSimpleFile.getUser().getAddress(),
                                jsonObject.toJSONString(),
                                feedItemData.feedSimpleFile.getDate().getSeconds(), isMine);
                        EventBus.getDefault().post(tMsg);
                    }
                });
            }
        }
    }

    public void handleThread2Update(Thread2UpdateWork thread2UpdateWork){
        Thread2Data thread2Data = thread2UpdateWork.thread2Data;
        String threadId = thread2Data.threadId;
        TDialog tDialog = DBHelper.getInstance(getApplicationContext(), loginAccount).queryDialogByThreadID(threadId);//根据threadid从数据库中查询群组
        //System.out.println("**********thread2 update type:"+thread2Data.collection);
        String myAddr = Textile.instance().account.address();
        if (thread2Data.collection.equals("GroupInfo")){
            System.out.println("**********Handle Thread2 group update  ");
            if (tDialog!=null){//如果这个thread不是第一次出现，则是对这个thread的修改，需修改数据库

            }else{//否则是这个thread第一次出现，需将其加入数据库
                boolean authorIsMe = thread2Data.groupInstance.groupCreator.equals(myAddr); //表明是否是自己创建的
                TDialog newDialog = DBHelper.getInstance(getApplicationContext(), loginAccount).insertDialog(
                        threadId,
                        "你好啊，现在我们已经成为好友了",
                        thread2Data.groupInstance.createdTime,
                        0, //后台收到默认是未读的
                        "",
                         false, 1);//默认为群组
                EventBus.getDefault().post(newDialog);
            }
        }
        if (thread2Data.collection.equals("GroupMember")){
            System.out.println("**********Handle Thread2 member update  ");
        }
        if (thread2Data.collection.equals("GroupMessage")) {
            if (thread2Data.messageInstance.type.equals("TEXT_MESSAGE_THREAD2")) {
                System.out.println("********** Handle Thread2 message TEXT update  ");
                int ismine = 0;
                if (thread2Data.messageInstance.sender.equals(myAddr)) { // 是我自己的消息
                    ismine = 1;
                }
                //插入msgs表
                TMsg tMsg = DBHelper.getInstance(getApplicationContext(), loginAccount).insertMsg(
                        threadId, MsgType.MSG_TEXT, thread2Data.instanceId,
                        thread2Data.messageInstance.sender,
                        thread2Data.messageInstance.content,
                        thread2Data.messageInstance.sendTime, ismine);

                //更新dialogs表
                TDialog updateDialog = DBHelper.getInstance(getApplicationContext(), loginAccount).dialogGetMsg(tDialog, threadId,
                        thread2Data.messageInstance.content, thread2Data.messageInstance.sendTime,
                        tDialog.add_or_img);
                tDialog.isRead = false;

                EventBus.getDefault().post(updateDialog);
                EventBus.getDefault().post(tMsg); //我的消息也要广播，所有的消息的显示都不要从本地来，而是后发送，本地其实还是很快的。

            }

            if (thread2Data.messageInstance.type.equals("PICTURE_MESSAGE_THREAD2")){
                System.out.println("********** Handle Thread2 message PICTURE update  ");
                String picMessage = thread2Data.messageInstance.content;
                String picPath= "";
                String picName = "";
                String picSize = "";
                try {//解析收到的xml
                    Document doc;
                    doc = DocumentHelper.parseText(picMessage);
                    Element rootElt = doc.getRootElement();
                    picPath = rootElt.elementText("path");
                    picName = rootElt.elementText("name");
                    picSize = rootElt.elementText("size");
                }catch (DocumentException e) {
                    e.printStackTrace();
                }
                TDialog updateDialog = DBHelper.getInstance(getApplicationContext(), loginAccount).dialogGetMsg(tDialog, threadId,
                        thread2Data.messageInstance.sender + "分享了图片", thread2Data.messageInstance.sendTime,
                        picPath);
                updateDialog.isRead = false;

                int ismine = 0;
                if (thread2Data.messageInstance.sender.equals(myAddr)) {
                    ismine = 1;
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("fileHash", picPath);
                jsonObject.put("fileName", picName);
                String body = jsonObject.toJSONString();

                TMsg tMsg = DBHelper.getInstance(getApplicationContext(), loginAccount).insertMsg(
                        threadId, MsgType.MSG_PICTURE, thread2Data.instanceId,
                        thread2Data.messageInstance.sender,
                        body,
                        thread2Data.messageInstance.sendTime, ismine);
                EventBus.getDefault().post(updateDialog);
                EventBus.getDefault().post(tMsg);
            }

        }
    }

    class FileTransInfo {
        public String peerkey;
        public long gettime;

        public FileTransInfo(String a, long b) {
            peerkey = a;
            gettime = b;
        }
    }

    class ShareListener extends BaseTextileEventListener {

        @Override
        public void nodeOnline() {
            super.nodeOnline();

            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("textileon", true);
            editor.commit();

            EventBus.getDefault().post(Integer.valueOf(0));

            try {
                Textile.instance().profile.setName(myname);
                if (avatarpath != null) {
                    Textile.instance().profile.setAvatar(avatarpath, new Handlers.BlockHandler() {
                        @Override
                        public void onComplete(Model.Block block) {
                            Log.d(TAG, "onComplete: Shareit注册设置头像成功");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.d(TAG, "onError: ShareIt注册设置头像失败");
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ShareUtil.createDeviceThread();

            try {
//                Textile.instance().ipfs.swarmConnect("/ip4/192.168.3.2/tcp/40102/ipfs/12D3KooWQS76E9Xgp8zzfhma24R7tUJQos4kY5FYguavWWLm3nkD");
//                Textile.instance().ipfs.swarmConnect("/ip4/192.168.3.6/tcp/40102/ipfs/12D3KooWQS76E9Xgp8zzfhma24R7tUJQos4kY5FYguavWWLm3nkD");
                boolean s2 = Textile.instance().ipfs.swarmConnect("/ip4/202.120.38.131/tcp/4001/ipfs/12D3KooWDmfnG1cJmP4vwee2Cu7Gd3eSfAv7ZWF3YKHrtsKbVZXS");
//                boolean s3=Textile.instance().ipfs.swarmConnect("/ip4/202.120.38.100/tcp/4001/ipfs/QmZt8jsim548Y5UFN24GL9nX9x3eSS8QFMsbSRNMBAqKBb");
                Log.d(TAG, "nodeOnline: swarmConnect 131: " + s2);
            } catch (Exception e) {
                e.printStackTrace();
            }

//            Textile.instance().connectShadowRelay("12D3KooWECMPH8VuXm4tC43HePXhfkZqZJH5CBLDaMvmSgChaHzq");

            Textile.instance().streams.setSpeedIntervalMillis(100);

            //connect cafe
            Log.d(TAG, "nodeOnline: connectcafe:" + connectCafe);
            if (connectCafe) {
                CafeUtil.connectCafe(new Handlers.ErrorHandler() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: cafe连接成功1");
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("ok131", true);
                        editor.putBoolean("connectCafe", true);
                        editor.commit();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "onError: cafe连接失败1");
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("ok131", false);
                        editor.commit();
                    }
                });
            }

            // connect shadow
            String tmp = pref.getString("shadowPeer", "");
            Log.d(TAG, "drwaUI: swarm peer: " + tmp);
            if (!tmp.equals("")) {
                String[] addrs = tmp.split("/");
                Textile.instance().connectShadowRelay(addrs[addrs.length - 1]);
            }
        }

        private void processStreamFileNotification(Model.Notification notification){
            if (notification.getBody().equals("stream video")) {
                return;
            }
            String streamId = notification.getSubject();
            synchronized (SHOW_LOCK) {
                if (metaNotiMap.containsKey(streamId)) { //如果已经有meta,就进行显示
                    String fileId = notification.getBlock(); // 存的是stream id
                    Log.d(TAG, "notificationReceived: have meta, get root: " + streamId);
                    View.FeedStreamMeta feedStreamMeta = metaNotiMap.get(streamId).threadUpdateWork.feedItemData.feedStreamMeta;
                    final String tmpThreadId = metaNotiMap.get(streamId).threadUpdateWork.thread.getId();
                    final String tmpBlock = feedStreamMeta.getBlock();
                    final String tmpAuthor = feedStreamMeta.getUser().getAddress();
                    final long tmpSendTime = feedStreamMeta.getDate().getSeconds();

                    if (feedStreamMeta.getStreammeta().getType().equals(Model.StreamMeta.Type.FILE)) { //如果是文件
                        //拿到文件名
                        String jsonStr = notification.getSubjectDesc();
                        JSONObject jsonObject = JSON.parseObject(jsonStr);
                        String msgFileName = jsonObject.getString("fileName");
                        Textile.instance().streams.tmpFilePathAtStream(feedStreamMeta, streamId, new Handlers.PathHandler() {
                            @Override
                            public void onComplete(String tmpFilePath, String media) {
                                File file = new File(tmpFilePath);
                                Log.d(TAG, "onComplete: notification中成功拿到文件：" + file.length() + " " + tmpFilePath);
                                String filePath = ShareUtil.storeBigFile(tmpFilePath, msgFileName);

                                JSONObject jsonObject=new JSONObject();
                                jsonObject.put("streamId",streamId);
                                jsonObject.put("fileSize",file.length());
                               // jsonObject.put("duration",Textile.instance().streams.getStreamDuration(streamId));
                                jsonObject.put("fileName",msgFileName);
                                TMsg tMsg = DBHelper.getInstance(getApplicationContext(), loginAccount).insertMsg(
                                        tmpThreadId, 8, tmpBlock, tmpAuthor,
                                        jsonObject.toJSONString(), tmpSendTime, 0);
                                EventBus.getDefault().post(tMsg);
                                Log.d(TAG, "noti onComplete: stream file get: " + filePath);
                                metaNotiMap.remove(streamId);
                            }
                        });
                        return;
                    } else if (feedStreamMeta.getStreammeta().getType().equals(Model.StreamMeta.Type.PICTURE)) {
                        Textile.instance().streams.dataAtStreamFile(feedStreamMeta, streamId, new Handlers.DataHandler() {
                            @Override
                            public void onComplete(byte[] data, String media) {
                                String cachePath = ShareUtil.cacheImg(data, streamId);
                                Log.d(TAG, "onComplete: notification中拿到stream picture:" + cachePath);
                                ShareUtil.saveImage(data, System.currentTimeMillis() + ".jpg");

                                JSONObject jsonObject=new JSONObject();
                                jsonObject.put("filePath",cachePath);
                                jsonObject.put("fileSize",data.length);
                              //  jsonObject.put("duration",Textile.instance().streams.getStreamDuration(streamId));

                                TMsg tMsg = DBHelper.getInstance(getApplicationContext(), loginAccount).insertMsg(
                                        tmpThreadId, 7, tmpBlock, tmpAuthor,
                                        jsonObject.toJSONString(), tmpSendTime, 0);
                                EventBus.getDefault().post(tMsg);
                                metaNotiMap.remove(streamId);
                            }

                            @Override
                            public void onError(Exception e) {
                                e.printStackTrace();
                            }
                        });
                        return;
                    }
                } else {
                    metaNotiMap.put(streamId, new ShareService.MetaAndNotification(null, notification));
                }
            }
        }

        private void processRecordNotification(Model.Notification notification){
            Log.d(TAG, "notificationReceived: from: " + notification.getUser().getAddress());
            if (notification.getSubject().equals("shadowDone")) {
                Log.d(TAG, "notificationReceived: shadowDone");
                String done = "shadowDone";
                EventBus.getDefault().post(done);
                return;
            }
            String Id = notification.getBlock();
            if (notification.getSubject().equals("ipfsGet")) {
                LinkedList<ShareService.FileTransInfo> list = null;
                if (!recordTmp.containsKey(Id)) {
                    list = new LinkedList<>();
                } else {
                    list = recordTmp.get(Id);
                }
                long gett1 = notification.getDate().getSeconds() * 1000 + (notification.getDate().getNanos() / 1000000);
                ShareService.FileTransInfo fileTransInfo = new ShareService.FileTransInfo(notification.getActor(), gett1);
                list.add(fileTransInfo);
                Log.d(TAG, "notificationReceived: ipfsGet,sec,nanosec: " + gett1 + " " + Id);
                recordTmp.put(Id, list);
                Log.d(TAG, "notificationReceived: list size:" + list.size());
            } else if (notification.getSubject().equals("ipfsDone")) {
                LinkedList<ShareService.FileTransInfo> l = recordTmp.get(Id);
                TRecord tRecord = null;
                if (l == null) { // stream方式只有done，没有get
                    JSONObject object = JSON.parseObject(notification.getBody());
                    String parent = object.getString("Parent");
                    String dura = object.getString("Duration");
                    long dural = Long.parseLong(dura);
                    // 查出来最大的t2，使用t2.
                    long maxt2=DBHelper.getInstance(getApplicationContext(), loginAccount).getLastStreamRecord(Id);
                    tRecord = new TRecord(Id, notification.getActor(), dural, 0, System.currentTimeMillis(), 1, parent);
                    DBHelper.getInstance(getApplicationContext(), loginAccount).recordGet(tRecord.cid, tRecord.recordFrom, tRecord.t1, maxt2, tRecord.t3, parent);
                    Log.d(TAG, "notificationReceived: cid, get1, get2: " + tRecord.cid + " " + tRecord.t1 + " " + 0);
                } else { // ticket方式之前存过get
                    int i = 0;
                    for (; i < l.size(); i++) {
                        if (l.get(i).peerkey.equals(notification.getActor())) { //找到那个人的get1
                            long get1 = l.get(i).gettime;
                            Log.d(TAG, "notificationReceived: get1: " + get1 + " " + i);
                            long get2 = notification.getDate().getSeconds() * 1000 + (notification.getDate().getNanos() / 1000000);
                            Log.d(TAG, "notificationReceived: ipfsDone,sec,nanosec: " + get2);
                            tRecord = new TRecord(Id, notification.getActor(), get1, get2, System.currentTimeMillis(), 1, "");
                            DBHelper.getInstance(getApplicationContext(), loginAccount).recordGet(tRecord.cid, tRecord.recordFrom, get1, get2, tRecord.t3, "");
                            Log.d(TAG, "notificationReceived: cid, get1, get2: " + tRecord.cid + " " + get1 + " " + get2);
                            break;
                        }
                    }
                    l.remove(i);
                }
                EventBus.getDefault().post(tRecord);
            }

            Log.d(TAG, "notificationReceived: block json: " + notification.getBlock());
        }

        private void processStreamTimeout(Model.Notification notification){
//            if (notification.getType().equals(Model.Notification.Type.INFORM_TIMEOUT)) {
//                String streamId = notification.getSubject();
//                synchronized (SHOW_LOCK) {
//                    if (metaNotiMap.containsKey(streamId)) { //如果已经有meta,就进行显示
//                        Log.d(TAG, "notificationReceived: have meta, get root: " + streamId);
//                        View.FeedStreamMeta feedStreamMeta = metaNotiMap.get(streamId).threadUpdateWork.feedItemData.feedStreamMeta;
//                        final String tmpThreadId = metaNotiMap.get(streamId).threadUpdateWork.thread.getId();
//                        final String tmpBlock = feedStreamMeta.getBlock();
//                        final String tmpAuthor = feedStreamMeta.getUser().getAddress();
//                        final long tmpSendTime = feedStreamMeta.getDate().getSeconds();
//
//                        if (feedStreamMeta.getStreammeta().getType().equals(Model.StreamMeta.Type.FILE)) { //如果是文件
//                            //拿到文件名
////                            String jsonStr=notification.getSubjectDesc();
//                            String jsonStr = feedStreamMeta.getStreammeta().getCaption();
//                            Log.d(TAG, "notificationReceived: stream getSubject: " + jsonStr);
//                            JSONObject jsonObject = JSON.parseObject(jsonStr);
//                            String msgFileName = jsonObject.getString("fileName");
//                            Textile.instance().streams.dataAtStreamFile(feedStreamMeta, streamId, new Handlers.DataHandler() {
//                                @Override
//                                public void onComplete(byte[] data, String media) {
//                                    String filePath = ShareUtil.storeSyncFile(data, msgFileName);
//                                    String msgBody = streamId + "##" + msgFileName;
////                                    TMsg tMsg=DBHelper.getInstance(getApplicationContext(),loginAccount).insertMsg(
////                                            tmpThreadId,8,tmpBlock,tmpAuthor,
////                                            msgBody,tmpSendTime,0);
//                                    TMsg tMsg = new TMsg(feedStreamMeta.getBlock(), tmpThreadId, 8, tmpAuthor, msgBody, tmpSendTime, false);
//                                    DBHelper.getInstance(getApplicationContext(), loginAccount).updateMsg(feedStreamMeta.getBlock(), msgBody);
//
//                                    EventBus.getDefault().post(tMsg);
//                                    Log.d(TAG, "noti onComplete: stream file get: " + filePath);
//                                    metaNotiMap.remove(streamId);
//                                }
//
//                                @Override
//                                public void onError(Exception e) {
//                                    Log.d(TAG, "onError: dddddddddddd");
//                                    e.printStackTrace();
//                                }
//                            });
//                            return;
//                        } else if (feedStreamMeta.getStreammeta().getType().equals(Model.StreamMeta.Type.PICTURE)) {
//                            Textile.instance().streams.dataAtStreamFile(feedStreamMeta, streamId, new Handlers.DataHandler() {
//                                @Override
//                                public void onComplete(byte[] data, String media) {
//                                    String cachePath = ShareUtil.cacheImg(data, streamId);
//                                    Log.d(TAG, "onComplete: stream picture:" + cachePath);
//                                    ShareUtil.saveImage(data, System.currentTimeMillis() + ".jpg");
//                                    TMsg tMsg = DBHelper.getInstance(getApplicationContext(), loginAccount).insertMsg(
//                                            tmpThreadId, 7, tmpBlock, tmpAuthor,
//                                            cachePath, tmpSendTime, 0);
//                                    EventBus.getDefault().post(tMsg);
//                                }
//
//                                @Override
//                                public void onError(Exception e) {
//                                    e.printStackTrace();
//                                }
//                            });
//                            return;
//                        }
//                    } else {
//                        Log.d(TAG, "notificationReceived: noti no meta");
////                        metaNotiMap.put(streamId,new MetaAndNotification(null,notification));
//                    }
//                }
//            }
        }

        @Override
        public void notificationReceived(Model.Notification notification) {
            Log.d(TAG, "notificationReceived, ========== body:" + notification.getBody() + " ============type:" + notification.getType() + " ==============subject:" + notification.getSubject());
            if (notification.getUser().getAddress().equals(Textile.instance().account.address())) {
                Log.d(TAG, "notificationReceived: 自己的notification");
                return;
            }

            if(!notification.getType().equals(Model.Notification.Type.RECORD_REPORT)) {
                if (notification.getBody().equals("")) {
                    Log.d(TAG, "notificationReceived: 空body");
                    return;
                }
            }

            switch (notification.getType()){
                case STREAM_FILE:
                    processStreamFileNotification(notification);
                    break;
                case RECORD_REPORT:
                    processRecordNotification(notification);
                    break;
                case INFORM_TIMEOUT:
                    processStreamTimeout(notification);
                    break;
                default:
                    //查出邀请中最近的一个，添加到头部。
                    int gpinvite = 0;
                    sjtu.opennet.textilepb.View.InviteView lastInvite = null;
                    try {
                        if (Textile.instance().invites != null) {
                            List<View.InviteView> invites = Textile.instance().invites.list().getItemsList();
                            for (sjtu.opennet.textilepb.View.InviteView v : invites) { //遍历所有的邀请
                                if (!v.getName().equals("FriendThread1219")) { //只要群组名不等于这个那就是好友邀请
                                    gpinvite++;
                                    lastInvite = v;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (gpinvite > 0) { //如果有群组邀请就要显示出来
//                TDialog noti=new TDialog("",lastInvite.getInviter().getName()+" 邀请你",
//                        lastInvite.getDate().getSeconds(),false,"tongzhi",true,true);
//                EventBus.getDefault().post(noti);
                        try {
                            Textile.instance().notifications.acceptInvite(notification.getId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            }
        }

        @Override
        public void contactQueryResult(String queryId, Model.Contact contact) {
            EventBus.getDefault().post(contact);
        }

        @Override
        public void threadAdded(String threadId) {
            EventBus.getDefault().post(threadId); //只在添加联系人的时候起作用，创建群组的时候要过滤掉
        }

        @Override
        public void threadUpdateReceived(String threadId, FeedItemData feedItemData) {
            synchronized (LOCK) {
                if (lastBlock.equals(feedItemData.block)) {
                    return;
                } else {
                    lastBlock = feedItemData.block;
                }
            }
            Model.Thread thread = null;
            try {
                thread = Textile.instance().threads.get(threadId);
                if ((thread != null) && thread.getSharing().equals(Model.Thread.Sharing.NOT_SHARED)) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Message msg = threadUpdateHandler.obtainMessage();
            msg.obj = new ShareService.ThreadUpdateWork(thread, feedItemData);
            threadUpdateHandler.sendMessage(msg);
        }

        @Override
        public void thread2UpdateReceived(String threadId, Thread2Data thread2Data) {
            Message msg = thread2UpdateHandler.obtainMessage();
            msg.obj = new ShareService.Thread2UpdateWork(thread2Data);
            thread2UpdateHandler.sendMessage(msg);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void shutDown(Integer stop) {
        if (stop == 943) {
            Log.d(TAG, "shutDown: 服务stop");
            Textile.instance().destroy();
            serviceOn = false;
            stopForeground(true);
            stopSelf();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startTextile(Integer stop) {
        if (stop == 2562) {
            new Thread() {
                @Override
                public void run() {
                    launchTextile();
                }
            }.start();
        }
    }

    private void joinDefaultThread(){
//            if (ShareUtil.getThreadByName("default") == null) {
//                try {
//                    Textile.instance().invites.acceptExternal("QmdocmhxFuJ6SdGMT3Arh5wacWnWjZ52VsGXdSp6aXhTVJ","2NfdMrvABwHorxeJxSckSkBKfBJMMF4LqGwdmjY5ZCKw8TDpfHYxELbWnNhed");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
    }
}
