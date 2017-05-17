package org.streaminer.stream.membership;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.streaminer.util.hash.MurmurHash;

public class VarCountingBloomFilter implements IFilter<String> {
    private static final MurmurHash hasher = new MurmurHash();
    
    private long maxCount = 15;
    private int bucketsPerWord = 16;
    private int exp;
    private int hashCount;
    private int numNonZero;
    private int numBuckets;
    private long[] buckets;

    /**
     * Creates a new filter with k hash functions accordingly with {@link BloomCalculations#computeBestK(int)}.
     * @param numElements Estimated number of distinct elements
     * @param bucketsPerElement Number of buckets per element
     * @param exp The 2^exp number of buckets per word, resulting in a counter of at most (2^exp)-1
     */
    public VarCountingBloomFilter(int numElements, int bucketsPerElement, int exp) {
        this.exp = exp;
        bucketsPerWord = (int) Math.pow(2, exp);
        maxCount   = bucketsPerWord - 1;
        hashCount  = BloomCalculations.computeBestK(bucketsPerElement);
        numBuckets = (numElements * bucketsPerElement + 20) / bucketsPerWord;
        numNonZero = 0;
        
        buckets = new long[numBuckets];
    }

    /**
     * Clear the filter
     */
    public void clear() {
        Arrays.fill(buckets, (byte)0);
        numNonZero = 0;
    }

    /**
     * Merge another filter within this one.
     * @param cbf The filter to be merged
     */
    public void merge(VarCountingBloomFilter cbf) {
        assert cbf != null;
        assert buckets.length == cbf.buckets.length;
        assert hashCount == cbf.hashCount;
        
        for ( int i = 0; i < buckets(); ++i )
        {
            Bucket b = new Bucket(i);
            Bucket b2 = cbf.getBucket(i);
            long merged = b.value + b2.value;
            b.set(merged > maxCount ? maxCount : merged);
        }
    }

    /**
     * Checks if item exists in the filter
     * @param item The item to be checked
     * @return True if the item exists of false otherwise
     */
    public boolean membershipTest(String item) {
        for (int bucketIndex : getHashBuckets(item)) {
            Bucket bucket = new Bucket(bucketIndex);
            if (bucket.value == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add an item to the filter, if item exists increment counter by one until
     * it reaches (2^exp)-1.
     * @param item The item to be added
     */
    public void add(String item) {
        assert item != null;
        for (int bucketIndex : getHashBuckets(item)) {
            Bucket bucket = new Bucket(bucketIndex);
            if(bucket.value < maxCount) {
                if (bucket.value == 0) numNonZero++;
                bucket.set(bucket.value + 1);
            }
        }
    }

    /**
     * Removes an item from the filter, if it already exists subtracts one from the
     * counters.
     * @param item The item to be removed
     */
    public void delete(String item) {
        if (!membershipTest(item)) {
            throw new IllegalArgumentException("key is not present");
        }

        for (int bucketIndex : getHashBuckets(item)) {
            Bucket bucket = new Bucket(bucketIndex);
            if(bucket.value >= 1 && bucket.value < maxCount) {
                bucket.set(bucket.value - 1);
                if ((bucket.value - 1) == 0) numNonZero--;
            }
        }
    }
    
    /**
     * Get the percentage of buckets that have a value different than zero, i.e.
     * the percentage of buckets not empty.
     * @return A value between 0 and 1
     */
    public double getPercentNonZero() {
        return (double) numNonZero / (double) numBuckets;
    }
    
    private Bucket getBucket(int i) {
        return new Bucket(i);
    }

    private int buckets() {
        return buckets.length * bucketsPerWord;
    }
    
    protected int emptyBuckets() {
        int n = 0;
        for (int i = 0; i < buckets(); i++) {
            if (new Bucket(i).value == 0) {
                n++;
            }
        }
        return n;
    }
    
    protected int maxBucket() {
        int max = 0;
        for (int i = 0; i < buckets(); i++) {
            Bucket bucket = new Bucket(i);
            if (bucket.value > max) {
                max = (int)bucket.value;
            }
        }
        return max;
    }
    
    private int[] getHashBuckets(String key) {
        return getHashBuckets(key, hashCount, buckets());
    }
    
    // murmur is faster than a sha-based approach and provides as-good collision
    // resistance.  the combinatorial generation approach described in
    // https://www.eecs.harvard.edu/~michaelm/postscripts/tr-02-05.pdf
    // does prove to work in actual tests, and is obviously faster
    // than performing further iterations of murmur.
    private static int[] getHashBuckets(String key, int hashCount, int max) {
        byte[] b;
        try {
            b = key.getBytes("UTF-16");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        int[] result = new int[hashCount];
        int hash1 = hasher.hash(b, b.length, 0);
        int hash2 = hasher.hash(b, b.length, hash1);
        for (int i = 0; i < hashCount; i++) {
            result[i] = Math.abs((hash1 + i * hash2) % max);
        }
        return result;
    }

    private class Bucket {
        public final int wordIndex;
        public final int shift;
        public final long mask;
        public final long value;

        public Bucket(int bucketIndex) {
            wordIndex = bucketIndex >> exp;
            shift = (bucketIndex & 0x0f) << 2;

            mask = maxCount << shift;
            value = ((buckets[wordIndex] & mask) >>> shift);
        }

        void set(long val) {
            buckets[wordIndex] = (buckets[wordIndex] & ~mask) | (val << shift);
        }
    }
}
