package org.streaminer.stream.change;

import java.util.Random;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public abstract class AbstractChange {
    protected int depth;
    protected int width;
    protected int lgn;
    protected int size;
    protected long[] testa;
    protected long[] testb;
    
    protected Random random;

    /**
     * Initialize the data structure for finding changes.
     * @param width 1/eps = width of hash functions
     * @param depth Number of independent repetitions to avoid misses
     * @param lgn Number of bits in representation of item indexes
     */
    public AbstractChange(int width, int depth, int lgn) {
        this.depth = depth;
        this.width = width;
        this.lgn = lgn;
        
        size = width * depth;
        random = new Random();
        
        testa = new long[depth];
        testb = new long[depth];
        
        createHashFunctions();
    }
    
    protected final void createHashFunctions() {
        // create the hash functions
        for (int i=0; i<depth; i++) {
            testa[i] = random.nextLong();
            if (testa[i] < 0) testa[i]= -testa[i];
            testb[i] = random.nextLong();
            if (testb[i] < 0) testb[i]= -testb[i];
        }
    }
    
    protected void logInsert(int[] lists, int val, int length, int diff) {
        // update the logn different tests for a particular item  
        for (int i=length; i>0; i--) {
            if ((val & 1) == 1)
                lists[i] += diff;
            val >>= 1;
        }
    }
    
    protected void logInsert(float[] lists, int val, int length, float diff) {
        // update the logn different tests for a particular item  
        for (int i=length; i>0; i--) {
            if ((val & 1) == 1)
                lists[i] += diff;
            val >>= 1;
        }
    }
}
