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
}
