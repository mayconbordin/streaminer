/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.streaminer.stream.frequency;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Random;
import org.streaminer.stream.frequency.decay.DecayFormula;
import org.streaminer.stream.frequency.decay.ExpDecayFormula;

public class TimeDecayCountMinSketchTest
{
    @Test
    public void testAccuracy() throws FrequencyException
    {
        int seed = 7364181;
        Random r = new Random(seed);
        int numItems = 1000000;
        int[] xs = new int[numItems];
        int maxScale = 20;
        for (int i = 0; i < xs.length; ++i)
        {
            int scale = r.nextInt(maxScale);
            xs[i] = r.nextInt(1 << scale);
        }

        double epsOfTotalCount = 0.0001;
        double confidence = 0.99;
        DecayFormula decay = new ExpDecayFormula(60 * 60);

        TimeDecayCountMinSketch sketch = new TimeDecayCountMinSketch(epsOfTotalCount, confidence, seed, decay);
        TimeDecayRealCounting<Integer> real = new TimeDecayRealCounting<Integer>(decay);
        for (int x : xs) {
            long timestamp = System.currentTimeMillis();
            sketch.add(x, 1, timestamp);
            real.add(x, 1, timestamp);
        }


        int numErrors = 0;
        for (int i = 0; i < real.size(); ++i)
        {
            long timestamp = System.currentTimeMillis();
            double ratio = 1.0 * (sketch.estimateCount(i, timestamp) - real.estimateCount(i, timestamp)) / xs.length;
            if (ratio > 1.0001)
            {
                numErrors++;
            }
            
            //System.out.println(String.format("%d\t%f\t%f", i, real.estimateCount(i, timestamp), sketch.estimateCount(i, timestamp)));
        }
        double pCorrect = 1 - 1.0 * numErrors / real.size();
        assertTrue("Confidence not reached: required " + confidence + ", reached " + pCorrect, pCorrect > confidence);
    }

    /*
    @Test
    public void testAccuracyStrings() throws FrequencyException
    {
        int seed = 7364181;
        Random r = new Random(seed);
        int numItems = 1000000;
        String[] xs = new String[numItems];
        int maxScale = 20;
        for (int i = 0; i < xs.length; ++i)
        {
            int scale = r.nextInt(maxScale);
            xs[i] = RandomStringUtils.random(scale);
        }

        double epsOfTotalCount = 0.0001;
        double confidence = 0.99;

        CountMinSketchAlt sketch = new CountMinSketchAlt(epsOfTotalCount, confidence, seed);
        for (String x : xs)
        {
            sketch.add(x, 1);
        }

        Map<String, Long> actualFreq = new HashMap<String, Long>();
        for (String x : xs)
        {
            Long val = actualFreq.get(x);
            if (val == null)
            {
                actualFreq.put(x, 1L);
            }
            else
            {
                actualFreq.put(x, val + 1L);
            }
        }

        sketch = CountMinSketchAlt.deserialize(CountMinSketchAlt.serialize(sketch));

        int numErrors = 0;
        for (int i = 0; i < actualFreq.size(); ++i)
        {
            Long value = actualFreq.get(i);
            long lvalue = (value == null) ? 0 : value;
            double ratio = 1.0 * (sketch.estimateCount(i) - lvalue) / xs.length;
            if (ratio > 1.0001)
            {
                numErrors++;
            }
        }
        double pCorrect = 1 - 1.0 * numErrors / actualFreq.size();
        assertTrue("Confidence not reached: required " + confidence + ", reached " + pCorrect, pCorrect > confidence);
    }

*/
}
