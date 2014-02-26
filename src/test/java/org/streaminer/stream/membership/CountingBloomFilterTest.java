package org.streaminer.stream.membership;

import java.io.DataInput;
import java.io.DataOutput;
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
public class CountingBloomFilterTest {
    
    public CountingBloomFilterTest() {
    }
    
    /**
     * Test of add method, of class CountingBloomFilter.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        Random r = new Random();
        CountingBloomFilter instance = new CountingBloomFilter(10000, 32, Hash.MURMUR_HASH);
        Map<Integer, Integer> count = new HashMap<Integer, Integer>(1000);
        
        for (int i=0; i<1000; i++) {
            int num = r.nextInt(10);
            Key k = new Key(String.valueOf(num).getBytes());
            instance.add(k);
            
            int c = 1;
            if (count.containsKey(num))
                c = c + count.get(num);
            count.put(num, c);
        }

        System.out.println("Num\tEst.\tActual");
        for (int i=0; i<10; i++) {
            Key k = new Key(String.valueOf(i).getBytes());
            System.out.println(i + "\t" + instance.estimateCount(k) + "\t" + count.get(i));
        }
    }
}
