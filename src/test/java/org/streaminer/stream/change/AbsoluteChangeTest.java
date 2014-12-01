package org.streaminer.stream.change;

import cern.colt.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class AbsoluteChangeTest {
    
    public AbsoluteChangeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of add method, of class AbsoluteChange.
     */
    @Test
    public void test() {
        /**
         * This is the width to use for the data structure it should be proportional 
         * to 1/error desired. In turn, error should be less than phi, but in practice 
         * this isn't always necessar. Width should be a few hundred to a few thousand
         * maybe 10,000 or higher for best accuracy, depending on  memory available.
         */
        int width = 200;
        /**
         * Depth is the number of repetitions of the testing with different hash functions.
         * More repetitions drive down the probability of missing something exponentially.
         * In practice, quite small values seem to suffice, but setting this to 5 or 
         * more gives very good accuracy although increasing this slows the updates 
         * down, since "depth" copies are run in parallel.
         */
        int depth = 2;
        
        // this defines the universe size
        int n = 1048576; 
        
        int lgn = (int) Math.ceil(Math.log((float) n)/Math.log(2.0));
        
        // parameters
        int noStreams = 2;
        int streamLength = 50000;
        int offset = 52521;
        double zipfpar = 3.0;
        double phi = 0.005;
        
        StreamGenerator gen = new StreamGenerator(streamLength, offset, zipfpar, noStreams);
        gen.generate();
        
        long[][] streams = gen.getStreams();
        
        // exact solution
        ExactSolution exact = new ExactSolution(streams[0], streams[1], phi);
        exact.computeDeltoids();
        
        System.out.println("Threshold: " + exact.getThresh());
        System.out.println("Relative Threshold: " + exact.getRelThresh());
        System.out.println("Absolute deltoids: " + Arrays.toString(exact.getAbsDeltoids().toArray()));
        System.out.println("Relative deltoids: " + Arrays.toString(exact.getRelDeltoids().toArray()));
    
    
    
        // absolute change
        AbsoluteChange abs = new AbsoluteChange(width, depth, lgn);
        for (int j=0; j<streamLength; j++)
            abs.add(streams[0][j], 1);
        for (int j=0; j<streamLength; j++)
            abs.add(streams[1][j], -1);
        
        
        List<Long> absDeltoids = abs.getDeltoids((int) exact.getThresh());
        System.out.println("Result: " + Arrays.toString(absDeltoids.toArray()));
    }
    
}
