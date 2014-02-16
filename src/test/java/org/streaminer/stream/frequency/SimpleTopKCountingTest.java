/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.streaminer.stream.frequency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.streaminer.stream.frequency.util.CountEntry;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class SimpleTopKCountingTest {
    private Random random = new Random();
    
    public SimpleTopKCountingTest() {
    }

    /**
     * Test of add method, of class SimpleTopKCounting.
     */
    @Test
    public void testIntegerSet() throws Exception {
        int numItems = 1000;
        int[] xs = new int[numItems];
        int maxScale = 20;
        for (int i = 0; i < xs.length; ++i) {
            int scale = random.nextInt(maxScale);
            xs[i] = random.nextInt(1 << scale);
        }
        
        SimpleTopKCounting<Integer> counter = new SimpleTopKCounting(maxScale);
        
        for (int v : xs) {
            counter.add(v);
        }
        
        int count = 0;
        List<CountEntry<Integer>> topk  = counter.getFrequentItems();
        Collections.sort(topk);
                
        List<Integer> frequentItems = new ArrayList<Integer>();
        CountEntry<Integer> lastItem = topk.get(topk.size() - 1);
        double epsilon = 1.0/(double)maxScale;
        double threshold = epsilon * (double)numItems;
        
        for (CountEntry<Integer> item : topk) {
            count += item.getFrequency();
            frequentItems.add(item.getItem());
        }

        // sum of all counters should be equal to number of items on the stream (n)
        assertEquals("Sum of all counter should be equal to stream size", count, numItems);
        
        // smallest counter value should be at most epsilon*n
        assertTrue("Smallest counter value should be at most epsilon*n, actual: "
                + lastItem.getFrequency() + " <= " + threshold, 
                lastItem.getFrequency() <= threshold);
        
        
        // calculates actual frequencies
        RealCounting<Integer> actualFreq = new RealCounting<Integer>();
        for (int v : xs) {
            actualFreq.add(v);
        }
        
        for (CountEntry<Integer> item : actualFreq.getFrequentItems()) {
            // check if all items whose count > epsilon*n have been stored
            if (item.getFrequency() > (epsilon*numItems)) {
                assertTrue("Any item whose count > epsilon*n should be stored", frequentItems.contains(item.getItem()));
            }
            
            // check if non-stored items have count <= min count
            if (!counter.contains(item.getItem())) {
                assertTrue("Count of non-stored items should be at most the min count stored", item.getFrequency() <= lastItem.getFrequency());
            }
        }
    }
    
}
