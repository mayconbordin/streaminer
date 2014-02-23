package org.streaminer.stream.sampler;

/**
 * This class implements systematic sampling. 
 * 
 * Systematic sampling chooses population unit with a fixed period.
 * */
public class SystematicSampler implements ISampler {
    private long period = 0;
    private long counter = 0;
    
    public SystematicSampler(long period) {
        if(period < 2)
            throw new IllegalArgumentException("period must be greater than 1");
        this.period = period;
    }

    public boolean next() {
        boolean res = counter % period == 0;
        counter ++; if(counter < 0) counter = 0;
        return res;
    }

    public void setSeed(long seed) {
        if(seed < 0) seed *= -1;
        counter = seed;
    }

    public void reset() {
        counter = 0;
    }
}