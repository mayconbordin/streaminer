
package org.streaminer.stream.norm;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.streaminer.stream.frequency.AMSSketch;
import org.streaminer.stream.frequency.RealCounting;
import org.streaminer.stream.frequency.StreamGenerator;
import org.streaminer.stream.frequency.util.CountEntry;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class StableSketchTest {
    
    public StableSketchTest() {
    }

    @Test
    public void testL2Norm() {
        System.out.println("Testing L2 Norm");
        
        int n = 1048575;
        int range = 12345;
        
        StableSketch sketch = new StableSketch(128,2.0,54211);
        StreamGenerator gen = new StreamGenerator(0.8, n, range);
        gen.generate();
        gen.exact();
        
        long[] stream = gen.stream;
        long sumsq = gen.sumsq;
        
        for (int i=1; i<=range; i++) 
            sketch.add((int) stream[i], 1.0);  
        
        double z = sketch.norm();
        
        System.out.println("Exact L2: " + sumsq);
        System.out.println("Estimated L2: " + (z*z));
        System.out.println(StringUtils.repeat("-", 80));
    }
    
    @Test
    public void testL1Norm() {
        System.out.println("Testing L1 Norm");
        
        int n = 1048575;
        int range = 12345;
        
        StableSketch sketch = new StableSketch(128,1.0,13461);
        StreamGenerator gen = new StreamGenerator(0.8, n, range);
        gen.generate();
        gen.exact();
        
        long[] stream = gen.stream;
        long sumsq = gen.sumsq;
        
        for (int i=1; i<=range; i++) 
            sketch.add((int) stream[i], 1.0);  
        
        double z = sketch.norm();
        
        System.out.println("Exact L1: " + range);
        System.out.println("Estimated L1: " + z);
        System.out.println(StringUtils.repeat("-", 80));
    }
    
    @Test
    public void testL0Norm() {
        System.out.println("Testing L0 Norm");
        
        int n = 1048575;
        int range = 12345;
        
        StableSketch sketch = new StableSketch(128,0.00001,13461);
        StreamGenerator gen = new StreamGenerator(0.8, n, range);
        gen.generate();
        gen.exact();
        
        long[] stream = gen.stream;
        long distinct = gen.distinct;
        
        for (int i=1; i<=range; i++) 
            sketch.add((int) stream[i], 1.0);  
        
        double z = sketch.norm()/1.43;
        
        System.out.println("Exact L0: " + distinct);
        System.out.println("Fast estimate L0: " + z);
        
        sketch = new StableSketch(128,0.02,13461);
        for (int i=1; i<=range; i++) 
            sketch.add((int) stream[i], 1.0);  
        
        z = sketch.norm()/1.43;
        
        System.out.println("Slow estimate L0: " + z);
        System.out.println(StringUtils.repeat("-", 80));
    }
}
