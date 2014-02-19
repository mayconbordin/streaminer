package org.streaminer.stream.frequency;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.streaminer.stream.frequency.util.CountEntry;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class CGTTest {
    
    public CGTTest() {
    }

    /**
     * Test of add method, of class CGT.
     */
    @Test
    public void test() throws Exception {
        int n=1048575, lgn=10, range=123456;
        int width = 512, depth = 5, gran = 1;
        double phi = 0.01;
        
        StreamGenerator gen = new StreamGenerator(1.1, n, range);
        gen.generate();
        
        lgn = 20;
        
        double thresh = Math.floor(phi*(double)range);  
        if (thresh == 0) thresh = 1.0;
        
        int hh = gen.exact((int) thresh);
        long[] stream = gen.stream;
        
        CGT sketch = new CGT(width, depth, lgn, gran);
        for (int i=1; i<=range; i++) 
            //if (stream[i]>0)
                sketch.add((int)stream[i], 1);      
           // else
           //     sketch.add((int)-stream[i], -1);   
        
        // actual frequency
        RealCounting<Integer> actualFreq = new RealCounting<Integer>();
        for (int i=1; i<=range; i++)
            actualFreq.add((int)stream[i], 1);      
        
        List<CountEntry<Integer>> topk = actualFreq.peek(10);
        
        System.out.println("Frequency Table\n" + StringUtils.repeat("-", 80));
        System.out.println("Item\tactual");
        for (CountEntry<Integer> item : topk) {
            System.out.println(item.getItem() + "\t" 
                    + item.getFrequency());
        }
        
        System.out.println("Frequent Items");
        List<CountEntry<Integer>> outlist = sketch.peek(10, thresh);
        for (CountEntry<Integer> item : outlist) {
            System.out.println(item.getItem() + "\t" 
                    + item.getFrequency());
        }
        
        /*
        
        int[] outlist = sketch.output((int) thresh);
        
        System.out.println("Output: " + Arrays.toString(outlist));
        
        int val = 96699;
        System.out.println("Estimative for " + val + ": " + sketch.estimateCount(96699, depth));
        
        gen.checkOutput(outlist, (int) thresh, hh);*/
    }
}
