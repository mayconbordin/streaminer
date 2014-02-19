package org.streaminer.stream.frequency;

import org.streaminer.util.hash.function.HashFunction;
import org.streaminer.util.hash.factory.HashFunctionFactory;
import org.streaminer.util.hash.factory.SimpleHashFactory;
import org.streaminer.stream.frequency.util.CountEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Implementation of the CountSketch algorithm from the paper
 * 'Finding frequent items in data streams' written by 
 * 'Charikar, M., Chen, K., and Farach-colton, M. (2002)'.
 * </p>
 * 
 * @author Marcin Skirzynski (main work), Benedikt Kulmann (modifications)
 * @param <T>
 */
public class CountSketch<T> extends BaseFrequency<T> {
    /**
     * Total number of occurences of all elements counted so far.
     */
    protected long elementsCounted;

    /**
     * <p>
     * Data structure to estimate the frequency.
     * </p>
     */
    protected int[][] data;

    /**
     * <p>
     * As CountSketch also provides top-k estimation a parameter k can be provided.
     * If <= 0 top-k overhead is disabled.
     * </p>
     */
    protected int k;

    /**
     * <p>
     * Map for the top-k-algorithm.
     * </p>
     */
    protected Map<T, CountEntry<T>> topItems;

    /**
     * <p>The hash function which will be used.</p>
     */
    protected Class<? extends HashFunction<?>> functionClass;

    /**
     * <p>
     * Hashfunctions which determine the bucket to add the
     * s-hashfunctions.
     * </p>
     */
    protected List<HashFunction<T>> h;

    /**
     * Hashfunctions which determine the value which will be added.
     */
    protected List<HashFunction<T>> s;
    
    /**
     * <p>
     * Constructor of the CountSketch algorithm. This construction
     * can take quite a long time since the construction of the hashfunctions
     * is rather time-consuming.
     * </p>
     *
     * @param domain The (estim.) domain, i.e. how many different items are expected
     * @param numberOfHashFunctions The number of hashfunctions which determine a bucket
     * @param numberOfBuckets The number of buckets where a counter will be maintained
     * @param k parameter for the top-k variant. If you want to disable
     * the top-k overhead, than set k to 0 or lower
     */
    public CountSketch(int domain, int numberOfHashFunctions, int numberOfBuckets, int k) {
        this.k = k;
        this.elementsCounted = 0L;
        this.topItems = new ConcurrentHashMap<T, CountEntry<T>>();

        // Initialize data structure
        data = new int[numberOfHashFunctions][];
        for (int i = 0; i < data.length; i++) {
            data[i] = new int[numberOfBuckets];
        }

        initializeHashes(domain, numberOfHashFunctions, numberOfBuckets, new SimpleHashFactory<T>());
    }
    
    /**
     * <p>
     * Initialize all necessary hash functions.
     * </p>
     *
     * @param domain the (estim.) domain, i.e. how many different items are expected
     * @param nrOfHashFunctions the number of hashfunctions which determine a bucket
     * @param nrOfbuckets the number of buckets where a counter will be maintained
     */
    protected void initializeHashes(int domain, int nrOfHashFunctions, int nrOfbuckets, HashFunctionFactory<T> factory) {
        h = new ArrayList<HashFunction<T>>();
        s = new ArrayList<HashFunction<T>>();

        for (int i = 0; i < nrOfHashFunctions; i++) {
            h.add(factory.build(nrOfbuckets));
            s.add(factory.build(2));
//            h.add(new TwoUniversalHashFunction<T>(domain, nrOfbuckets));
//            s.add(=new TwoUniversalHashFunction<T>(domain, 2));
        }
    }

    /**
     * <p>
     * Counts the item by passing it to the internal
     * data strucutre.
     * </p>
     *
     * <p>
     * If a k greater than zero was set, the top-k
     * map will be maintained also.
     * </p>
     *
     * @param item The item to count
     * @param incrementCount
     * @return 
     */
    @Override
    public boolean add(T item, long incrementCount) {
        boolean newItem = true;
        boolean kGreaterZero = updateData(item);
        if (!kGreaterZero) {
            return newItem;
        }

        if(isTopItem(item)) {
            incrementCount(item, incrementCount);
            newItem = false;
        } else if(notYetKItems()) {
            insertTopItem(item, incrementCount);
        } else {
            /**
             * Remove the item with the lowest frequency if the new item
             * has a higher frequency.
             */
             CountEntry<T> lowFreqItem = getItemWithLowestCount();
             long estimatedFreq = estimateCount(item);
             
             if (lowFreqItem.frequency < estimatedFreq) {
                 removeTopItem(lowFreqItem.item);
                 insertTopItem(item, estimatedFreq);
             }
        }
        
        return newItem;
    }

    @Override
    public long estimateCount(T item) {
        if (isTopItem(item)) {
            return topItems.get(item).frequency;
        } else {
            return estimateFrequency(item);
        }
    }

    @Override
    public long size() {
        return elementsCounted;
    }
    
    @Override
    public Set<T> keySet() {
        return topItems.keySet();
    }

    public List<CountEntry<T>> getFrequentItems(double minSupport) {
        List<CountEntry<T>> result = new ArrayList<CountEntry<T>>();
        
        for (CountEntry entry : topItems.values()) {
            result.add(entry);
        }
        
        return result;
    }

    /**
    * <p>
    * Returns the top-k items which were
    * counted so far.
    * </p>
    *
    * <p>
    * If k was set to 0 or lower, an empty
    * Collection will be returned.
    * </p>
    *
    * @return	a collection of the top-k items
    */
    public Collection<T> getTopK() {
        if (k <= 0) {
            return new ArrayList<T>();
        }
        return topItems.keySet();
    }
    
    /**
     * <p>
     * Estimates the frequency of the provided item.
     * </p>
     *
     * @param item the item which frequency shall be estimated
     * @return the estimated frequency of the item
     */
    public long estimateFrequency(T item) {
        List<Integer> values = new ArrayList<Integer>();
        for (int i = 0; i < h.size(); i++) {
            int hi = (int) h.get(i).hash(item);
            values.add(data[i][hi]);
        }

        Collections.sort(values);

        if (values.size() % 2 == 1) {
            return values.get((values.size() + 1) / 2 - 1);
        } else {
            double lower = values.get(values.size() / 2 - 1);
            double upper = values.get(values.size() / 2);

            return (long) ((lower + upper) / 2.0);
        }

    }
    
    public boolean contains(T item) {
        return topItems.containsKey(item);
    }
    
    
    private boolean isTopItem(T item) {
        return topItems.containsKey(item);
    }

    private boolean notYetKItems() {
        return topItems.size() <= k;
    }

    private void incrementCount(T item, long incrementCount) {
        topItems.get(item).frequency += incrementCount;
        elementsCounted++;
    }

    private void insertTopItem(T item, long initialFrequncy) {
        topItems.put(item, new CountEntry<T>(item, initialFrequncy));
        elementsCounted += initialFrequncy;
    }

    private void removeTopItem(T item) {
        topItems.remove(item);
    }

    private CountEntry<T> getItemWithLowestCount() {
        return Collections.min(topItems.values(), new Comparator<CountEntry<T>>() {

            @Override
            public int compare(CountEntry<T> o1, CountEntry<T> o2) {
                return new Long(o1.frequency).compareTo(o2.frequency);
            }
        });
    }

    /**
     * <p>
     * Updates the data structure with the given item.
     * </p>
     *
     * @param item	the item to insert into the data structure
     * @return 
     */
    protected boolean updateData(T item) {
        for (int i = 0; i < h.size(); i++) {
            int hi = (int) h.get(i).hash(item);
            int si = (int) s.get(i).hash(item);
            if (si == 0) {
                si = -1;
            }

            data[i][hi] += si;
        }

        return k <= 0;
    }
}
