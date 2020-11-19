package com.sjtuopennetwork.shareit.util.contactlist;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.promeg.pinyinhelper.Pinyin;

public class MyContactBean implements Parcelable {

    public String id;
    public String name;
    public String avatar;
    public boolean isChoose;
    public boolean checkEnable = true;

    //英文下标
    public int index = -1;
    public String letter;


    public MyContactBean(String id, String name, String avatar) {
        this.id = id;
        if(name.equals("")){
            this.name="null";
        }else{
            this.name = name;
        }
        this.avatar = avatar;
    }

    public MyContactBean(Parcel in){
        id = in.readString();
        name = in.readString();
        avatar = in.readString();
        isChoose = in.readByte() != 0;
        checkEnable = in.readByte() != 0;
        index =  in.readInt();
        letter= in.readString();
    }

    public static final Creator<MyContactBean> CREATOR = new Creator<MyContactBean>() {
        @Override
        public MyContactBean createFromParcel(Parcel source) {
            return new MyContactBean(source);
        }

        @Override
        public MyContactBean[] newArray(int size) {
            return new MyContactBean[size];
        }
    };

    public void setFields(String id, String name, String avatar) {
        this.id = id;
        this.avatar = avatar;
        if(name.equals("")){
            this.name="null";
        }else {
            this.name = name;
        }
        String upperCase = Pinyin.toPinyin(this.name.charAt(0)).toUpperCase();
        String value = String.valueOf(upperCase.charAt(0));
        if (!value.matches("[A-Z]")) {
            //如果不是A-Z字母开头
            value = "#";
        }
        this.letter=value;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MyContactBean){
            MyContactBean cobj = (MyContactBean) obj;
            return id.equals(cobj.id);
        }
        return super.equals(obj);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(avatar);
        dest.writeByte((byte) (isChoose ? 1 : 0));
        dest.writeByte((byte) (checkEnable ? 1 : 0));
        dest.writeInt(index);
        dest.writeString(letter);
    }
}
