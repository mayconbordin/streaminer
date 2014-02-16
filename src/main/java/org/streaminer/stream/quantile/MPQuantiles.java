/**
 * Copyright 2011 Cloudera Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.streaminer.stream.quantile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the Munro-Paterson one-pass quantile estimation
 * algorithm, available via:
 * http://scholar.google.com/scholar?q=munro+paterson
 * 
 * <p>This implementation follows the implementation in the szl compiler:
 * http://code.google.com/p/szl/source/browse/trunk/src/emitters/szlquantile.cc
 * 
 */
public class MPQuantiles implements IQuantiles<Double> {
     private static final long MAX_TOT_ELEMS = 1024L * 1024L * 1024L * 1024L;

    private final List<List<Double>> buffer = new ArrayList<List<Double>>();
    private final int maxElementsPerBuffer;
    private final int numQuantiles;
    private int totalElements;
    private double min;
    private double max;
    
    public MPQuantiles(int numQuantiles) {
        this.numQuantiles = Math.max(2, numQuantiles);
        this.maxElementsPerBuffer = computeMaxElementsPerBuffer();
    }
    
    @Override
    public void offer(Double value) {
        if (totalElements == 0 || value < min) {
            min = value;
        }
        if (totalElements == 0 || max < value) {
            max = value;
        }

        if (totalElements > 0 && totalElements % (2 * maxElementsPerBuffer) == 0) {
            Collections.sort(buffer.get(0));
            Collections.sort(buffer.get(1));
            recursiveCollapse(buffer.get(0), 1);
        }

        ensureBuffer(0);
        ensureBuffer(1);
        int index = buffer.get(0).size() < maxElementsPerBuffer ? 0 : 1;
        buffer.get(index).add(value);
        totalElements++;
    }

    @Override
    public Double getQuantile(double q) throws QuantilesException {
        double quantileKey = 0.0;
        for (double quantileValue : getQuantiles()) {
          if (round(quantileKey) == q)
              return quantileValue;
          quantileKey += 1.0/(this.numQuantiles-1);
        }
        
        return 0.0;
    }
    
    public void clear() {
        buffer.clear();
        totalElements = 0;
    }
    
    public Map<Double, Double> getFullQuantiles() {
        Map<Double, Double> quantiles = new HashMap<Double, Double>();
        double quantileKey = 0.0;
        
        for (double quantileValue : getQuantiles()) {
            quantiles.put(round(quantileKey), quantileValue);
            quantileKey += 1.0/(this.numQuantiles-1);
        }
        return quantiles;
    }
    
    public List<Double> getQuantiles() {
        List<Double> quantiles = new ArrayList<Double>();
        quantiles.add(min);

        if (buffer.get(0) != null) {
            Collections.sort(buffer.get(0));
        }
        if (buffer.get(1) != null) {
            Collections.sort(buffer.get(1));
        }

        int[] index = new int[buffer.size()];
        long S = 0;
        for (int i = 1; i <= numQuantiles - 2; i++) {
            long targetS = (long) Math.ceil(i * (totalElements / (numQuantiles - 1.0)));

            while (true) {
                double smallest = max;
                int minBufferId = -1;
                for (int j = 0; j < buffer.size(); j++) {
                    if (buffer.get(j) != null && index[j] < buffer.get(j).size()) {
                        if (!(smallest < buffer.get(j).get(index[j]))) {
                            smallest = buffer.get(j).get(index[j]);
                            minBufferId = j;
                        }
                    }
                }

                long incrementS = minBufferId <= 1 ? 1L : (0x1L << (minBufferId - 1));
                if (S + incrementS >= targetS) {
                    quantiles.add(smallest);
                    break;
                } else {
                    index[minBufferId]++;
                    S += incrementS;
                }
            }
        }

        quantiles.add(max);
        return quantiles;
    }
    
    private int computeMaxElementsPerBuffer() {
        double epsilon = 1.0 / (numQuantiles - 1.0);
        int b = 2;
        while ((b - 2) * (0x1L << (b - 2)) + 0.5 <= epsilon * MAX_TOT_ELEMS) {
          ++b;
        }
        return (int) (MAX_TOT_ELEMS / (0x1L << (b - 1)));
    }
  
    private void ensureBuffer(int level) {
        while (buffer.size() < level + 1) {
            buffer.add(null);
        }
        if (buffer.get(level) == null) {
            buffer.set(level, new ArrayList<Double>());
        }
    }
  
    private void collapse(List<Double> a, List<Double> b, List<Double> out) {
        int indexA = 0, indexB = 0, count = 0;
        Double smaller = null;
        while (indexA < maxElementsPerBuffer || indexB < maxElementsPerBuffer) {
            if (indexA >= maxElementsPerBuffer || 
                    (indexB < maxElementsPerBuffer && a.get(indexA) >= b.get(indexB))) {
                smaller = b.get(indexB++);
            } else {
                smaller = a.get(indexA++);
            }

            if (count++ % 2 == 0) {
                out.add(smaller);
            }
        }
        a.clear();
        b.clear();
    }
  
    private void recursiveCollapse(List<Double> buf, int level) {
        ensureBuffer(level + 1);

        List<Double> merged;
        if (buffer.get(level + 1).isEmpty()) {
            merged = buffer.get(level + 1);
        } else {
            merged = new ArrayList<Double>(maxElementsPerBuffer);
        }

        collapse(buffer.get(level), buf, merged);
        if (buffer.get(level + 1) != merged) {
            recursiveCollapse(merged, level + 1);
        }
    }
    
    private static double round(double d) {
        return Math.round(d*100000.0)/100000.0;
    }
}
