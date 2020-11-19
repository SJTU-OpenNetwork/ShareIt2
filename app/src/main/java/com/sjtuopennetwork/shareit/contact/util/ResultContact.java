package com.sjtuopennetwork.shareit.contact.util;

public class ResultContact {
    public String address;
    public String name;
    public String avatarhash;
    public byte[] avatar;
    public boolean isMyFriend;

    public ResultContact(String address, String name, String avatarhash, byte[] avatar, boolean isMyFriend) {
        this.address = address;
        this.name = name;
        this.avatarhash=avatarhash;
        this.avatar = avatar;
        this.isMyFriend=isMyFriend;
    }
}
