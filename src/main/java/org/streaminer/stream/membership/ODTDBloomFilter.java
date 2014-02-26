package org.streaminer.stream.membership;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.streaminer.util.hash.MurmurHash;

/**
 * Implementation of the On-demand Time-decaying Bloom Filter. It is similar to a
 * time decaying bloom filter, except for the fact that only the counters that are
 * going to be used are updated, instead of all of them periodically.
 * 
 * TODO: replace the 2d array of buckets and introduce bitwise operations
 * 
 * Reference:
 *   Dusi, Maurizio, et al. "Blockmon: Flexible and high-performance big data stream 
 *   analytics platform and its use cases." NEC Technical Journal 7.2 (2012): 103.
 * 
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class ODTDBloomFilter implements IFilter<String> {
    private static final MurmurHash hasher = new MurmurHash();
    
    private int bucketsPerWord;
    private int hashCount;
    private int numBuckets;
    private double[][] buckets;
    private long[] timers;
    private double beta;
    
    /**
     * Constructor with default number of buckets per word (16).
     * @param numElements Expected number of distinct elements
     * @param bucketsPerElement The number of buckets per element, used to calculate number of hashes (k)
     * @param beta The normalized smoothing coeficient, i.e. measured rate decay in the unit time
     */
    public ODTDBloomFilter(int numElements, int bucketsPerElement, double beta) {
        this(numElements, bucketsPerElement, beta, 16);
    }

    /**
     * Creates a new filter with k hash functions accordingly with {@link BloomCalculations#computeBestK(int)}.
     * Each bucket will have bucketsPerWord counters, but since this implementation
     * uses a 2D array there's no limitation in the counter value, unlike {@link CountingBloomFilter}.
     * 
     * @param numElements Expected number of distinct elements
     * @param bucketsPerElement The number of buckets per element, used to calculate number of hashes (k)
     * @param beta The normalized smoothing coeficient, i.e. measured rate decay in the unit time
     * @param bucketsPerWord Number of counters within a bucket (bin)
     */
    public ODTDBloomFilter(int numElements, int bucketsPerElement, double beta, int bucketsPerWord) {
        this.beta = beta;
        this.bucketsPerWord = bucketsPerWord;
        
        hashCount = BloomCalculations.computeBestK(bucketsPerElement);
        numBuckets = (numElements * bucketsPerElement + 20) / bucketsPerWord;
        
        buckets = new double[numBuckets][bucketsPerWord];
        timers  = new long[numBuckets];
    }

    /**
     * Checks if the item is stored in the data structure.
     * @param item The item to be verified
     * @return True if the item exists of false otherwise
     */
    public boolean membershipTest(String item) {
        for (int bucketIndex : getHashBuckets(item)) {
            if (getBucket(bucketIndex) == 0) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Add the item to the data structure with quantity's value one.
     * @param item The item to be added
     */
    public void add(String item) {
        add(item, 1);
    }

    /**
     * Add an item to the data structure.
     * @param item The item to be added
     * @param q The quantity for the key
     */
    public void add(String item, int q) {
        assert item != null;
        double count = estimateCount(item) + ((double)q * Math.log(1/beta));
        
        for (int bucketIndex : getHashBuckets(item)) {
            if (getBucket(bucketIndex) < count)
                setBucket(bucketIndex, count);
        }
    }

    /**
     * Delete an item from the data structure, subtracting q to the counters.
     * @param item The item to be deleted
     * @param q The quantity to be subtracted from the item's counters
     */
    public void delete(String item, int q) {
        if (!membershipTest(item)) {
            throw new IllegalArgumentException("key is not present");
        }

        for (int bucketIndex : getHashBuckets(item)) {
            if (getBucket(bucketIndex) >= q) {
                addToBucket(bucketIndex, -q);
            }
        }
    }
    
    /**
     * Estimate the count for an item.
     * @param item The item to be estimated
     * @return The counter value for the item
     */
    public double estimateCount(String item) {
        long time = time();
        double res = Double.MAX_VALUE;
        
        for (int bucketIndex : getHashBuckets(item)) {
            // update the counter with the smoothing coeficient
            double value = getBucket(bucketIndex) * Math.pow(beta, time - getTimer(bucketIndex));
            setBucket(bucketIndex, value);
            
            // update the bucket timer
            setTimer(bucketIndex, time);
            
            if (value < res)
                res = value;
        }
        return (res != Double.MAX_VALUE) ? res : 0;
    }
    
    /**
     * Resets the data structure
     */
    public void clear() {
        for (int i=0; i<buckets.length; i++)
            Arrays.fill(buckets[i], 0d);
        
        Arrays.fill(timers, 0L);
    }

    /**
     * @return Return the total number of counters in the data structure
     */
    private int buckets() {
        return buckets.length * bucketsPerWord;
    }

    /**
     * Get a list of hashes for an item
     * @param item The item for calculating the hashes
     * @return The list of hashes
     */
    private int[] getHashBuckets(String item) {
        return getHashBuckets(item, hashCount, buckets());
    }
    
    // murmur is faster than a sha-based approach and provides as-good collision
    // resistance.  the combinatorial generation approach described in
    // http://www.eecs.harvard.edu/~kirsch/pubs/bbbf/esa06.pdf
    // does prove to work in actual tests, and is obviously faster
    // than performing further iterations of murmur.
    public static int[] getHashBuckets(String item, int hashCount, int max) {
        byte[] b;
        try {
            b = item.getBytes("UTF-16");
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
    
    private static long time() {
        return System.currentTimeMillis()/1000;
    }
    
    private double getBucket(int bucketIndex) {
        return buckets[bucketIndex/16][bucketIndex%16];
    }
    
    private void addToBucket(int bucketIndex, double value) {
        buckets[bucketIndex/16][bucketIndex%16] += value;
    }
    
    private void setBucket(int bucketIndex, double value) {
        buckets[bucketIndex/16][bucketIndex%16] = value;
    }
    
    private long getTimer(int bucketIndex) {
        return timers[bucketIndex/16];
    }
    
    private void setTimer(int bucketIndex, long value) {
        timers[bucketIndex/16] = value;
    }
}