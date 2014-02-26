package org.streaminer.stream.membership;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class VarCountingBloomFilterTest {
    public VarCountingBloomFilter cbf;
    static final double MAX_FAILURE_RATE = 0.1;
    public static final BloomCalculations.BloomSpecification spec = BloomCalculations.computeBucketsAndK(MAX_FAILURE_RATE);
    static final int ELEMENTS = 10000;
    static final int EXP = 4;
    static final int MAX_COUNT = (int) (Math.pow(2, EXP) - 1);
    
    public VarCountingBloomFilterTest() {
        cbf = new VarCountingBloomFilter(ELEMENTS, spec.bucketsPerElement, EXP);
    }

    @Test
    public void testOne() {
        cbf.clear();
        
        cbf.add("a");
        assertTrue(cbf.membershipTest("a"));
        assertEquals(1, cbf.maxBucket());
        assertFalse(cbf.membershipTest("b"));

        cbf.delete("a");
        assertFalse(cbf.membershipTest("a"));
        assertEquals(0, cbf.maxBucket());
    }

    @Test
    public void testCounting() {
        cbf.clear();
        
        cbf.add("a");
        cbf.add("a");
        assertEquals(2, cbf.maxBucket());
        cbf.delete("a");
        assertTrue(cbf.membershipTest("a"));
        assertEquals(1, cbf.maxBucket());

        for (int i = 0; i < ELEMENTS; i++) {
            cbf.add(Integer.toString(i));
        }
        for (int i = 0; i < ELEMENTS; i++) {
            cbf.delete(Integer.toString(i));
        }
        assertTrue(cbf.membershipTest("a"));

        cbf.delete("a");
        assertFalse(cbf.membershipTest("a"));
        assertEquals(0, cbf.maxBucket());
    }

    
    @Test
    public void testMerge() {
        cbf.clear();
        
        cbf.add("a");
        cbf.add("a");

        VarCountingBloomFilter cbf2 = new VarCountingBloomFilter(ELEMENTS, spec.bucketsPerElement, EXP);
        cbf2.add("a");
        cbf2.add("a");

        cbf.merge(cbf2);
        assertEquals(4, cbf.maxBucket());
    }

    @Test
    public void testMergeMaxCount() {
        cbf.clear();
        
        for (int i = 0; i < MAX_COUNT; i++) {
            cbf.add("b");
        }

        VarCountingBloomFilter cbf2 = new VarCountingBloomFilter(ELEMENTS, spec.bucketsPerElement, EXP);
        cbf2.add("b");

        cbf.merge(cbf2);
        assertEquals(MAX_COUNT, cbf.maxBucket());
    }

    @Test
    public void testMaxCount() {
        cbf.clear();
        
        for (int i = 0; i < MAX_COUNT; i++) {
            cbf.add("a");
        }
        assertEquals(MAX_COUNT, cbf.maxBucket());
        cbf.add("a");
        assertEquals(MAX_COUNT, cbf.maxBucket());
        cbf.delete("a");
        assertEquals(MAX_COUNT, cbf.maxBucket());
    }
}
