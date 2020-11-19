package sjtu.opennet.util;

public class BytesUtil {

    public static byte[] intToBytes(int a, int length) {
        byte[] bs = new byte[length];
        for (int i = bs.length - 1; i >= 0; i--) {
            bs[i] = (byte) (a % 0xFF);
            a = a / 0xFF;
        }
        return bs;
    }

    public static byte[] intToByteArr(int x){
        byte[] arr = new byte[4];
        arr[3]= (byte)(x & 0xff);
        arr[2]= (byte)(x>>8 & 0xff);
        arr[1]= (byte)(x>>16 & 0xff);
        arr[0]= (byte)(x>>24 & 0xff);
        return arr;
    }


    public static int bytesToInt(byte[] bs) {
        int a = 0;
        for (int i = bs.length - 1; i >= 0; i--) {
            a += bs[i] * Math.pow(0xFF, bs.length - i - 1);
        }
        return a;
    }


    public static int byteArrayToInt(byte[] bytes) {
        int value=0;
        for(int i = 0; i < 4; i++) {
            int shift= (3-i) * 8;
            value +=(bytes[i] & 0xFF) << shift;
        }
        return value;
    }
}
