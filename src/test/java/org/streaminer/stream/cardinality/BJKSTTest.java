package org.streaminer.stream.cardinality;

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
public class BJKSTTest {
    
    public BJKSTTest() {
    }

    /**
     * Test of offer method, of class BJKST.
     */
    @Test
    public void testOffer() {
        System.out.println("offer");
        BJKST instance = new BJKST(1024, 64, 0.3);
        
        long cardinality = 500L;
        
        System.out.println("First batch:");
        for (long i = 0; i < cardinality; i++) {
            instance.offer(Long.valueOf(i));
            if (i % 50 == 0) {
                System.out.println("actual: " + i + ", estimated: " + instance.cardinality());
            }
        }
        
        System.out.println("Second batch:");
        for (long i = 0; i < cardinality; i++) {
            instance.offer(Long.valueOf(i));
            if (i % 50 == 0) {
                System.out.println("actual: " + i + ", estimated: " + instance.cardinality());
            }
        }

        System.out.println("actual: " + cardinality + ", estimated: " + instance.cardinality());
        
        assertEquals(cardinality, instance.cardinality(), 100);
    }
    
}
