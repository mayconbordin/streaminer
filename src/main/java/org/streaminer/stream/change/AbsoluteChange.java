package org.streaminer.stream.change;

import java.util.ArrayList;
import java.util.List;
import org.apache.mahout.math.Arrays;
import org.streaminer.util.hash.HashUtils;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class AbsoluteChange extends AbstractChange {
    private int[][] counts;
    
    public AbsoluteChange(int width, int depth, int lgn) {
        super(width, depth, lgn);
        
        counts = new int[size][lgn+1];
    }
    
    /**
     * Update the count of an item.
     * @param item The idenfitier of the item being updated
     * @param diff The change, positive or negative
     */
    public void add(long item, int diff) {
        long hash;
        
        // for each set of groups, find the group that the item belongs in, update it
        for (int i=0; i<depth; i++) {
            // use the hash function to find the place where the item belongs
            hash = HashUtils.hash31(testa[i], testb[i], item);
            hash = hash % width;
            
            // call external routine to update the counts
            logInsert(counts[i * width + (int)hash], (int)item, lgn, diff);
        }
    }
    
    public List<Long> getDeltoids(int thresh) {
        long guess, hash;
        int testval = 0;
        List<Long> results = new ArrayList<Long>();
        
        for (int i=0; i<depth; i++) {
            for (int j=0; j<width; j++) {
                // go over all the different tests and see if there is a 
                // deltoid within each test
                guess = findOne(testval, thresh);
                
                if (guess > 0) {
                    hash = HashUtils.hash31(testa[i], testb[i], guess);
                    hash = hash % width;
                    
                    // check item does hash into that bucket... 
                    if (hash == j) {
                        if (!results.contains(guess))
                            results.add(guess);
                    }
                }
                // advance to next item
                testval++;
            }
        }
        
        return results;
    }
    
    
    /**
     * Search through a set of tests to detect whether there is a deltoid there
     * returns 0 if none found, returns id of found item otherwise.
     * @param thresh
     * @return 
     */
    private long findOne(int pos, int thresh) {
        long j = 1, k = 0;
        
        if (Math.abs(counts[pos][0]) < thresh) {
            k = 0;
        } else {
            int c = counts[pos][0];
            
            for (int i=lgn; i>0; i--) {
                // main test: if one side is above threshold and the otherside is not
                if (isOneSideAbove(counts[pos][i], (c - counts[pos][i]), thresh)) {
                    k = 0;
                    // if test fails, bail out
                    break;
                }
                
                if (Math.abs(counts[pos][i]) >= thresh)
                    k += j;
                
                // build the binary representation of the item
                j = j << 1;
            }
        }
        
        return k;
    }
    
    private boolean isOneSideAbove(int a, int b, int thresh) {
        return ((Math.abs(a) < thresh && Math.abs(b) < thresh) 
                || (Math.abs(a) >= thresh && Math.abs(b) >= thresh));
    }
}
