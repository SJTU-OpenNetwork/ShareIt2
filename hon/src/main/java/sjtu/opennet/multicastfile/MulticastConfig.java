package sjtu.opennet.multicastfile;

public class MulticastConfig {
//    private int shardSize;
    private int shardNum;
    private int parityNum;
//    private float interval;
    private int speed;

    public MulticastConfig(int shardNum, int parityNum, int speed) {
//        this.shardSize = shardSize;
        this.shardNum = shardNum;
        this.parityNum = parityNum;
//        this.interval = interval;
        this.speed = speed;
    }

    public int getShardNum() {
        return shardNum;
    }

    public int getParityNum() {
        return parityNum;
    }

    public int getSpeed() {
        return speed;
    }
}
