package org.streaminer.stream.frequency;

import java.security.SecureRandom;
import org.streaminer.util.ArrayUtils;
import org.streaminer.util.hash.HashUtils;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class AMSSketch implements ISimpleFrequency<Long>, Comparable<AMSSketch> {
    private int depth;
    private int buckets;
    private int count = 0;
    private int[] counts;
    private long[][] test;
    
    private SecureRandom prng = new SecureRandom();

    public AMSSketch(int depth, int buckets) {
        this.depth = depth;
        this.buckets = buckets;
        
        counts = new int[buckets*depth];
        test = new long[6][depth];
        
        for (int i=0; i<depth; i++) {
            for (int j=0; j<6; j++) {
                test[j][i] = prng.nextLong();
                if (test[j][i] < 0)
                    test[j][i] = -test[j][i];
            }
        }
    }
    
    public boolean add(Long item) {
        return add(item, 1);
    }
    
    public boolean add(Long item, long incrementCount) {
        int offset = 0, hash, mult;
        count += incrementCount;
        
        for (int j=0; j<depth; j++) {
            hash = (int) HashUtils.hash31(test[0][j], test[1][j], item);
            hash = hash % buckets;
            mult = (int) HashUtils.fourwise(test[2][j], test[3][j], test[4][j], test[5][j], item);
            if ((mult&1)==1)
                counts[offset+hash] += incrementCount;
            else
                counts[offset+hash]-= incrementCount;
            
            offset += buckets;
        }
        
        return true;
    }

    public int compareTo(AMSSketch o) {
        if (buckets != o.buckets)
            return (buckets > o.buckets) ? 1: -1;
        
        
        if (depth != o.depth)
            return (depth > o.depth) ? 1: -1;
        
        for (int i=0; i<depth; i++)
            for (int j=0; j<6; j++)
                if (test[j][i] != o.test[j][i])
                    return (test[j][i] > o.test[j][i]) ? 1: -1;
        
        return 0;
    }
    
    public long estimateCount(Long item) {
        int offset = 0, hash, mult, estimate;
        int[] estimates = new int[depth+1];

        for (int i=1; i<=depth; i++) {
            hash = (int) HashUtils.hash31(test[0][i-1], test[1][i-1], item);
            hash = hash % buckets; 
            mult = (int) HashUtils.fourwise(test[2][i-1], test[3][i-1], test[4][i-1], test[5][i-1], item);
            if ((mult&1)==1)
                estimates[i] = counts[offset+hash];
            else
                estimates[i] = -counts[offset+hash];
            offset += buckets;
        }
  
        if (depth == 1) 
            estimate = estimates[1];
        else if (depth == 2)
            estimate = (estimates[1]+estimates[2])/2; 
        else
            estimate = ArrayUtils.medSelect(1+depth/2, depth, estimates);
        
        return estimate;
    }
    
    public long size() {
        return count;
    }
    
    
    public long estimateF2() {
        // estimate the F2 moment of the vector (sum of squares)

        int r = 0;
        long[] estimates = new long[depth+1];
        long result, z;

        for (int i=1; i<=depth; i++) {
            z=0;
            for (int j=0; j<buckets; j++) {
                z += counts[r] * counts[r];
                r++;
            }
            estimates[i]=z;
        }
        
        if (depth == 1)
            result = estimates[1];
        else if (depth == 2)
            result = (estimates[1]+estimates[2])/2; 
        else
            result = ArrayUtils.longMedSelect(1+depth/2, depth, estimates);

        return result;
    }
    
    public long innerProduct(AMSSketch b){
        int r = 0;
        long[] estimates = new long[depth+1];
        long result, z;
        // estimate the innerproduct of two vectors using their sketches.

        if (this.compareTo(b) != 0) return 0;

        for (int i=1; i<=depth; i++) {
            z=0;
            for (int j=0; j<buckets; j++) {
                z += counts[r] * b.counts[r];
                r++;
            }
            estimates[i] = z;
        }
        
        if (depth == 1)
            result = estimates[1];
        else if (depth == 2)
            result = (estimates[1]+estimates[2])/2; 
        else
            result = ArrayUtils.longMedSelect(1+depth/2, depth, estimates);
        
        return result;
    }
    
    public boolean add(AMSSketch source) {
        int r = 0;

        // add one sketch to another

        if (this.compareTo(source) != 0)
            return false;
        
        for (int i=0; i<source.depth; i++)
            for (int j=0; j<source.buckets; j++) {
                counts[r] += source.counts[r];
                r++;
            }
        
        return true;
    }
    
    public boolean subtract(AMSSketch source) {
        int r = 0;
        // subtract one sketch from another

        if (this.compareTo(source) != 0)
            return false;
        
        for (int i=0; i<source.depth; i++)
            for (int j=0; j<source.buckets; j++) {
                counts[r] -= source.counts[r];
                r++;
            }
        
        return true;
    }
}
