package org.streaminer.stream.frequency;

import java.util.Collections;
import java.util.List;
import org.streaminer.stream.frequency.util.CountEntry;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 * @param <T> The type of object that will be stored
 */
public abstract class BaseFrequency<T> implements IRichFrequency<T> {
    protected double defaultMinSupport = 0.1d;

    public BaseFrequency() {
    }

    public BaseFrequency(double minSupport) {
        defaultMinSupport = minSupport;
    }
    
    @Override
    public boolean add(T item) throws FrequencyException {
        return add(item, 1);
    }
    
    public List<CountEntry<T>> peek(int k) {
        return peek(k, defaultMinSupport);
    }
    
    public List<CountEntry<T>> peek(int k, double minSupport) {
        List<CountEntry<T>> items = getFrequentItems(minSupport);

        Collections.sort(items);
        if (items.size() > k)
            return items.subList(0, k);
        else
            return items;
    }
    
    public List<CountEntry<T>> getFrequentItems() {
        return getFrequentItems(defaultMinSupport);
    }
    
    public double getDefaultMinSupport() {
        return defaultMinSupport;
    }

    public void setDefaultMinSupport(double defaultMinSupport) {
        this.defaultMinSupport = defaultMinSupport;
    }
}
