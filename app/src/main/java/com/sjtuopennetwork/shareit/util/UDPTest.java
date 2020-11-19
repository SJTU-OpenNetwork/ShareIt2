package com.sjtuopennetwork.shareit.util;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;


public class UDPTest {

    // 数据报套接字
    static private DatagramSocket datagramSocket;
    // 用以接收数据报
    static private DatagramPacket datagramPacket;
    static byte[] buf = new byte[65530];

    static void startListen(){
        new Thread(){
            @Override
            public void run() {

                try {
                    datagramSocket = new DatagramSocket(9000);
                    System.out.println("Server Start and listenering 9000");
                } catch (SocketException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }

                int ii=0;
                while (true){
                    byte[] receiveData = new byte[128];
                    datagramPacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        datagramSocket.receive(datagramPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int plength = datagramPacket.getLength();
                    System.out.println(new String(datagramPacket.getData()));
//                    Multicast.packet multiPacket = null;
//                    try {
//                        multiPacket = Multicast.packet.parseFrom(pByte);
//                    } catch (InvalidProtocolBufferException e) {
//                        e.printStackTrace();
//                    }
//                    if (multiPacket.getPacketType() == 3) {
//                        System.out.println(multiPacket.getIndex());
//                        continue;
//                    }
                }
            }
        }.start();
    }
}
