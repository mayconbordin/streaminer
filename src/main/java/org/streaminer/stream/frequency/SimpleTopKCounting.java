package org.streaminer.stream.frequency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streaminer.stream.frequency.util.CountEntry;

/**
 * 
 * This is a simple implementation of a stream-counting model. The model is
 * updatable and will - for a given threshold <code>k</code> - approximate the
 * counts of the top-k elements within a example-set/stream.
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 * 
 */
public class SimpleTopKCounting<T> extends BaseFrequency<T> {
    private static final long serialVersionUID = 4365995573179300743L;
    private static final Logger LOG = LoggerFactory.getLogger(SimpleTopKCounting.class);

    /** This map holds the list of monitored items and their counters */
    private HashMap<T, Long> dataStructure;

    /**
     * This is the maximum number of observed items within the
     * stream/example-set
     */
    private int k;

    /** The number of elements that have been processed */
    private Long elementsCounted = 0L;

    /**
     * This initially creates a counting model for streams. The model will not
     * use more than the last <code>k</code> elements in order to approximate
     * the item counts within the stream.
     * @param k The maximum number of items that may be tracked/monitored by
     * the model.
     */
    public SimpleTopKCounting(int k) {
        this.k = k;
        this.dataStructure = new HashMap<T, Long>();
        LOG.debug("Creating top-k counter with k = {}", k);
    }

    /**
     * This method actually does all the work when learning from the stream. It
     * will update the inner structures to reflect the incoporation of the given
     * example.
     * 
     * @param item
     * @param incrementCount
     * @return 
     * @throws org.streaminer.stream.frequency.FrequencyException
     */
    @Override
    public boolean add(T item, long incrementCount) throws FrequencyException {
        boolean newItem = true;
        elementsCounted++;
        
        if (elementsCounted % 100 == 0)
            LOG.debug("   space used: {}/{}", dataStructure.size(), k);

        // is the element already in the list of our top-k monitored items?
        if (dataStructure.get(item) != null) {
            LOG.debug("Incrementing count of top-k element {}", item);
            // LogService.getGlobal().logNote( "Current top-k list is:\n" +
            // this.toResultString() );
            Long count = dataStructure.get(item) + incrementCount;
            dataStructure.put(item, count);
            newItem = false;
        } else {
            // we must not monitor more than k elements
            if (dataStructure.size() >= k) {
                LOG.debug("Need to replace the most in-frequent top-k element with {}", item);
                // LogService.getGlobal().logNote("Current top-k list is:\n" +
                // this.toResultString() );
                //
                // find the one with the smallest count and replace it
                //
                Long min = 0L;
                T leastElement = null;

                for (T key : dataStructure.keySet()) {
                    if (leastElement == null) {
                        min = dataStructure.get(key);
                        leastElement = key;
                    } else {
                        if (dataStructure.get(key) < min) {
                            min = dataStructure.get(key);
                            leastElement = key;
                        }
                    }
                }

                Long newCount = min + incrementCount;
                dataStructure.remove(leastElement);
                dataStructure.put(item, newCount);
            } else {
                // ok, there is space left in our monitor-list
                LOG.debug("Enough space to add new element {}", item);
                LOG.debug("   space used: {}/{}", dataStructure.size(), k);
                
                if (dataStructure.get(item) != null)
                    LOG.warn("Overwriting existing element with count {}", dataStructure.get(item));

                dataStructure.put(item, 1L);
            }
        }
        
        return newItem;
    }

    @Override
    public long estimateCount(T item) {
        if (dataStructure.containsKey(item))
            return dataStructure.get(item);
        return 0L;
    }

    public boolean contains(T item) {
        return dataStructure.containsKey(item);
    }

    /**
     * @return 
     * @see stream.counter.CountModel#getTotalCount()
     */
    @Override
    public long size() {
        return elementsCounted;
    }

    /**
     * @return 
     * @see stream.counter.CountModel#keySet()
     */
    @Override
    public Set<T> keySet() {
        return dataStructure.keySet();
    }

    public List<CountEntry<T>> getFrequentItems(double minSupport) {
        List<CountEntry<T>> result = new ArrayList<CountEntry<T>>();
        for (Map.Entry<T, Long> entry : dataStructure.entrySet()) {
            result.add(new CountEntry<T>(entry.getKey(), entry.getValue()));
        }
        return result;
    }
}