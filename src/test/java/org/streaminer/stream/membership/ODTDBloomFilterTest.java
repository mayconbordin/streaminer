package org.streaminer.stream.membership;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.streaminer.util.hash.Hash;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class ODTDBloomFilterTest {
    static final double MAX_FAILURE_RATE = 0.1;
    public static final BloomCalculations.BloomSpecification spec = BloomCalculations.computeBucketsAndK(MAX_FAILURE_RATE);
    static final int ELEMENTS = 100000;
    
    public ODTDBloomFilterTest() {
    }
    

    @Test
    public void test() {
        Random r = new Random();
        ODTDBloomFilter instance = new ODTDBloomFilter(ELEMENTS, spec.bucketsPerElement, 0.9672);
        Map<Integer, Integer> count = new HashMap<Integer, Integer>(1000);
        
        for (int i=0; i<100000000; i++) {
            int num = r.nextInt(1000);
            instance.add(String.valueOf(num), 1);
            
            int c = 1;
            if (count.containsKey(num))
                c = c + count.get(num);
            count.put(num, c);
        }

        double diff = 0;
        System.out.println("Num\tAprox\tReal");
        for (int i=0; i<100; i++) {
            double aprox = instance.estimateCount(String.valueOf(i));
            double real = count.get(i);
            diff += (real - aprox);
            System.out.println(i + "\t" + aprox + "\t" + real);
        }
        System.out.println("Difference: " + diff);
    }
}
