package com.sjtuopennetwork.shareit.share.util;

public class TRecord {
    public int id;
    public String cid;
    public String recordFrom;
    public long t1;
    public long t2;
    public long t3;
    public int type;
    public String parent;

    public TRecord(String cid, String recordFrom, long t1, long t2, long t3, int type, String parent) {
        this.cid = cid;
        this.recordFrom = recordFrom;
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.type = type;
        this.parent = parent;
    }
}
