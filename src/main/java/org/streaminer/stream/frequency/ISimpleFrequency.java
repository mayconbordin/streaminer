package org.streaminer.stream.frequency;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 * @param <T>
 */
public interface ISimpleFrequency<T> extends IBaseFrequency<T> {
    /**
     * Estimate the frequency for the given item.
     * @param item The item that will have the frequency estimated
     * @return The estimated frequency for the item
     */
    public long estimateCount(T item);
    
    /**
     * Tells whether the data structure contains the given item or not.
     * @param item The item to be verified
     * @return True if the item is in the data structure or false otherwise
     */
    public boolean contains(T item);
}
