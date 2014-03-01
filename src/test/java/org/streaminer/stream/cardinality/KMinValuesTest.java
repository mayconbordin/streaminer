package org.streaminer.stream.cardinality;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.streaminer.util.hash.MurmurHash3;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class KMinValuesTest {
    
    public KMinValuesTest() {
    }
    
    /**
     * Test of offer method, of class KMinValues.
     */
    @Test
    public void testOffer() {
        System.out.println("offer");
        KMinValues instance = new KMinValues(20);
        
        System.out.println("Approx.\tReal");
        for (int i=0; i<100; i++) {
            instance.offer(String.valueOf(i));
            System.out.println(instance.cardinality() + "\t" + (i+1));
        }
    }
}
