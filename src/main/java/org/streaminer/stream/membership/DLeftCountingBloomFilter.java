package org.streaminer.stream.membership;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class DLeftCountingBloomFilter {
    private static final int HASH_BYTES = 20;
    private static final int BUCKET_HEIGHT = 8;
    private static final int FINGERPRINT_BITS = 13;
    
    private int width;
    private int height;
    private long bits;
    private Bucket[][] buckets;

    public DLeftCountingBloomFilter(int d, int b) {
        this.width = d;
        this.height = b;
        
        bits = Math.round(Math.log(b)/Math.log(2));
        
        buckets = new Bucket[d][b];
    }
    
    private void getTargets(byte[] hash) {
        for (int i=0, pos=0; i<width; i++, pos+=FINGERPRINT_BITS) {
            long p = getBits(hash, bits, pos);
        }
    }
    
    
    protected long getBits(byte[] input, long numBits, int pos) {
        long value = 0;
        long postBits = pos % 8;
        int i = pos/8;
        long start = postBits > numBits ? numBits : postBits;
        
        if (start != 0) {
            value = input[i++] & (0xFF >> (8 - start));
            numBits -= start;
        }
        while (numBits >= 8) {
            value = (value << 8) | input[i++];
            numBits -= 8;
        }
        if (numBits != 0) {
            long last = (input[i] >> (8 - numBits));
            value = (value << numBits) | last;
        }
        return value;
    }
    
    private static class Bucket {
        
    }
}
