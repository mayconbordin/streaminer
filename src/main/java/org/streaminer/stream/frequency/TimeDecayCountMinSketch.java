package org.streaminer.stream.frequency;

import java.util.Random;
import org.streaminer.stream.frequency.decay.DecayFormula;
import org.streaminer.util.hash.HashUtils;

/**
 * see: https://github.com/arinto/storm-word-count/blob/master/src/main/java/com/yahoo/swc/bolt/ForwardDecayCountWord.java
 * and: ODTDBloomFilter
 * and: decayhashmap
 * 
 * it is better to update a counter on demand instead of updating all of them at once
 */
public class TimeDecayCountMinSketch implements ITimeDecayFrequency<Object> {
    public static final long PRIME_MODULUS = (1L << 31) - 1;
    private int depth;
    private int width;
    private double[][] table;
    private long[] hashA;
    private long[] timers;
    private long size;
    private double eps;
    private double confidence;
    private DecayFormula formula;

    private TimeDecayCountMinSketch() {
    }

    public TimeDecayCountMinSketch(int depth, int width, int seed, DecayFormula formula) {
        this.depth = depth;
        this.width = width;
        this.eps = 2.0 / width;
        this.confidence = 1 - 1 / Math.pow(2, depth);
        this.formula = formula;
        initTablesWith(depth, width, seed);
    }

    public TimeDecayCountMinSketch(double epsOfTotalCount, double confidence, int seed, DecayFormula formula) {
        // 2/w = eps ; w = 2/eps
        // 1/2^depth <= 1-confidence ; depth >= -log2 (1-confidence)
        this.eps = epsOfTotalCount;
        this.confidence = confidence;
        this.width = (int) Math.ceil(2 / epsOfTotalCount);
        this.depth = (int) Math.ceil(-Math.log(1 - confidence) / Math.log(2));
        this.formula = formula;
        initTablesWith(depth, width, seed);
    }

    private TimeDecayCountMinSketch(int depth, int width, int size, long[] hashA, double[][] table) {
        this.depth = depth;
        this.width = width;
        this.eps   = 2.0 / width;
        this.confidence = 1 - 1 / Math.pow(2, depth);
        this.hashA = hashA;
        this.table = table;
        this.size  = size;
    }

    private void initTablesWith(int depth, int width, int seed) {
        this.table = new double[depth][width];
        this.hashA = new long[depth];
        this.timers = new long[width];
        
        Random r = new Random(seed);
        // We're using a linear hash functions
        // of the form (a*x+b) mod p.
        // a,b are chosen independently for each hash function.
        // However we can set b = 0 as all it does is shift the results
        // without compromising their uniformity or independence with
        // the other hashes.
        for (int i = 0; i < depth; ++i) {
            hashA[i] = r.nextInt(Integer.MAX_VALUE);
        }
    }

    public double getRelativeError() {
        return eps;
    }

    public double getConfidence() {
        return confidence;
    }
    
    private int hash(long item, int i) {
        long hash = hashA[i] * item;
        // A super fast way of computing x mod 2^p-1
        // See http://www.cs.princeton.edu/courses/archive/fall09/cos521/Handouts/universalclasses.pdf
        // page 149, right after Proposition 7.
        hash += hash >> 32;
        hash &= PRIME_MODULUS;
        // Doing "%" after (int) conversion is ~2x faster than %'ing longs.
        return ((int) hash) % width;
    }
    
    public void add(Object item, long qtd, long timestamp) {
        if (qtd < 0) {
            throw new IllegalArgumentException("Negative increments not implemented");
        }

        if (item instanceof Integer) {
            addLong(((Integer)item).longValue(), qtd, timestamp);
        } else if (item instanceof Long) {
            addLong((Long)item, qtd, timestamp);
        } else if (item instanceof String) {
            addString((String)item, qtd, timestamp);
        }
    }

    public void addString(String item, long qtd, long timestamp) {
        int[] buckets = HashUtils.getHashBuckets((String)item, depth, width);

        for (int i = 0; i < depth; ++i) {
            double quantity = 0.0;
            if (timers[buckets[i]] <= timestamp) {
                quantity = projectValue(timestamp, timers[buckets[i]], table[i][buckets[i]]) + qtd;
                timers[buckets[i]] = timestamp;
            } else {
                quantity += projectValue(timers[buckets[i]], timestamp, qtd);
            }
            
            table[i][buckets[i]] = quantity;
        }
        
        size += qtd;
    }
    
    private void addLong(long item, long qtd, long timestamp) {
        for (int i = 0; i < depth; ++i) {
            int h = hash((Long)item, i);
            
            double quantity = 0.0;
            if (timers[h] <= timestamp) {
                quantity = projectValue(timestamp, timers[h], table[i][h]) + qtd;
                timers[h] = timestamp;
            } else {
                quantity += projectValue(timers[h], timestamp, qtd);
            }
            
            table[i][h] = quantity;
        }
        size += qtd;
    }
    
    public double estimateCount(Object item, long timestamp) {
        if (item instanceof Integer) {
            return estimateCountLong(((Integer)item).longValue(), timestamp);
        } else if (item instanceof Long) {
            return estimateCountLong((Long)item, timestamp);
        } else if (item instanceof String) {
            return estimateCountString((String) item, timestamp);
        }
        
        return 0d;
    }

    public double estimateCountString(String item, long timestamp) {
        double res = Double.MAX_VALUE;
        int[] buckets = HashUtils.getHashBuckets((String)item, depth, width);
        for (int i = 0; i < depth; ++i) {
            double value = projectValue(timestamp, timers[buckets[i]], table[i][buckets[i]]);
            res = Math.min(res, value);
        }
        return res;
    }
    
    private double estimateCountLong(long item, long timestamp) {
        double res = Double.MAX_VALUE;
        for (int i = 0; i < depth; ++i) {
            int h = hash((Long)item, i);
            double value = projectValue(timestamp, timers[h], table[i][h]);
            res = Math.min(res, value);
        }
        return res;
    }
    
    private double projectValue(long futureTimestamp, long timestamp, double quantity) {
        if (futureTimestamp < timestamp) {
            throw new IllegalArgumentException("Cannot project decaying quantity into the past.");
        }
        double t = Double.valueOf(futureTimestamp -  timestamp);
        return formula.evaluate(quantity, t);
    }

    public long size() {
        return size;
    }
}
