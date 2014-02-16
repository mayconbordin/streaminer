/**
 * Copyright 2013 ananthc
 *
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.streaminer.stream.frequency.util.CountEntry;

/**
 * Implementation of the MisraGries frequency count algorithm.
 * 
 * Reference:
 *  Jayadev Misra and David Gries. Finding repeated elements. Sci. Comput.
 *  Program., 2(2):143–152, 1982.
 * 
 * Source: https://github.com/ananthc/streamstats
 * 
 * @author ananthc
 * @param <T> 
 */
public class MisraGries<T> extends BaseFrequency<T> {
    private int k = 1;
    private Map<T, Long> dataStructure = new HashMap<T, Long>();
    private long elementsCounted;

    public MisraGries(int k) {
        this.k = k;
    }

    @Override
    public boolean add(T item, long incrementCount) throws FrequencyException {
        boolean newItem = true;
        long count = 0;
        
        if (dataStructure.containsKey(item) ) {
            count = dataStructure.get(item) + incrementCount;
            dataStructure.put(item, count);
            newItem = false;
        } else {
            if (dataStructure.size() < k - 1) {
                dataStructure.put(item, incrementCount);
            } else {
                Iterator<Map.Entry<T, Long>> itr = dataStructure.entrySet().iterator();
                while (itr.hasNext()) {
                    Map.Entry<T, Long> entry = itr.next();
                    count = dataStructure.get(entry.getKey()) - 1;
                    if (count <= 0 ) {
                        itr.remove();
                    } else {
                        dataStructure.put(entry.getKey(), count);
                    }
                }
            }
        }
        
        return newItem;
    }

    @Override
    public long estimateCount(T item) {
        if (dataStructure.containsKey(item)) {
            return dataStructure.get(item).longValue();
        }
        return 0L;
    }

    public boolean contains(T item) {
        return dataStructure.containsKey(item);
    }
    
    @Override
    public long size() {
        return dataStructure.size();
    }
    
    @Override
    public Set<T> keySet() {
        return dataStructure.keySet();
    }
    
    public List<CountEntry<T>> getFrequentItems(double minSupport) {
        List<CountEntry<T>> frequentItems = new ArrayList<CountEntry<T>>();

        for (Map.Entry<T, Long> entry : dataStructure.entrySet()) {
            if (isFrequent(entry.getValue(), minSupport)) {
                frequentItems.add(new CountEntry<T>(entry.getKey(), entry.getValue()));
            }
        }

        return frequentItems;
    }
    
    /**
     * Determines whether a frequency is currently (i.e. in relation to the current total number
     * of elements) said to be frequent given a specific threshold.
     *
     * @param frequency The frequency in question
     * @param minSupport The threshold for determining whether a frequency is deemed to be frequent
     * @return
     */
    private boolean isFrequent(long frequency, double minSupport) {
        return frequency >= minSupport * elementsCounted;
    }
}
