package org.streaminer.stream.frequency;

import java.util.Random;
import org.streaminer.util.ArrayUtils;
import org.streaminer.util.hash.HashUtils;

/**
 * Count sketches from Charikar, Chen, Farach-Colton '02. They support: finding 
 * frequent items, returning point estimates.
 * 
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class CCFCSketch implements IBaseFrequency<Integer> {
    private int tests;
    private int logn;
    private int gran;
    private int buckets;
    private int count;
    private int[][] counts;
    private int[] testa, testb, testc, testd;
    
    private Random random = new Random();
    
    /**
     * Create the data structure for Adaptive Group Testing Keep T tests.
     * @param buckets The number of buckets for each test
     * @param tests The number of tests
     * @param lgn The bit depth of the items which will arrive
     * @param gran gran = 1 means to do one bit at a time, gran = 8 means to do one quad at a time, etc. 
     */
    public CCFCSketch(int buckets, int tests, int lgn, int gran) {
        this.tests = tests;
        this.logn = lgn;
        this.gran = gran;
        this.buckets = buckets;
        this.count = 0;
        
        testa = new int[tests];
        testb = new int[tests];
        testc = new int[tests];
        testd = new int[tests];
        // create space for the hash functions

        counts = new int[lgn+1][buckets*tests];

        for (int i=0; i<tests; i++) {
            testa[i] = random.nextInt();
            if (testa[i] < 0) testa[i]= -testa[i];
            testb[i] = random.nextInt();
            if (testb[i] < 0) testb[i]= -testb[i];
            testc[i] = random.nextInt();
            if (testc[i] < 0) testc[i]= -testc[i];
            testd[i] = random.nextInt();
            if (testd[i] < 0) testd[i]= -testd[i];
        }
    }
    
    public boolean add(Integer item) {
        return add(item, 1);
    }
    
    public boolean add(Integer item, long incrementCount) {
        int hash;
        int mult, offset;

        count += incrementCount;
        
        for (int i=0; i<logn; i+=gran) {
            offset = 0;
            for (int j=0;j< tests; j++) {
                hash = (int) HashUtils.hash31(testa[j], testb[j], item);
                hash = hash % buckets;
                mult = (int) HashUtils.hash31(testc[j], testd[j], item);
                if ((mult&1) == 1)
                    counts[i][offset+hash] += incrementCount;
                else
                    counts[i][offset+hash] -= incrementCount;
                offset += buckets;
            }
            item >>= gran;
        }
        
        return true;
    }
    
    public long estimateCount(Integer item, int depth) {
        int offset = 0, hash, mult, estimate;
        int[] estimates = new int[tests+1];

        if (depth == logn) return(count);

        for (int i=1; i<=tests; i++) {
            hash = (int) HashUtils.hash31(testa[i-1], testb[i-1], item);
            hash = hash % (buckets); 
            mult = (int) HashUtils.hash31(testc[i-1], testd[i-1], item);
            if ((mult&1) == 1)
                estimates[i] = counts[depth][offset+hash];
            else
                estimates[i] = -counts[depth][offset+hash];
            offset += buckets;
        }
        
        if (tests == 1)
            estimate = estimates[1];
        else if (tests == 2)
            estimate = (estimates[1]+estimates[2])/2; 
        else
            estimate = ArrayUtils.medSelect(1+tests/2,tests,estimates);

        return estimate;
    }
    
    private int[] recursive(int depth, int start, int thresh) {
        int blocksize;
        int estcount = (int) estimateCount(start, depth);
        int itemshift;
        int[] results = new int[buckets];
        results[0] = 0;
        
        if (estcount >= thresh) { 
            if (depth == 0) {
                if (results[0] < buckets) {
                    results[0]++;
                    results[results[0]] = start;
                }
            } else {
                blocksize = 1 << gran;
                itemshift = start << gran;
                // assumes that gran is an exact multiple of the bit dept
                for (int i=0; i<blocksize; i++)
                    recursive(depth-gran, itemshift+i, thresh);
              }
        }
        
        return results;
    }
    
    public int[] output(int thresh) {
        return recursive(logn, 0, thresh);
    }
    
    public long estimateF2() {
        int r = 0;
        long[] estimates = new long[tests+1];
        long result, z;

        for (int i=1; i<=tests; i++) {
            z=0;
            for (int j=0; j<buckets; j++) {
                z += (long)counts[0][r] * (long)counts[0][r];
                r++;
            }
            estimates[i] = z;
        }
        
        if (tests == 1)
            result = estimates[1];
        else if (tests == 2)
            result = (estimates[1]+estimates[2])/2; 
        else
            result = ArrayUtils.longMedSelect(1+tests/2, tests, estimates);

        return result;
    }

    public long size() {
        return count;
    }
    
    
}
