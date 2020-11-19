package com.sjtuopennetwork.shareit.share.util;

public class TMsg {
    public String blockid;
    public String threadid;
    public int msgType;
    public String author;
    public String body;
    public long sendtime;
    public boolean ismine;

    public TMsg(String blockid,String threadid, int msgType,  String author, String body, long sendtime, boolean ismine) {
        this.threadid = threadid;
        this.msgType = msgType;
        this.blockid = blockid;
        this.author = author;
        this.body = body;
        this.sendtime = sendtime;
        this.ismine = ismine;
    }
}
