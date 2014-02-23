/*
Copyright 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose
is hereby granted without fee, provided that the above copyright notice appear in all copies and
that both that copyright notice and this permission notice appear in supporting documentation.
CERN makes no representations about the suitability of this software for any purpose.
It is provided "as is" without expressed or implied warranty.
*/

package org.streaminer.stream.sampler;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.JDKRandomGenerator;

/**
 * Conveniently computes a stable subsequence of elements from a given input sequence;
 * Picks (samples) exactly one random element from successive blocks of <tt>weight</tt> input elements each.
 * For example, if weight==2 (a block is 2 elements), and the input is 5*2=10 elements long, then picks 5 random elements from the 10 elements such that
 * one element is randomly picked from the first block, one element from the second block, ..., one element from the last block.
 * weight == 1.0 --> all elements are picked (sampled). weight == 10.0 --> Picks one random element from successive blocks of 10 elements each. Etc.
 * The subsequence is guaranteed to be <i>stable</i>, i.e. elements never change position relative to each other.
 *
 * @author  wolfgang.hoschek@cern.ch
 * @version 1.0, 02/05/99
 */
public class WeightedRandomSampler implements ISampler {
    private static final int UNDEFINED = -1;
    
    protected int skip;
    protected int nextTriggerPos;
    protected int nextSkip;
    protected int weight;
    protected RandomGenerator generator;
    
    public WeightedRandomSampler() {
        this(1, new JDKRandomGenerator());
    }

    public WeightedRandomSampler(int weight, RandomGenerator generator) {
        this.generator = generator;
        setWeight(weight);
    }

    public boolean next() {
        if (skip > 0) { //reject
            skip--; 
            return false;
        }
        
        if (nextTriggerPos == UNDEFINED) {
            if (weight == 1)
                nextTriggerPos = 0; // tuned for speed
            else
                nextTriggerPos = generator.nextInt(weight);

            nextSkip = weight - 1 - nextTriggerPos;
        }
        
        if (nextTriggerPos > 0) { //reject
            nextTriggerPos--;
            return false;
        }

        //accept
        nextTriggerPos = UNDEFINED;
        skip = nextSkip;
        
        return true;
    }

    public void setSeed(long seed) {
        generator.setSeed(seed);
    }
    
    public final void setWeight(int weight) {
        if (weight < 1)
            throw new IllegalArgumentException("Weight should be greater than 0");
        
        this.weight = weight;
        this.skip = 0;
        this.nextTriggerPos = UNDEFINED;
        this.nextSkip = 0;
    }
    
    
}
