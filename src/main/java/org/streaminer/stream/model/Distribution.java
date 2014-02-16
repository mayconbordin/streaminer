/**
 * 
 */
package org.streaminer.stream.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * This is the root interface for all kinds of distributions. The methods defined need
 * to be provided for nominal or numerical distribution implementations.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 * @param <T> The generic type of elements covered by this distribution.
 */
public interface Distribution<T extends Serializable> {
	
	/**
	 * Returns a histogram of this distribution.
	 * 
	 * @return
	 */
	public abstract Map<T, Double> getHistogram();
	
	
	public Set<T> getElements();
	
	
	/**
	 * Returns the total number of all observations incorporated into
	 * this model.
	 * 
	 * @return
	 */
	public abstract Integer getCount();

	
	/**
	 * Returns the number of observations that equal the given value.
	 * 
	 * @param value
	 * @return
	 */
	public abstract Integer getCount( T value );
	
	
	/**
	 * Computes the empirical probability of the given value, i.e.
	 *   count(value) / count(*)
	 * 
	 * @param value
	 * @return
	 */
	public abstract Double prob( T value );
	

	/**
	 * This method is called to incorporate a new observation into
	 * the distribution model.
	 * 
	 * @param item
	 */
	public abstract void update( T item );
}