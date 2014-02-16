package org.streaminer.stream.frequency;

import org.streaminer.stream.frequency.util.CountEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * A naive counter implementation. Simply contains a counter
 * for each element which will be incremented in a deterministic way within the learn
 * method. Of course the purpose of this implementation is not a "live environment". Instead
 * it is intended to be used as "the truth" for evaluation intents.
 * </p>
 *
 * @author Benedikt Kulmann, office@kulmann.biz
 * @param <T>
 */
public class RealCounting<T> extends BaseFrequency<T> {
    /**
     * Top-K parameter for the invokation of {@link #getTopK()}.
     */
    private int k;

    /**
     * Threshold value for the invokation of {@link #getFrequentItems()}.
     */
    private double minSupport;

    /**
     * Total number of occurences of all elements so far.
     */
    private long elementsCounted;

    /**
     * Internal data structure for the count frequencies of each element.
     */
    private Map<T, CountEntry<T>> dataStructure;
    
    public RealCounting() {
        this(0.0);
    }

    public RealCounting(double minSupport) {
        this(minSupport, 0);
    }
    
    public RealCounting(int k) {
        this(0.0, k);
    }

    public RealCounting(double minSupport, int k) {
        super(minSupport);
        this.minSupport = minSupport;
        this.k = k;
        
        elementsCounted = 0;
        dataStructure = new ConcurrentHashMap<T, CountEntry<T>>();
    }

    @Override
    public boolean add(T item, long incrementCount) {
        if(containsItem(item)) {
            incrementCount(item, incrementCount);
            return false;
        } else {
            insertItem(item, incrementCount);
            return true;
        }
    }

    @Override
    public long estimateCount(T item) {
        if (dataStructure.containsKey(item)) {
            return dataStructure.get(item).frequency;
        } else {
            return 0L;
        }
    }

    public boolean contains(T item) {
        return dataStructure.containsKey(item);
    }

    @Override
    public long size() {
        return elementsCounted;
    }
    
    @Override
    public Set<T> keySet() {
        return dataStructure.keySet();
    }

    public List<CountEntry<T>> getFrequentItems(double minSupport) {
        List<CountEntry<T>> frequentItems = new ArrayList<CountEntry<T>>();

        for (CountEntry<T> entry : dataStructure.values()) {
            if (isFrequent(entry.frequency, minSupport)) {
                frequentItems.add(entry);
            }
        }

        return frequentItems;
    }
    
    /**
     * Returns whether the internal data structure already contains a counter for
     * the provided item.
     *
     * @param item The item for which the existence of a counter is in question.
     * @return <code>true</code> if a counter for the provided item already exists, false otherwise.
     */
    private boolean containsItem(T item) {
        return dataStructure.containsKey(item);
    }

    /**
     * Increments the counter of the provided item by 1.
     *
     * @param item Ttem in question.
     */
    private void incrementCount(T item, long incrementCount) {
        dataStructure.get(item).frequency += incrementCount;
        elementsCounted++;
    }

    /**
     * Creates a counter for the provided item and sets its initial frequency to 1.
     *
     * @param item The item to insert into the internal data structure.
     */
    private void insertItem(T item, long incrementCount) {
        dataStructure.put(item, new CountEntry<T>(item, incrementCount));
        elementsCounted++;
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RealCountingModel[");
        for(T key : dataStructure.keySet()) {
            sb.append(key).append(" ").append(dataStructure.get(key)).append(";");
        }
        sb.append("]");
        return sb.toString();
    }
}
