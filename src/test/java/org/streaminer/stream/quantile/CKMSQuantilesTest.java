/*
   Copyright 2012 Andrew Wang (andrew@umbrant.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.streaminer.stream.quantile;

import org.streaminer.stream.quantile.CKMSQuantiles.Quantile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;


import org.junit.Test;

public class CKMSQuantilesTest {

  @Test
  public void TestCKMS() {
  
    final int window_size = 1000000;
    boolean generator = false;
  
    List<Quantile> quantiles = new ArrayList<Quantile>();
    quantiles.add(new Quantile(0.50, 0.050));
    quantiles.add(new Quantile(0.90, 0.010));
    quantiles.add(new Quantile(0.95, 0.005));
    quantiles.add(new Quantile(0.99, 0.001));
  
    CKMSQuantiles estimator = new CKMSQuantiles(
        quantiles.toArray(new Quantile[] {}));
  
    System.out.println("Inserting into estimator...");
  
    long startTime = System.currentTimeMillis();
    Random rand = new Random(0xDEADBEEF);
  
    if (generator) {
      for (int i = 0; i < window_size; i++) {
        estimator.offer(rand.nextLong());
      }
    } else {
      Long[] shuffle = new Long[window_size];
      for (int i = 0; i < shuffle.length; i++) {
        shuffle[i] = (long) i;
      }
      Collections.shuffle(Arrays.asList(shuffle), rand);
      for (long l : shuffle) {
        estimator.offer(l);
      }
    }
  
    for (Quantile quantile : quantiles) {
      double q = quantile.quantile;
      try {
        long estimate = estimator.getQuantile(q);
        long actual = (long) ((q) * (window_size - 1));
        double off = ((double) Math.abs(actual - estimate)) / (double) window_size;
        System.out.println(String.format("Q(%.2f, %.3f) was %d (off by %.3f)",
            quantile.quantile, quantile.error, estimate, off));
      } catch (QuantilesException e) {
        System.out.println("No samples were present, could not query quantile.");
      }
    }
    System.out.println("# of samples: " + estimator.sample.size());
    System.out.println("Time (ms): " + (System.currentTimeMillis() - startTime));
  }
}