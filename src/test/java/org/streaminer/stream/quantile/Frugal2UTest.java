package org.streaminer.stream.quantile;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;
import org.junit.Test;

/**
 *
 * @author maycon
 */
public class Frugal2UTest {
    
    @Test
    public void testOffer() throws QuantilesException {
        System.out.println("offer");

        double[] quantiles = new double[]{0.05, 0.25, 0.5, 0.75, 0.95};
        Frugal2U instance = new Frugal2U(quantiles, 0);
        ExactQuantilesAll<Integer> exact = new ExactQuantilesAll<Integer>();
        
        RandomEngine r = new MersenneTwister64(0);
        Normal dist = new Normal(100, 50, r);
        int numSamples = 1000;
                
        for(int i = 0; i < numSamples; ++i) {
            int num = (int) Math.max(0, dist.nextDouble());
            instance.offer(num);
            exact.offer(num);
        }
        
        System.out.println("Q\tEst\tExact");
        for (double q : quantiles) {
            System.out.println(q + "\t" + instance.getQuantile(q) + "\t" + exact.getQuantile(q));
        }
        
        
    }
}