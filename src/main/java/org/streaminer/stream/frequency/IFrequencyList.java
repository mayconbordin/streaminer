package org.streaminer.stream.frequency;

import java.util.List;
import java.util.Set;
import org.streaminer.stream.frequency.util.CountEntry;

public interface IFrequencyList<T> extends IBaseFrequency<T> {

    
    /**
     * @return The keys of the items stored in the data structure
     */
    public Set<T> keySet();
    
    /**
     * Get the k most frequent elements.
     * @param k The maximum number of elements to be returned
     * @return A list of the most frequent items, ordered in descending order of frequency.
     */
    public List<CountEntry<T>> peek(int k);
    
    /**
     * Get the most frequent items, sorted in descending order of frequency.
     * @param k The maximum number of items to be returned
     * @param minSupport
     * @return The list with the k most frequent items
     */
    public List<CountEntry<T>> peek(int k, double minSupport);
    
    /**
     * @return The list of all the frequent items without any particular order,
     * with the default support.
     */
    public List<CountEntry<T>> getFrequentItems();
    
    /**
     * @param minSupport
     * @return The list of all the frequent items without any particular order.
     */
    public List<CountEntry<T>> getFrequentItems(double minSupport);
}