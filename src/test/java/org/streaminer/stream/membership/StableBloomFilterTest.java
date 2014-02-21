package org.streaminer.stream.membership;

import com.google.common.hash.Funnels;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: aarutyunyants
 * Date: 7/12/13
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class StableBloomFilterTest {
    private StableBloomFilter<CharSequence> sbf;
    
    public StableBloomFilterTest() {
    }

    @Before
    public void setUp() {
        sbf = new StableBloomFilter<CharSequence>(50, 10, 5, Funnels.stringFunnel());
    }
    
    @After
    public void tearDown() {
        sbf = null;
    }

    @Test
    public void testMightContain() throws Exception {
        sbf.add("1test");
        sbf.add("2test");
        sbf.add("3test");
        sbf.add("3test");
        sbf.add("3test");
        sbf.add("3test");
        sbf.add("3test");
        sbf.add("3test");
        sbf.add("3test");
        sbf.add("3test");        
        //assertTrue("False negative", sbf.membershipTest("1test"));
        //assertFalse("False positive", sbf.membershipTest("10test"));
    }

    @Test
    public void testPut() throws Exception {
        sbf.add("1test");
        assertTrue(sbf.membershipTest("1test"));
        assertFalse(sbf.membershipTest("10test"));
    }

    @Test
    public void testDecrement() throws Exception {
      int min = 65;
      int max = 90;
      for (int i = min; i < max; i++) {
        sbf.add((char) i + "");
      }
      assertTrue("Recent items should be in the filter", sbf.membershipTest((char) (max - 1) + ""));
      assertTrue("Recent items should be in the filter", sbf.membershipTest((char) (max - 2) + ""));
      assertTrue("Recent items should be in the filter", sbf.membershipTest((char) (max - 3) + ""));
      //assertFalse("False positive", sbf.membershipTest("no"));
      //assertFalse("Old items might NOT be in the filter", sbf.membershipTest((char) min + ""));
    }
    
}