package org.streaminer.stream.frequency.topk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.streaminer.stream.frequency.FrequencyException;
import org.streaminer.stream.frequency.util.CountEntry;

/**
 * Frequent algorithm.
 * https://github.com/gdusbabek/signalbrook
 * 
 * @author Gary Dusbabek
 * @param <T> 
 */
public class Frequent<T> implements ITopK<T> {
    private long elementsCounted = 0;
    private boolean over = false;
    private int k;
    private final Map<T, AtomicLong> dataStructure;
    
    public Frequent(double error) {
        this.k = (int)Math.ceil(1d / error);
        dataStructure = new HashMap<T, AtomicLong>(k - 1);
    }
    
    @Override
    public boolean add(T item) throws FrequencyException {
        return add(item, 1);
    }
    
    @Override
    public boolean add(T item, long incrementCount) throws FrequencyException {
        if (elementsCounted == Long.MAX_VALUE) {
            throw new FrequencyException("Overflowed " + Long.MAX_VALUE);
        } else {
            elementsCounted += 1;
        }
        
        AtomicLong counter = dataStructure.get(item);
        if (counter != null) {
            counter.addAndGet(incrementCount);
            return false;
        } else if (dataStructure.size() < k) {
            dataStructure.put(item, new AtomicLong(incrementCount));
        } else {
            long newValue;
            List<T> toRemove = new ArrayList<T>();
            
            // decrement every body.
            for (Map.Entry<T, AtomicLong> entry : dataStructure.entrySet()) {
                newValue = entry.getValue().decrementAndGet();
                if (newValue == 0) {
                    toRemove.add(entry.getKey());
                }
            }
            
            // remove counters at zero.
            if (toRemove.size() > 0) {
                for (T t : toRemove) {
                    dataStructure.remove(t);
                }
            }
        }
        
        return true;
    }
    
    @Override
    public List<CountEntry<T>> peek(int k) {
        List<CountEntry<T>> list = new ArrayList<CountEntry<T>>();
        for (Map.Entry<T, AtomicLong> entry : dataStructure.entrySet()) {
            list.add(new CountEntry<T>(entry.getKey(), entry.getValue().get()));
        }
        Collections.sort(list);
        return list;
    }

    public long size() {
        return elementsCounted;
    }
    
}