package org.streaminer.stream.sampler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Sampling with replacement: a method of randomly sampling n items from a set 
 * of M items, with equal probability; where M >= n and M, the number of items 
 * is unknown until the end. This means that the equal probability sampling 
 * should be maintained for all successive items > n as they become available 
 * (although the content of successive samples can change).
 * 
 * Source code: http://rosettacode.org/wiki/Knuth's_algorithm_S#Java
 * Reference:
 *   Knuth, Donald Ervin. The art of computer programming. Pearson Education, 2005.
 * 
 * @param <T>
 */
public class WRSampler<T> implements ISampleList<T> {
    private static final Random rand = new Random();
 
    private List<T> sample;
    private int count = 0;
    private int sampleSize;
    
    public WRSampler(int sampleSize) {
        this.sampleSize = sampleSize;
        sample = new ArrayList<T>(sampleSize);
    }   
    
    public void sample(T item) {
        count++;
	if (count <= sampleSize) {
            sample.add(item);
	} else if (rand.nextInt(count) < sampleSize) {
	    sample.set(rand.nextInt(sampleSize), item);
	}
    }

    public void sample(T... items) {
        for (T item : items)
            sample(item);
    }

    public Collection<T> getSamples() {
        return sample;
    }

    public int getSize() {
        return sampleSize;
    }
    
}
