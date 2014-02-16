package org.streaminer.stream.frequency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.streaminer.stream.frequency.util.CountEntry;

/**
 * Majority algorithm.
 * https://github.com/gdusbabek/signalbrook
 * 
 * @author Gary Dusbabek
 */
public class Majority extends BaseFrequency<Boolean> {
    private long n = 0;
    private boolean current = true;
    private long counter = 0;

    @Override
    public boolean add(Boolean item, long incrementCount) throws FrequencyException {
        boolean newItem = true;
        
        if (n == Long.MAX_VALUE) {
            throw new FrequencyException("Overflowed " + Long.MAX_VALUE);
        } else {
            n += 1;
        }
        
        if (current == item) {
            counter += 1;
            newItem = false;
        } else if (counter == 0) {
            current = item;
            counter = 1;
        } else {
            counter -= 1;
        }
        
        return newItem;
    }

    @Override
    public long estimateCount(Boolean item) {
        // invariant: major + minor == n.
        // this is busted after overflow, btw.
        long major = n / 2;
        long minor = n / 2 + (n % 2);
        while (major - minor != counter) {
            major += 1;
            minor -= 1;
        }
        return current == item ? major : minor;
    }

    @Override
    public long size() {
        return n;
    }
    
    @Override
    public Set<Boolean> keySet() {
        return null;
    }
    
    public boolean isMajority(boolean b) {
        return current == b && counter > 0;
    }

    public List<CountEntry<Boolean>> getFrequentItems(double minSupport) {
        List<CountEntry<Boolean>> list = new ArrayList<CountEntry<Boolean>>(2);
        list.add(new CountEntry<Boolean>(Boolean.TRUE, estimateCount(Boolean.TRUE)));
        list.add(new CountEntry<Boolean>(Boolean.FALSE, estimateCount(Boolean.FALSE)));
        Collections.sort(list);
        return list;
    }
    
    public boolean contains(Boolean item) {
        return estimateCount(item) > 0;
    }
}
