package org.streaminer.stream.frequency;

import org.apache.mahout.math.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import org.streaminer.util.hash.HashUtils;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class CCFCSketchTest {
    
    public CCFCSketchTest() {
    }

    /**
     * Test of add method, of class CCFCSketch.
     */
    @Test
    public void test() {
        System.out.println(HashUtils.hash31(10000, 233000, 6677));
        /*
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
        
        CCFCSketch sketch = new CCFCSketch(width, depth, lgn, gran);
        for (int i=1; i<=range; i++) 
            if (stream[i]>0)
                sketch.add((int)stream[i], 1);      
            else
                sketch.add((int)-stream[i], -1);   
        
        int[] outlist = sketch.output((int) thresh);
        
        System.out.println("Output: " + Arrays.toString(outlist));
        
        int val = 96699;
        System.out.println("Estimative for " + val + ": " + sketch.estimateCount(96699, depth));
        
        gen.checkOutput(outlist, (int) thresh, hh);*/
    }
    
}
