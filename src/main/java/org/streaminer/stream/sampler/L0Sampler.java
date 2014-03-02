package org.streaminer.stream.sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.streaminer.stream.sampler.sre.OneSparseRecoveryEstimator;
import org.streaminer.stream.sampler.sre.SSparseRecoveryEstimator;
import org.streaminer.util.hash.Hash;

/**
 * A naive implementation of an L0-Sampling data structure, as described in 
 * Cormode and Firmani's 2013 paper, "On Unifying the Space of L0-Sampling Algorithms".
 * 
 * N refers to the size of the input space (e.g. an unsigned 64-bit int in the
 * case of most cookie ID spaces)
 * 
 * k refers to the number of hash functions used in the s-sparse recovery data
 * structure.
 * 
 * s refers to the sparsity of the s-sparse recovery data structure.
 * 
 * In theory, one generally should hold k >= s/2, but in practice C&F note that 
 * "it suffices to use small values of k, for instance k=7, to ensure that the 
 * failure rate holds steady, independent of the number of reptitions made." 
 * 
 * Also of note: "When time is important, using s<=12 and k<=6 ensures fast 
 * computation. On the other hand, by selecting bigger values for both s and 
 * k, the process becomes slower than the FIS variant."
 * 
 * Python Source Code: https://github.com/venantius/droplet
 * 
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class L0Sampler {
    private static Random rand = new Random();
    
    private int size;
    private int sparsity;
    private int k;
    private SSparseRecoveryEstimator[] levels;
    private Hash hasher;

    
    public L0Sampler(int size, int sparsity, Hash hasher) {
        this.size = size;
        this.sparsity = sparsity;
        this.hasher = hasher;
        
        double delta = Math.pow(2, (-sparsity/ 12));
        k = (int) Math.round(Math.log(sparsity/delta)/Math.log(2));
        
        initialize();
    }

    public L0Sampler(int size, int sparsity, int k, Hash hasher) {
        this.size = size;
        this.sparsity = sparsity;
        this.k = k;
        this.hasher = hasher;
        
        initialize();
    }
    
    private void initialize() {
        int numLevels = (int) Math.round(Math.log(size)/Math.log(2));
        levels = new SSparseRecoveryEstimator[numLevels];
        
        for (int i=0; i<numLevels; i++)
            levels[i] = new SSparseRecoveryEstimator(sparsity*2, k, hasher);
    }
    
    /**
     * Attempt to recover a nonzero vector from one of the L0 Sampler's levels.
     * @return 
     */
    public int[] recover() {
        return recover(rand.nextInt(size));
    }
    
    /**
     * Attempt to recover a nonzero vector from one of the L0 Sampler's levels.
     * @param i
     * @return 
     */
    public int[] recover(int i) {
        List<OneSparseRecoveryEstimator> vector = null;
        for (SSparseRecoveryEstimator level : levels) {
            if (level.isSSparse()) {
                vector = level.recover();
                if (!vector.isEmpty())
                    break;
            }
        }
        
        if (vector != null && !vector.isEmpty())
            return select(vector);
        else
            return null;
    }
    
    /**
     * Update the L0 sampler. This process generally aligns with the 'sample'
     * step as described in section 2 of the paper.
     * @param i
     * @param value 
     */
    public void update(int i, int value) {
        if (!(i > 0 && i <= size))
            throw new IllegalArgumentException("Update value " + i + "outside size" + size);
        
        for (int j=0; j<levels.length; j++) {
            if (size * Math.pow(2, -(j + 1)) >= (hasher.hash(String.valueOf(i)) % size) + 1)
                levels[j].update(i, value);
        }
    }
    
    /**
     * Attempts to select (and delete) an item from the data structure until
     * either the data structure is empty or no more items can be recovered.
     * @return 
     */
    public List<Integer[]> recursiveSelection() {
        List<Integer[]> sample = new ArrayList<Integer[]>();
        
        while (true) {
            int[] selection = recover();
            if (selection == null)
                break;
            
            sample.add(new Integer[]{selection[0], selection[1]});
            update(selection[0], -selection[1]);
        }
        
        return sample;
    }
    
    /**
     * Given a vector of recovered items, grabs the one with the lowest hash value.
     * @param vector
     * @return 
     */
    private int[] select(List<OneSparseRecoveryEstimator> vector) {
        Collections.sort(vector, sreComparator);
        OneSparseRecoveryEstimator item = vector.get(0);
        int i = item.getIota() / item.getPhi();
        return new int[]{i, item.getPhi()};
    }
    
    private Comparator<OneSparseRecoveryEstimator> sreComparator = new Comparator<OneSparseRecoveryEstimator>() {
        public int compare(OneSparseRecoveryEstimator o1, OneSparseRecoveryEstimator o2) {
            int h1 = hasher.hash(String.valueOf(o1.getIota()/o1.getPhi()));
            int h2 = hasher.hash(String.valueOf(o2.getIota()/o2.getPhi()));

            if (h1 < h2) return -1;
            else if (h1 > h2) return 1;
            else return 0;
        }
    };


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("L0Sampler{levels=[");
        for (int i=0; i<levels.length; i++) {
            sb.append(String.format("level %d: %s", i, levels[i].toString()));
        }
        sb.append(String.format("], size=%d, sparsity=%d, k=%d}", size, sparsity, k));
        return sb.toString();
    }
}
