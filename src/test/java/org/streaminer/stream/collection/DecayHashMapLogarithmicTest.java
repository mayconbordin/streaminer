package org.streaminer.stream.collection;

import java.util.Map;

public class DecayHashMapLogarithmicTest {

    static public void main(String[] arg) throws InterruptedException {
        Integer lifeTime = 10;
        
        DecayFormula formula = new LogDecayFormula(lifeTime);
        
        Map<String,Quantity> decayMap = new DecayHashMap<String>(formula);
        
        Quantity q = new Quantity(lifeTime, formula);        
        
        decayMap.put("@", new Quantity(lifeTime));
        
        long t = System.currentTimeMillis();
        boolean added = false;
        while (System.currentTimeMillis() - t < 10 * lifeTime * 1000) {
            long d = (System.currentTimeMillis() - t) / 1000; 
            Double value = q.valueNow(); 
            System.out.println(d + " " + value+ " map:" + decayMap.get("@"));

            if (!added && System.currentTimeMillis() - t > lifeTime * 500) {
                added = true;
                System.err.println("adding");
                q.add(new Quantity(1000, formula));
                decayMap.put("@", new Quantity(lifeTime));
            }
            Thread.sleep(250);
        }
        
    }
}
