package com.sjtuopennetwork.shareit.share.util;

public class TDialog {
    public String threadid;
    public String lastmsg;
    public long lastmsgdate;
    public boolean isRead;
    public String add_or_img;
    public boolean isSingle; //标记是否是两人的thread
    public boolean isVisible; //如果是两人的thread，删除只是设置不可见，下一次发消息之后再次出现

    public TDialog(String threadid, String lastmsg, long lastmsgdate, boolean isRead, String add_or_img, boolean isSingle, boolean isVisible) {
        this.threadid = threadid;
        this.lastmsg = lastmsg;
        this.lastmsgdate = lastmsgdate;
        this.isRead = isRead;
        this.add_or_img = add_or_img;
        this.isSingle = isSingle;
        this.isVisible = isVisible;
    }
}
