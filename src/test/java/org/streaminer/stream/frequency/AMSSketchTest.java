package org.streaminer.stream.frequency;

import java.util.Random;
import org.streaminer.stream.frequency.AMSSketch;
import org.junit.Test;
import static org.junit.Assert.*;
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
        
        zipf = new ZipfDistribution(0.8, n, range);
        AMSSketch sketch = new AMSSketch(5, 512);
        
        int[] exact  = new int[n+1];
        long[] stream = new long[range+1];
        
        long a = random.nextInt() % HashUtils.MOD;
        long b = random.nextInt() % HashUtils.MOD;
        
        long value;
        
        // generate stream
        for (int i=1; i<=range; i++) {
            value =  (HashUtils.hash31(a,b, (long) zipf.nextDouble())&1048575);
            exact[(int)value]++;
            stream[i] = value;
            System.out.println("Stream " + i + " is " + value);
        }
        
        
        long sumsq=0, distinct=0;
        for (int i=0; i<n; i++) {
            sumsq += (long)exact[i] *  (long)exact[i];
            if (exact[i]>0) {
                distinct++;
            }
        }
        
        
        for (int i=1; i<=range; i++) 
            if (stream[i]>0)
                sketch.add(stream[i], 1);      
            else
                sketch.add(-stream[i], -1);   
        
        System.out.println("Exact F2: " + sumsq);
        System.out.println("Estimated F2: " + sketch.estimateF2());
    }
}
