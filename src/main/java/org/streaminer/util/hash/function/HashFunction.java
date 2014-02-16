package org.streaminer.util.hash.function;

/**
 * <p>
 * Interface for specific hash functions.
 * </p>
 * 
 * @author Marcin Skirzynski
 *
 * @param <T>
 */
public interface HashFunction<T> {
	
    /**
     * <p>
     * Computes a hash which is not unique but returns for an 
     * object x the same value.
     * </p>
     * 
     * <p>
     * If two objects are the 'same' will be determined by the 
     * {@link Object#hashCode()} and {@link Object#equals(Object)}
     * methods.
     * </p>
     * 
     * @param x		the object for which the hash shall be computed
     * @return		the hash for the object
     */
    public long hash(T x);
}
