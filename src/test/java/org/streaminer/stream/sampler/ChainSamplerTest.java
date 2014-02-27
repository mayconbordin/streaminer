package org.streaminer.stream.sampler;

import java.io.Serializable;
import java.util.Collection;
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
public class ChainSamplerTest {
    
    public ChainSamplerTest() {
    }
    
    /**
     * Test of sample method, of class ChainSampler.
     */
    @Test
    public void testSample() {
        System.out.println("sample");
        
        ChainSampler sampler = new ChainSampler(10, 125);
        
        for (int j=0; j<1000; j++) {
            sampler.sample(j);
        }
    }

}
