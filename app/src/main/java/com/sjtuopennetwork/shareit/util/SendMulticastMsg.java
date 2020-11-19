package com.sjtuopennetwork.shareit.util;

import sjtu.opennet.hon.MulticastFile;

public class SendMulticastMsg {
    public int shardSize;
    public float sleepTime;
    public int speed;
    public int data;
    public int parity;
    public MulticastFile multicastFile;

    public SendMulticastMsg(int shardSize, float sleepTime, int speed, int data, int parity, MulticastFile multicastFile) {
        this.shardSize = shardSize;
        this.sleepTime = sleepTime;
        this.speed = speed;
        this.data = data;
        this.parity = parity;
        this.multicastFile = multicastFile;
    }
}
