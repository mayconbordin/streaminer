package org.streaminer.stream.membership;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class TimingBloomFilterTest {
    
    public TimingBloomFilterTest() {
    }
    
    @Test
    @Ignore
    public void testDecay() throws InterruptedException {
        TimingBloomFilter filter = new TimingBloomFilter(500, 4);
        filter.startDecay();
        filter.add("hello");
        
        assertTrue(filter.membershipTest("hello"));
        
        Thread.sleep(5 * 1000);
        
        assertFalse(filter.membershipTest("hello"));
    }
    
    @Test
    @Ignore
    public void testAdd() {
        int capacity = 100000;
        TimingBloomFilter filter = new TimingBloomFilter(capacity*2, 10);
        filter.startDecay();
        
        System.out.println("Adding");
        for (int i=0; i<capacity; i++)
            filter.add("idx_" + i);
        
        System.out.println("MembershipTest(+)");
        for (int i=0; i<capacity; i++)
            assertTrue("False negative", filter.membershipTest("idx_" + i));
        
        System.out.println("MembershipTest(-)");
        int c = 0;
        for (int i=capacity; i<2*capacity; i++) {
            c += (filter.membershipTest("idx_" + i) == true) ? 1 : 0;
        }
        
        double falsePos = ((double)c) / ((double)capacity);
        System.out.println("C: " + c);
        System.out.println("False positives: " + falsePos);
        System.out.println("Error threshold: " + filter.getError());
        assertTrue("Too many false positives", falsePos <= filter.getError());
    }
    
    /*
    @Test
    public void testAdd() {
        System.out.println("Adding");
        for (int i=0; i<capacity; i++)
            filter.add("idx_" + i);
    }
    
    @Test
    public void testMembershipTestPositive() {
        System.out.println("MembershipTest(+)");
        for (int i=0; i<capacity; i++)
            assertTrue("False negative", filter.membershipTest("idx_" + i));
    }
    
    @Test
    public void testMembershipTestNegative() {
        System.out.println("MembershipTest(-)");
        int c = 0;
        for (int i=capacity; i<2*capacity; i++)
            c += filter.membershipTest("idx_" + i) ? 1 : 0;
        
        double falsePos = c / capacity;
        System.out.println("C: " + c);
        System.out.println("Capacity: " + capacity);
        System.out.println("False positives: " + falsePos);
        System.out.println("Error threshold: " + filter.getError());
        assertTrue("Too many false positives", falsePos <= filter.getError());
    }
    
    /*
    @Test
    public void test() throws InterruptedException {

        filter.add("teste", (System.currentTimeMillis()/1000));
        filter.add("done", (System.currentTimeMillis()/1000));
        
        //long[] hashes = filter.indexes("teste");
        
        System.out.println("Contains 'teste': " + filter.membershipTest("teste"));
        
        System.out.println("Sleep 5s");
        Thread.sleep(5 * 1000);
        System.out.println("Contains 'teste': " + filter.membershipTest("teste"));
        
        System.out.println("Sleep 5s");
        Thread.sleep(5 * 1000);
        System.out.println("Contains 'teste': " + filter.membershipTest("teste"));
    }*/
    
}
