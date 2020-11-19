package sjtu.opennet.erasure;

import java.util.Comparator;

public class Shard {
    public int index;
    public int gid;
    public byte[] data;
    public Shard(int gid, int shardIndex, byte[] shardData){
        this.gid = gid;
        index = shardIndex;
        data = shardData;
    }
}

class ShardComparator implements Comparator<Shard> {
    @Override
    public int compare(Shard s1, Shard s2) {
        return Integer.compare(s1.index, s2.index);
    }
}
