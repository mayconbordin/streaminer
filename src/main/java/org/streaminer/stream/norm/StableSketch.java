package org.streaminer.stream.norm;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.StableRandomGenerator;
import org.streaminer.util.ArrayUtils;

/**
 * Sketches of vectors using stable distributions for Euclidean, Manhattan, 
 * L_p distances. File originally Gaussian Distribution generator, Nick Koudas.
 * Extended to pick from arbitrary stable distributions with alpha in the range 
 * (0, 2], Graham Cormode.
 * 
 * <a href="http://www.cs.rutgers.edu/~muthu/massdal-code-index.html">Original code</a>
 * 
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class StableSketch implements Comparable<StableSketch> {
    /**
     * The norm we are working in
     */
    private double alpha;
    
    /**
     * Length of the sketch
     */
    private int sksize;
    
    /**
     * Seed for the PRNG
     */
    private long seed;
    
    /**
     * The sketch
     */
    private double[] sk;
    
    private RandomGenerator randomGen;
    private StableRandomGenerator stableGen;

    public StableSketch(int sksize, double alpha, long seed) {
        this.alpha = alpha;
        this.sksize = sksize;
        this.seed = seed;
        
        sk = new double[sksize];
        
        randomGen = new MersenneTwister(seed);
        stableGen = new StableRandomGenerator(randomGen, alpha, 0);
    }
    
    public void add(int item, double val) {
        seed = item + seed;
        randomGen.setSeed(seed);
        
        // for each entry in the vector, create a pseudo-random sequence 
        // based on its position
        for (int j=0; j<sksize; j++) {
            // use this to make a sequence of stable variables and 
            // multiply these by the entry in the vector to maintain the sketch
            sk[j] += val * stableGen.nextNormalizedDouble();
        }
    }
    
    /**
     * Sums that sketch with this.
     * @param that 
     */
    public void add(StableSketch that) {
        if (this.compareTo(that) == 0) {
            for (int i=0; i<sksize; i++)
                sk[i] += that.sk[i];
        }
    }
    
    /**
     * Subtracts that sketch with this.
     * @param that 
     */
    public void subtract(StableSketch that) {
        if (this.compareTo(that) == 0) {
            for (int i=0; i<sksize; i++)
                sk[i] -= that.sk[i];
        }
    }
    
    /**
     * Calculates the distance between this sketch and that. Do this by treating 
     * them by first finding the absolute difference between each component.
     * The either use median (for alpha<2) or L2 (for alpha=2) to approximate 
     * the L_alpha distance.
     * 
     * @param that
     * @return The distance
     */
    public double distance(StableSketch that) {
        if (this.compareTo(that) != 0) return -1.0;
        
        double sum = 0.0;
        double _alpha = this.alpha;
        double[] holder;
        
        if (_alpha == 2.0) {
            for (int i=0; i<sksize; i++)
                sum += Math.pow(Math.abs(this.sk[i] - that.sk[i]), 2.0);
        } else { // Calculate the L_alpha distance
            holder = new double[sksize+1];
            for (int i=0; i<sksize; i++)
                holder[i+1] = this.sk[i] - that.sk[i];
            sum = ArrayUtils.doubleMedSelect(sksize/2, sksize, holder);
        }
        
        return sum;
    }
    
    /**
     * Find the L_p norm of the vector that a sketch represents, and return 
     * this to the power p -- needed for distinct values and so on.
     * @return 
     */
    public double norm() {
        double[] holder;
        double sum = 0.0;
        double est;
        
        // use the j-l lemma approach for L_2 distance 
        if (alpha == 2.0) {
            for (int i=0; i<sksize; i++) {
                est = sk[i] * sk[i];
                sum += est;
            }
            // Calculate the L_alpha norm 
            sum = Math.pow(sum/((double) sksize), 0.5);
        } else {
            holder = new double[sksize+1];
            
            for (int i=0; i<sksize; i++)
                holder[i+1] = Math.abs(sk[i]);
            
            // transfer the details into a suitable array 
            sum = ArrayUtils.doubleMedSelect(sksize/2, sksize, holder);
            
            //find the median of the arary, this is the estimator for the 
            // L_p norm of the vector
            if (alpha < 0.01)
                sum = Math.pow(sum, 0.02);
            else
                sum = Math.pow(sum, alpha);
            //return not the L_p norm but the L_p norm ^p  
        }
        
        return sum;
    }

    public int compareTo(StableSketch that) {
        // incomparable sketches
        if (this.alpha != that.alpha || this.sksize != that.sksize 
                || this.seed != that.seed)
            return 1;
        return 0;
    }
}
