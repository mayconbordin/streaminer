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
public class FlajoletMartinTest {
    
    public FlajoletMartinTest() {
    }

    /**
     * Test of offer method, of class FlajoletMartin.
     */
    @Test
    public void testOffer() {
        System.out.println("offer");
        FlajoletMartin instance = new FlajoletMartin(32, 1024, 32);
        
        long cardinality = 500L;
        
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
