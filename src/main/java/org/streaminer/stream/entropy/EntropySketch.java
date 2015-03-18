/*
 * The MIT License
 *
 * Copyright 2015 mayconbordin.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.streaminer.stream.entropy;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.streaminer.util.hash.Hash;

/**
 * Based on implementation by Damian Gryski (github.com/dgryski/go-entropy).
 * @author mayconbordin
 */
public class EntropySketch {
    private int count;
    private double[] data;
    private final Hash hasher;
    private long seed;
    
    public EntropySketch(int k) {
        this(k, 0, Hash.getInstance(Hash.SPOOKY_HASH));
    }
    
    public EntropySketch(int k, long seed) {
        this(k, seed, Hash.getInstance(Hash.SPOOKY_HASH));
    }

    /**
     * Creates a new sketch with an epsilon accuracy of k=O(1/eps**2).
     * @param k
     * @param seed The seed used by the hasher
     * @param hasher 
     */
    public EntropySketch(int k, long seed, Hash hasher) {
        this.hasher = hasher;
        this.seed = seed;
        
        count = 0;
        data = new double[k];
    }
    
    /**
     * Add element b to the stream inc times.
     * @param b The element to be added
     * @param inc How many times the element occurred
     */
    public void push(byte[] b, int inc) {
        long it = hasher.hash64(b);
        
        count += inc;

        RandomGenerator r = new MersenneTwister(it);
        
        for (int i=0; i<data.length; i++) {
            double val = maxSkew(r);
            data[i] += val * (double)inc;
        }
    }
    
    /**
     * An estimate of the stream entropy so far.
     * @return 
     */
    public double entropy() {
        double sum = 0.0;
        
        for (double d : data) {
            double tmp = d / (double)count;
            sum += Math.exp(tmp);
        }
        
        return -(Math.log(sum / (double)data.length) / Math.log(2));
    }
    
    /**
     * Return a float from the maximally skewed stable distribution F(x;1,-1,math.Pi/2,0)
     * @param r
     * @return 
     */
    private double maxSkew(RandomGenerator r) {
        double u1 = r.nextDouble();
        double u2 = r.nextDouble();

        double w1 = Math.PI * (u1 * 0.5);
        double w2 = - (Math.log(u2) / Math.log(2));
        
        double halfPiW1 = Math.PI/2 - w1;
        
        return Math.tan(w1) * (halfPiW1) + (Math.log(w2 * (Math.cos(w1)/halfPiW1)) / Math.log(2));
    }
}
