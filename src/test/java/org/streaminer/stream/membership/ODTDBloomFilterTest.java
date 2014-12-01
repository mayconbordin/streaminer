package org.streaminer.stream.membership;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Ignore;
import org.junit.Test;
import org.streaminer.stream.frequency.FrequencyException;
import org.streaminer.stream.frequency.RealCounting;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 * 
 * 
 * TODO: check the timers, maybe they should be initialized, first value don't get
 * smoothed. Recent values shouldn't be so small. Check if your are using milliseconds or seconds.
 */
public class ODTDBloomFilterTest {
    static final double MAX_FAILURE_RATE = 0.1;
    public static final BloomCalculations.BloomSpecification spec = BloomCalculations.computeBucketsAndK(MAX_FAILURE_RATE);
    static final int ELEMENTS = 100000;
    
    public ODTDBloomFilterTest() {
    }
    

    @Test
    @Ignore
    public void test() {
        Random r = new Random();
        ODTDBloomFilter instance = new ODTDBloomFilter(ELEMENTS, spec.bucketsPerElement, 0.9672);
        Map<Integer, Integer> count = new HashMap<Integer, Integer>(1000);
        
        for (int i=0; i<100000; i++) {
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
    
    @Test
    public void newtest() throws FrequencyException {
        Random r = new Random();
        ODTDBloomFilter filter = new ODTDBloomFilter(ELEMENTS, spec.bucketsPerElement, 0.9672);
        RealCounting<Integer> exact = new RealCounting<Integer>();
        
        for (int i=0; i<100000; i++) {
            int num = r.nextInt(1000);
            filter.add(String.valueOf(num), 1);
            exact.add(num);
        }

        double diff = 0;
        System.out.println("Num\tAprox\tReal");
        for (int i=0; i<100; i++) {
            double aprox = filter.estimateCount(String.valueOf(i));
            double real = exact.estimateCount(i);
            diff += (real - aprox);
            System.out.println(i + "\t" + aprox + "\t" + real);
        }
        System.out.println("Difference: " + diff);
    }
}
