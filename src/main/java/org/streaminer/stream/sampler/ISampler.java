package org.streaminer.stream.sampler;

public interface ISampler {
    
    /**
     * Decide whether to sample the next item by sampler's algorithm .
     * @return True if sample is accepted or false otherwise
     */
    public boolean next();
   
    /**
     * Set seed so that sampling result can be repeated.
     * @param seed
     */
    public void setSeed(long seed);
}
