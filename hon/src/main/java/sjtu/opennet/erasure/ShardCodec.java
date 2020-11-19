package sjtu.opennet.erasure;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Locale;

public class ShardCodec {
    private static final String TAG = "ShardCodec";
    private static final int BYTES_IN_INT = 4;  // Size of 4 byte int (32 bit int).
                                                // This integer is used to keep the size of original data
                                                // because the data would be padded during encode.
                                                // This integer is stored at the begin of first output shard.
                                                // The decoder use it to cut the decode result.
    private int SHARD_NUM;
    private int PARITY_NUM;

    private ReedSolomon codec;

    public ShardCodec(int _SHARD_NUM, int _PARITY_NUM) {
        SHARD_NUM = _SHARD_NUM;
        PARITY_NUM = _PARITY_NUM;
        codec = ReedSolomon.create(SHARD_NUM, PARITY_NUM);
    }

    public static class LackShardException extends Exception {
        public LackShardException(int hasShards, int requiredShards) {
            super(String.format(Locale.getDefault(),"shards not enough: %d/%d", hasShards, requiredShards));
        }
    }

    public static class LackInputException extends Exception {
        public LackInputException(int inputSize, int requiredSize) {
            super(String.format(Locale.getDefault(), "input not enough: %d/%d", inputSize, requiredSize));
        }
    }

    public static class SizeMismatchException extends Exception {
        public SizeMismatchException(int needShardNum, int needParityNum, int getShardNum, int getParityNum) {
            super(String.format(Locale.getDefault(),
                    "shard list size mismatch: required (%d, %d), get (%d, %d)",
                    needShardNum, needParityNum, getShardNum, getParityNum));
        }
    }

    // Math.Ceil(int/int)
    private static int divisionCeil(int numerator, int denominator) {
        return (numerator + denominator - 1) / denominator;
    }

    public static class Decoder {
        private ShardCodec codec;
        public Decoder(){
            codec = null;
        }
        public byte[] Decode(ShardList shardList) throws Exception {
            if(codec == null || !codec.checkSize(shardList.SHARD_NUM, shardList.PARITY_NUM)) {
                codec = new ShardCodec(shardList.SHARD_NUM, shardList.PARITY_NUM);
            }
            return codec.decode(shardList);
        }
    }

    public ShardList encode(byte[] input, int dataSize, int gid) throws Exception{
        if (input.length < dataSize) {
            throw new LackInputException(input.length, dataSize);
        }

        // Compute size parameter
        int storeSize = dataSize + BYTES_IN_INT;  // BYTES_IN_INT is used to keep value of dataSize within output shards.
        int shardSize = divisionCeil(storeSize, SHARD_NUM);

        if (shardSize < BYTES_IN_INT) {           // We need BYTES_IN_INT within one shard.
            shardSize = BYTES_IN_INT;
        }

        // Create padded buffer
        // Size relationship: BYTES_IN_INT + dataSize + padded bytes = buffer size
        int bufferSize =  shardSize * SHARD_NUM;
        byte [] buffer = new byte[bufferSize];
        ByteBuffer.wrap(buffer).putInt(dataSize); // put size info at the first BYTES_IN_INT bytes of buffer.
                                                  // Note that java int is use 32-bit (4 bytes) .
        System.arraycopy(input, 0, buffer, BYTES_IN_INT, dataSize);

        // Create matrix
        int totalNumber = SHARD_NUM + PARITY_NUM;
        byte [] [] shards = new byte [totalNumber] [shardSize];
        for (int i = 0; i < SHARD_NUM; i++) {
            System.arraycopy(buffer, i * shardSize, shards[i], 0, shardSize);
        }

        // Do encode
        codec.encodeParity(shards, 0, shardSize);
        Shard[] res = new Shard[totalNumber];
        for (int i = 0; i < totalNumber; i++) {
            res[i] = new Shard(gid, i, shards[i]);
        }

        return new ShardList(res, SHARD_NUM, PARITY_NUM);
    }

    public byte[] decode(ShardList shardList) throws Exception {
        // Check size consistency
        if (shardList.SHARD_NUM != SHARD_NUM || shardList.PARITY_NUM != PARITY_NUM) {
//            throw new SizeMismatchException(SHARD_NUM, PARITY_NUM, shardList.SHARD_NUM, shardList.PARITY_NUM);
        }
        if (shardList.size() < shardList.SHARD_NUM) {
//            throw new LackShardException(shardList.size(), SHARD_NUM);
        }

        int totalNumber = SHARD_NUM + PARITY_NUM;
        byte [] [] shards = new byte [totalNumber][];
        boolean[] shardPresent = new boolean[totalNumber];

        // Note that all shards in shardList must have data length shardList.SHARD_SIZE.
        // That was ensured during the construction of shardList.
        // Here we do not check this.
        // TODO: maybe need to create every subArray of shards and use arrayCopy to fill them for safe.
        // fill shards
        shardList.traverse(s -> {
            shards[s.index] = s.data;
            shardPresent[s.index] = true;
        });

        // pad shards
        for (int i=0; i<totalNumber; i++) {
            if (!shardPresent[i]) {
                shards[i] = new byte[shardList.SHARD_SIZE];
            }
        }

        codec.decodeMissing(shards, shardPresent, 0, shardList.SHARD_SIZE);

        // build return bytes
        // In order to reduce memory use, we use only one buffer to hold the result with size fileSize.
        // So that the build process would be a bit ... complex.
        int fileSize = ByteBuffer.wrap(shards[0]).getInt();
        byte [] fileBytes = new byte [fileSize];
        int doneSize = 0;
        if (fileSize <= shardList.SHARD_SIZE - BYTES_IN_INT) {
            // shards[0][BYTES_IN_INT : BYTES_IN_INT + fileSize] => fileBytes[:fileSize]
            System.arraycopy(shards[0], BYTES_IN_INT, fileBytes, 0, fileSize);
            return fileBytes;
        } else {
            // TODO:
            //      Is there an exception if SHARD_SIZE = BYTES_IN_INT ?
            System.arraycopy(shards[0], BYTES_IN_INT, fileBytes, 0, shardList.SHARD_SIZE - BYTES_IN_INT);
            doneSize += shardList.SHARD_SIZE - BYTES_IN_INT;
            for (int i = 1; i < shardList.SHARD_NUM; i++) {
                if(fileSize - doneSize <= shardList.SHARD_SIZE) {
                    System.arraycopy(shards[i], 0, fileBytes, doneSize,fileSize - doneSize);
                    return fileBytes;
                } else {
                    System.arraycopy(shards[i], 0, fileBytes, doneSize, shardList.SHARD_SIZE);
                    doneSize += shardList.SHARD_SIZE;
                }
            }
        }

        if (doneSize < fileSize) {
            throw new IOException("decode size less than file size");
        }
        return fileBytes;
    }

    public boolean checkSize(int _SHARD_NUM, int _PARITY_NUM) {
        return SHARD_NUM == _SHARD_NUM && PARITY_NUM == _PARITY_NUM;
    }
}
