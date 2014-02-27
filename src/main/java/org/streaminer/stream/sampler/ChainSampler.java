package org.streaminer.stream.sampler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the chain-sample algorithm. When an item is added to the sample,
 * a random replacement is generated ad upon the arrival of this item, the old one
 * is replaced, composing a chain of replacements.
 * 
 * Reference:
 *   Babcock, Brian, Mayur Datar, and Rajeev Motwani. "Sampling from a moving 
 *   window over streaming data." Proceedings of the thirteenth annual ACM-SIAM 
 *   symposium on Discrete algorithms. Society for Industrial and Applied 
 *   Mathematics, 2002.
 * 
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class ChainSampler implements ISampleList<Serializable> {
    private static final Logger LOG = LoggerFactory.getLogger(ChainSampler.class);
    
    private int k;
    private int n;
    private long count = 0;
    private int fillSample = 0;
    
    private Serializable[] items;
    private Map<Integer, Integer> replacements;
    
    private Random rand = new Random();

    /**
     * Create a new sampler of size k for a window of size n.
     * @param k The size of the sample
     * @param n The size of the window
     */
    public ChainSampler(int k, int n) {
        this.k = k;
        this.n = n;
        items = new Serializable[k];
        replacements = new HashMap<Integer, Integer>(k);
    }
    
    /**
     * Give an item to the sampler so that it can decide to add or not as a sample.
     * @param item The item to be sampled
     */
    public void sample(Serializable item) {
        int i = (int) (count%n);
        
        if (replacements.containsKey(i)) {
            int replace = replacements.get(i);
            // replace the old item
            items[replace] = item;
            
            int next = rand.nextInt(n);
            
            LOG.info(String.format("Item=%s; i=%d; b=%d; next=%d", item, i, replace, next));
            
            replacements.remove(i);
            replacements.put(next, replace);
        }
        
        // this will build the initial sample
        else if (fillSample < k) {
            double prob = ((double)Math.min(i, n))/((double)n);
            
            if (rand.nextDouble() < prob) {
                int bucket = fillSample++;
                int next = rand.nextInt(n);
                
                items[bucket] = item;
                replacements.put(next, bucket);
                
                LOG.info(String.format("[init] Item=%s; i=%d; b=%d; next=%d", item, i, bucket, next));
            }
        }
        count++;
    }
    
    /**
     * Sample a list of items
     * @param t The list of items to be sampled
     */
    public void sample(Serializable... t) {
        for (Serializable item : t) {
            sample(item);
        }
    }

    /**
     * @return The list of currently sampled items
     */
    public Collection<Serializable> getSamples() {
        List<Serializable> result = new ArrayList<Serializable>();
        
        for (Serializable item : items)
            if (item != null)
                result.add(item);
        
        return result;
    }

    /**
     * @return The size of the sample
     */
    public int getSize() {
        return (fillSample < k) ? fillSample : k;
    }
}