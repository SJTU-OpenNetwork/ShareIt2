package sjtu.opennet.erasure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ShardList {
    public int SHARD_NUM;
    public int SHARD_SIZE;          // data length of each shard. not number of shards.
    public int PARITY_NUM;

    public LinkedList<Shard> shards;      // TODO: Try to make this private.
    private static ShardComparator comparator = new ShardComparator();

    public interface ShardIterator {
        void onEach(Shard s);
    }

    public ShardList(Shard[] _shards, int _SHARD_NUM, int _PARITY_NUM) throws Exception {
        if (_shards.length != _SHARD_NUM + _PARITY_NUM) {
            throw new Exception("shard number not match");
        }
        SHARD_NUM = _SHARD_NUM;
        PARITY_NUM = _PARITY_NUM;
        if (_shards.length > 0) {
            SHARD_SIZE = _shards[0].data.length;
        }

        shards = new LinkedList<>(Arrays.asList(_shards));

    }

    public ShardList(int _SHARD_NUM, int _PARITY_NUM) {
        SHARD_NUM = _SHARD_NUM;
        PARITY_NUM = _PARITY_NUM;
        shards = new LinkedList<Shard>();
    }

    public void add(Shard shard) throws Exception {
        if (shards.size() == 0) {
            SHARD_SIZE = shard.data.length;
        } else {
            if (SHARD_SIZE != shard.data.length) {
//                throw new Exception("SHARD_SIZE not match, need:"+SHARD_SIZE+" ,got:"+shard.data.length);
            }
        }
        shards.add(shard);
    }

    public int size() {
        return shards.size();
    }

    public void randomDrop(int dropSize) {
        Random rand = new Random(System.currentTimeMillis());
        if (dropSize > shards.size()) {
            dropSize = shards.size();
        }
        int tmp;
        for (int i=0; i<dropSize; i++) {
            tmp = rand.nextInt(shards.size());
            shards.remove(tmp);
        }
    }

    public void traverse(ShardIterator ite) {
        for (Shard s : shards) {
            ite.onEach(s);
        }
    }

    public LinkedList<Shard> getShards(){
        return shards;
    }

    public void shuffle() {
        Collections.shuffle(shards);
    }

    public void sort() {
        Collections.sort(shards, comparator);
    }
}

