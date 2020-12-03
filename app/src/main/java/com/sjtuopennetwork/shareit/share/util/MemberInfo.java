package com.sjtuopennetwork.shareit.share.util;

public class MemberInfo {
    public String name;
    public String avatar;
    public String address;
    public MemberInfo(String name,String avatar){
        this.name = name;
        this.avatar = avatar;
    }
    public MemberInfo(String name,String avatar,String address){
        this.name = name;
        this.avatar = avatar;
        this.address = address;
    }
}
