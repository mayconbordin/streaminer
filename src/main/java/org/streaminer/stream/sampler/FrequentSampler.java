package org.streaminer.stream.sampler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class FrequentSampler<T> implements ISampleList<T> {
    private int k;
    private Map<T, Integer> sample;

    public FrequentSampler(int k) {
        this.k = k;
        sample = new HashMap<T, Integer>(k);
    }

    public void sample(T item) {
        if (sample.containsKey(item))
            sample.put(item, sample.get(item) + 1);
        else if (sample.size() < k)
            sample.put(item, 1);
        else {
            for (Map.Entry<T, Integer> e : sample.entrySet()) {
                if (e.getValue() == 1) sample.remove(e.getKey());
                else sample.put(e.getKey(), e.getValue() - 1);
            }
        }
    }

    public void sample(T... t) {
        for (T item : t)
            sample(item);
    }

    public Collection<T> getSamples() {
        return sample.keySet();
    }

    public int getSize() {
        return sample.size();
    }
    
    private Map.Entry<T, Integer> getMin() {
        Map.Entry<T, Integer> min = null;
        for (Map.Entry<T, Integer> e : sample.entrySet()) {
            if (min == null || e.getValue() < min.getValue())
                min = e;
        }
        
        return min;
    }
}
