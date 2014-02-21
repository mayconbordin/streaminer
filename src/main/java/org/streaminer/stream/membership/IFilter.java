package org.streaminer.stream.membership;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public interface IFilter<T> {
    /**
     * Adds a key to <i>this</i> filter.
     * @param key The key to add.
     */
    public void add(T key);
    
    /**
     * Determines wether a specified key belongs to <i>this</i> filter.
     * @param key The key to test.
     * @return boolean True if the specified key belongs to <i>this</i> filter.
     * 		     False otherwise.
     */
    public boolean membershipTest(T key);
}
