package org.streaminer.stream.frequency;

import java.util.List;
import java.util.Random;
import org.apache.commons.lang.StringUtils;
import org.streaminer.stream.frequency.AMSSketch;
import org.junit.Test;
import static org.junit.Assert.*;
import org.streaminer.stream.frequency.util.CountEntry;
import org.streaminer.util.distribution.ZipfDistribution;
import org.streaminer.util.hash.HashUtils;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class AMSSketchTest {
    private Random random = new Random();
    private ZipfDistribution zipf;
    
    public AMSSketchTest() {
    }

    /**
     * Test of add method, of class AMSSketch.
     */
    @Test
    public void testAdd() {
        int n = 1048575;
        int range = 12345;
        
        AMSSketch sketch = new AMSSketch(5, 512);
        StreamGenerator gen = new StreamGenerator(0.8, n, range);
        gen.generate();
        gen.exact();
        
        long[] stream = gen.stream;
        long sumsq = gen.sumsq;
        
        for (int i=1; i<=range; i++) 
            sketch.add(stream[i], 1);  
        
        // actual frequency
        RealCounting<Long> actualFreq = new RealCounting<Long>();
        for (int i=1; i<=range; i++)
            actualFreq.add(stream[i], 1);      
        
        List<CountEntry<Long>> topk = actualFreq.peek(10);
        
        System.out.println("Frequency Table\n" + StringUtils.repeat("-", 80));
        System.out.println("Item\tactual\testimated");
        for (CountEntry<Long> item : topk) {
            System.out.println(item.getItem() + "\t" 
                    + item.getFrequency() + "\t" 
                    + sketch.estimateCount(item.getItem()));
        }
        
        System.out.println("Exact F2: " + sumsq);
        System.out.println("Estimated F2: " + sketch.estimateF2());
    }
}
