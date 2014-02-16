package org.streaminer.stream.model;

import org.streaminer.stream.frequency.FrequencyException;
import org.streaminer.stream.frequency.IRichFrequency;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * This class is a wrapper implementation and provides the adaption of different stream counting
 * algorithms to be used as approximations to distributions of nominal attributes.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public class StreamDistribution<T extends Serializable> 
	implements Serializable, Distribution<T> 
{
	/** The unique class ID */
	private static final long serialVersionUID = 2696199411688427094L;
	
	private IRichFrequency<T> counter;
	
	public StreamDistribution(){
	}

	
	public StreamDistribution( IRichFrequency<T> countAlgo ){
		this.counter = countAlgo;
	}
	
	
	public void setCounter( IRichFrequency<T> counter ){
		this.counter = counter;
	}
	
	/**
	 * @see stream.model.Distribution#getCount()
	 */
	@Override
	public Integer getCount() {
		return (int) counter.size();
	}

	/**
	 * @see stream.model.Distribution#getCount(java.io.Serializable)
	 */
	@Override
	public Integer getCount(T value) {
		return (int) counter.estimateCount(value);
	}

	/**
	 * @see stream.model.Distribution#getHistogram()
	 */
	@Override
	public Map<T, Double> getHistogram() {
		Map<T,Double> hist = new LinkedHashMap<T,Double>();
		for( T key : counter.keySet() )
			hist.put( key, (double) counter.estimateCount(key) );
		return hist;
	}

	/**
	 * @see stream.model.Distribution#prob(java.io.Serializable)
	 */
	@Override
	public Double prob(T value) {
		Double val = (double) counter.estimateCount(value);
		Long total = counter.size();
		return val / total.doubleValue();
	}

	/**
	 * @see stream.model.Distribution#update(java.io.Serializable)
	 */
	@Override
	public void update(T item) {
            try {
                counter.add( item );
            } catch (FrequencyException ex) {
                ex.printStackTrace();
            }
	}


	/**
	 * @see stream.model.Distribution#getElements()
	 */
	@Override
	public Set<T> getElements() {
            return counter.keySet();
	}
}