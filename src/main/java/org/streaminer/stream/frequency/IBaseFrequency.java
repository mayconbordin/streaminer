package org.streaminer.stream.frequency;

import java.util.List;
import org.streaminer.stream.frequency.util.CountEntry;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 * @param <T> The type of item to be stored
 */
public interface IBaseFrequency<T> {
    /**
     * Add a single element to the data structure.
     *
     * @param item the element to add to the data structure
     * @return false if the element was already in the top
     * @throws org.streaminer.stream.frequency.FrequencyException
     */
    public boolean add(T item) throws FrequencyException;
    
    /**
     * Offer a single element to the data structure and increment the count
     * for that element by incrementCount.
     *
     * @param item the element to add to the data structure
     * @param incrementCount the increment count for the given count
     * @return false if the element was already in the top
     * @throws org.streaminer.stream.frequency.FrequencyException
     */
    public boolean add(T item, long incrementCount) throws FrequencyException;
    
    /**
     * @return The number of elements in the data structure.
     */
    public long size();
}
