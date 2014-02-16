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

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import org.streaminer.stream.frequency.CountMinSketchAlt.CMSMergeException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class CountMinSketchAltTest
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

        CountMinSketchAlt sketch = new CountMinSketchAlt(epsOfTotalCount, confidence, seed);
        for (int x : xs)
        {
            sketch.add(x, 1);
        }

        int[] actualFreq = new int[1 << maxScale];
        for (int x : xs)
        {
            actualFreq[x]++;
        }

        sketch = CountMinSketchAlt.deserialize(CountMinSketchAlt.serialize(sketch));

        int numErrors = 0;
        for (int i = 0; i < actualFreq.length; ++i)
        {
            double ratio = 1.0 * (sketch.estimateCount(i) - actualFreq[i]) / xs.length;
            if (ratio > 1.0001)
            {
                numErrors++;
            }
        }
        double pCorrect = 1 - 1.0 * numErrors / actualFreq.length;
        assertTrue("Confidence not reached: required " + confidence + ", reached " + pCorrect, pCorrect > confidence);
    }

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


    @Test
    public void merge() throws CMSMergeException, FrequencyException
    {
        int numToMerge = 5;
        int cardinality = 1000000;

        double epsOfTotalCount = 0.0001;
        double confidence = 0.99;
        int seed = 7364181;

        int maxScale = 20;
        Random r = new Random();
        TreeSet<Integer> vals = new TreeSet<Integer>();

        CountMinSketchAlt baseline = new CountMinSketchAlt(epsOfTotalCount, confidence, seed);
        CountMinSketchAlt[] sketchs = new CountMinSketchAlt[numToMerge];
        for (int i = 0; i < numToMerge; i++)
        {
            sketchs[i] = new CountMinSketchAlt(epsOfTotalCount, confidence, seed);
            for (int j = 0; j < cardinality; j++)
            {
                int scale    = r.nextInt(maxScale);
                int val      = r.nextInt(1 << scale);
                vals.add(val);
                sketchs[i].add(val, 1);
                baseline.add(val, 1);
            }
        }

        CountMinSketchAlt merged = CountMinSketchAlt.merge(sketchs);

        assertEquals(baseline.size(), merged.size());
        assertEquals(baseline.getConfidence(), merged.getConfidence(), baseline.getConfidence() / 100);
        assertEquals(baseline.getRelativeError(), merged.getRelativeError(), baseline.getRelativeError() / 100);
        for (int val : vals)
        {
            assertEquals(baseline.estimateCount(val), merged.estimateCount(val));
        }
    }

    @Test
    public void testMergeEmpty() throws CMSMergeException
    {
        assertNull(CountMinSketchAlt.merge());
    }

    @Test(expected = CMSMergeException.class)
    public void testUncompatibleMerge() throws CMSMergeException
    {
        CountMinSketchAlt cms1 = new CountMinSketchAlt(1, 1, 0);
        CountMinSketchAlt cms2 = new CountMinSketchAlt(0.1, 0.1, 0);
        CountMinSketchAlt.merge(cms1, cms2);
    }
}
