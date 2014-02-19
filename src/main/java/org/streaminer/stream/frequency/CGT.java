package org.streaminer.stream.frequency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.streaminer.stream.frequency.util.CountEntry;
import org.streaminer.util.hash.HashUtils;

/**
 * Combinatorial Group Testing to Find Frequent Items.
 * 
 * Reference:
 * G. Cormode. What's Hot and What's Not: Tracking Frequent Items Dynamically, PODS 2003
 * 
 * <a href="http://www.cs.rutgers.edu/~muthu/massdal-code-index.html">Original code</a>
 * 
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class CGT implements IBaseFrequency<Integer>, IFrequencyList<Integer> {
    public static final int DEFAULT_THRESHOLD = 1;
    
    private int tests;
    private int logn;
    private int gran;
    private int buckets;
    private int subbuckets;
    private int count;
    private int[][] counts;
    private long[] testa;
    private long[] testb;
    
    private Random random = new Random();

    /**
     * Create the data structure for Combinatorial Group Testing.
     * 
     * @param buckets Each test has buckets buckets
     * @param tests Keep T tests
     * @param logn logn is the bit depth of the items which will arrive, this 
     *             code assumes lgn <= 32 since it manipulates unsigned ints
     * @param gran gran is the granularity at which to perform the testing
     *             gran = 1 means to do one bit at a time,
     *             gran = 4 means to do one nibble at time
     *             gran = 8 means to do one octet at a time, etc. 
     */
    public CGT(int buckets, int tests, int logn, int gran) {
        this.tests = tests;
        this.logn = logn;
        this.gran = gran;
        this.buckets = buckets;
        
        subbuckets = 1 + (logn/gran) * ((1 << gran) - 1);
        count = 0;
        
        testa = new long[tests];
        testb = new long[tests];
        counts = new int[buckets*tests][subbuckets];
        
        // initialise the hash functions
        for (int i=0; i<tests; i++) {
            testa[i] = random.nextLong();
            if (testa[i] < 0) 
                testa[i]= -testa[i];
            
            testb[i] = random.nextLong();
            if (testb[i] < 0)
                testb[i] = -testb[i];
        }
    }

    public boolean add(Integer item) throws FrequencyException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean add(Integer item, long incrementCount) throws FrequencyException {
        long hash;
        int offset = 0;
        
        count += incrementCount;
        
        for (int i=0; i<tests; i++) {
            hash = HashUtils.hash31(testa[i], testb[i], item);
            hash = hash % buckets;
            logInsert((int)(offset+hash), item, (int)incrementCount);
            offset += buckets;
        }
        
        return true;
    }

    public long size() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public List<CountEntry<Integer>> getFrequentItems(double minSupport) {
        Map<Integer,CountEntry<Integer>> results = new HashMap<Integer,CountEntry<Integer>>();
        int thresh = (int) minSupport;
        int testval=0, hash=0, guess = 0;
        boolean pass;
        
        for (int i=0; i<tests; i++) {
            for (int j=0; j<buckets; j++) {
                guess = (int) findOne(testval, thresh);
                
                // go into the group, and see if there is a frequent item there
                // then check item does hash into that group... 
                if (guess > 0) {
                    hash = (int) HashUtils.hash31(testa[i], testb[i], guess);
                    hash = hash % buckets;
                }
                
                if (guess > 0 && hash == j) {
                    pass = true;
                    
                    for (int k=0; k<tests; k++) {
                        // check every hash of that item is above threshold... 
                        hash = (int) HashUtils.hash31(testa[k], testb[k], guess);
                        hash = (buckets * k) + (hash % buckets);
                        
                        //System.out.println("item="+guess+"; freq="+counts[hash][0]);
                        
                        if (counts[hash][0] < thresh)
                            pass = false;
                    }
                    
                    if (pass) {
                        // if the item passes all the tests, then output it
                        results.put(guess, new CountEntry<Integer>(guess, counts[hash][0]));
                    }
                }
                
                testval++;
            }
        }
        
        return new ArrayList(results.values());
    }
    
    /**
     * 
     * @param pos
     * @param thresh
     * @return The identity of the frequent item if there was one or zero if there was none. 
     */
    private long findOne(int pos, int thresh) {
        int k = 0;
        int offset, countabove, sum, last;

        // if the count is not above threshold, then reject
        if (counts[pos][0] >= thresh) {
            offset = 1;
            
            for (int i=logn; i>0; i-=gran) {
                k <<= gran;
                countabove=0; sum=0; last=0;
                
                for (int l=1; l<(1 << gran); l++) {
                    if (counts[pos][offset] >= thresh) {
                        countabove++;
                        last = l;
                    }
                    
                    sum += counts[pos][offset++];
                }
                
                if (counts[pos][0] - sum >= thresh)
                    countabove++;
                
                // check: if both halves of a group are above threshold,
                // then reject the whole group
                if (countabove != 1) {
                    k = 0;
                    break;
                }
                
                // Update the record of the identity of the frequent item
                k += last;
            }
        }
        
        return k;
    }
    
    private void logInsert(int pos, int val, int inc) {
        int bitmask = (1 << gran) - 1;
        int offset = ((logn/gran)*bitmask) - bitmask;
        
        // add the increment to the count of the group
        counts[pos][0] += inc;
        
        for (int i=logn; i>0; i-=gran) {
            if ((val & bitmask) != 0) // if the lsb = 1, then add on to that group
                counts[pos][offset + (val&bitmask)] += inc;
            val >>= gran; // look at the next set of bits
            offset -= bitmask;
        }
    }

    public Set<Integer> keySet() {
        return null;
    }

    public List<CountEntry<Integer>> peek(int k) {
        return peek(k, DEFAULT_THRESHOLD);
    }

    public List<CountEntry<Integer>> peek(int k, double minSupport) {
        List<CountEntry<Integer>> items = getFrequentItems(minSupport);

        Collections.sort(items);
        if (items.size() > k)
            return items.subList(0, k);
        else
            return items;
    }

    public List<CountEntry<Integer>> getFrequentItems() {
        return getFrequentItems(DEFAULT_THRESHOLD);
    }
}
