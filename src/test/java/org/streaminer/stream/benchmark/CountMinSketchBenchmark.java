
package org.streaminer.stream.benchmark;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.streaminer.stream.frequency.decay.DecayFormula;
import org.streaminer.stream.frequency.decay.ExpDecayFormula;
import org.streaminer.stream.frequency.TimeDecayRealCounting;
import org.streaminer.stream.frequency.decay.Quantity;
import org.streaminer.stream.frequency.CountMinSketch;
import org.streaminer.stream.frequency.CountMinSketchAlt;
import org.streaminer.stream.frequency.FrequencyException;
import org.streaminer.stream.frequency.RealCounting;
import org.streaminer.stream.frequency.TimeDecayCountMinSketch;
import org.streaminer.stream.frequency.util.CountEntry;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class CountMinSketchBenchmark {
    
    public CountMinSketchBenchmark() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    @Ignore
    public void accuracy() throws IOException, FrequencyException {
        int seed = 7364181;
        double epsOfTotalCount = 0.0001;
        double confidence = 0.99;
        CountMinSketchAlt sketch = new CountMinSketchAlt(epsOfTotalCount, confidence, seed);
        RealCounting<String> exact = new RealCounting<String>();
        
        
        BufferedReader br = new BufferedReader(new FileReader("/home/maycon/Downloads/track2/sample.txt"));
        String line;
        long numItems = 0;
        while ((line = br.readLine()) != null) {
            String[] record = line.split("\t");
            
            String key = record[7] + ":" + record[3];

            sketch.add(key);
            exact.add(key);
            
            numItems++;
        }

        int numErrors = 0;
        for (String key : exact.keySet()) {
            long real = exact.estimateCount(key);
            long aprox = sketch.estimateCount(key);
            
            double ratio = 1.0 * (sketch.estimateCount(key) - exact.estimateCount(key)) / numItems;
            if (ratio > 1.0001) {
                numErrors++;
            }
        }
        
        double pCorrect = 1 - 1.0 * numErrors / exact.keySet().size();
        assertTrue("Confidence not reached: required " + confidence + ", reached " + pCorrect, pCorrect > confidence);
        
        System.out.println(String.format("Required confidence %f, reached %f", confidence, pCorrect));
    }
    
    @Test
    public void timeDecayCountMinSketch() throws IOException, FrequencyException {
        int seed = 7364181;
        double epsOfTotalCount = 0.0001;
        double confidence = 0.99;

        DecayFormula decay = new ExpDecayFormula(60 * 60);
        TimeDecayRealCounting<String> map = new TimeDecayRealCounting(decay);
        TimeDecayCountMinSketch timeSketch = new TimeDecayCountMinSketch(epsOfTotalCount, confidence, seed, decay);
        RealCounting<String> exact = new RealCounting<String>();
        CountMinSketchAlt sketch = new CountMinSketchAlt(epsOfTotalCount, confidence, seed);        
        
        BufferedReader br = new BufferedReader(new FileReader("/home/mayconbordin/Projects/datasets/ctr/track2/sample.txt"));
        String line;
        long numItems = 0;
        while ((line = br.readLine()) != null) {
            String[] record = line.split("\t");
            
            String key = record[7] + ":" + record[3];
            long timestamp = System.currentTimeMillis();
            
            map.add(key, 1, timestamp);
            exact.add(key);
            timeSketch.add(key, 1, timestamp);
            sketch.add(key);
            
            numItems++;
        }

        for (String key : map.keySet()) {
            if (exact.estimateCount(key) < 20) continue;
            
            long timestamp = System.currentTimeMillis();
            System.out.println(String.format("[%s]\t%d\t%f\t%f\t%d", key, exact.estimateCount(key), map.estimateCount(key, timestamp), timeSketch.estimateCount(key, timestamp), sketch.estimateCount(key)));
        }
        
        
        
        ////////////////////////////////////////////////////////////////////////
        int numErrors = 0;
        for (String key : exact.keySet()) {
            long real = exact.estimateCount(key);
            long aprox = sketch.estimateCount(key);
            
            double ratio = 1.0 * (sketch.estimateCount(key) - exact.estimateCount(key)) / numItems;
            if (ratio > 1.0001) {
                numErrors++;
            }
        }
        
        double pCorrect = 1 - 1.0 * numErrors / exact.keySet().size();
        //assertTrue("Confidence not reached: required " + confidence + ", reached " + pCorrect, pCorrect > confidence);
        
        System.out.println(String.format("Required confidence %f, reached %f", confidence, pCorrect));
    }
    
}
