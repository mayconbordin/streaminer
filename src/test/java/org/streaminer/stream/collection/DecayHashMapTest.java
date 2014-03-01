package org.streaminer.stream.collection;


import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class DecayHashMapTest {

   @Test
   public void testHashMapWithExpFormula() throws InterruptedException {
       DecayFormula formula = new ExpDecayFormula(1);
       final Map<String, Quantity> testInt = new DecayHashMap<String>(formula);
       final Map<String, Quantity> testLong = new DecayHashMap<String>(formula);
       final Map<String, Quantity> testDouble = new DecayHashMap<String>(formula);

       Long now = System.currentTimeMillis()-1000;
       testInt.put("i", new Quantity(100, now));
       testLong.put("l", new Quantity(100, now));
       testDouble.put("d", new Quantity(100.0, now));

       Long presentValue = Math.round(testInt.get("i").valueNow());       
       Long presentLongValue = Math.round(testLong.get("l").valueNow());
       Double presentDoubleValue = testDouble.get("d").valueNow();

       Assert.assertEquals(new Long(50), presentValue );
       Assert.assertEquals(new Long(50), presentLongValue); 
       Assert.assertTrue(new Double(49.5) < presentDoubleValue); 
   }

   @Test
   public void testConcurrentAccess() throws InterruptedException {

       final Map<String, Quantity> map = new DecayHashMap<String>(
         new DecayFormula() {
            public Double evaluate(Double value, Double t) {
               return value;
            }
         }
       );
       map.put("a1", new Quantity(0));
       ExecutorService e = Executors.newFixedThreadPool(10);
       for(int i=0; i<1000; i++) {
           e.submit(new Runnable() {
               public void run() {
                   map.put("a1", new Quantity(1));
               }
           });
       }
       e.shutdown(); 
       e.awaitTermination(1, TimeUnit.SECONDS);
       e.shutdownNow();
       Assert.assertEquals(1000.0, map.get("a1").valueNow());

   }
}
