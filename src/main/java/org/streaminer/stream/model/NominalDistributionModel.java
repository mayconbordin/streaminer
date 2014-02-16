/**
 * 
 */
package org.streaminer.stream.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * This class implements a histogram-model observed from a data stream. The input data
 * elements are regarded to be nominal elements, i.e. Java Strings.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public class NominalDistributionModel<T extends Serializable> 
	implements SelectiveDescriptionModel<T, Double>, Distribution<T> 
{

	/** The unique class ID  */
	private static final long serialVersionUID = -4642672370564928117L;

	/* A global logger for this class */
	static Logger log = LoggerFactory.getLogger( NominalDistributionModel.class );

	/* The total number of elements observed by this model */
	Integer count = 0;

	/* The maximum number of elements kept in the count-map */
	Integer max = Integer.MAX_VALUE;

	/* The map of counts, i.e. the frequencies for the observed elements */
	Map<T,Integer> counts = new LinkedHashMap<T,Integer>();


	/**
	 * Creates a new nominal distribution model with an infinite number
	 * of objects being tracked. Infinite here depends on available memory
	 * and is initially assumed as <code>Integer.MAX_VALUE</code>
	 */
	public NominalDistributionModel(){
		this( Integer.MAX_VALUE );
	}


	/**
	 * Creates a new nominal distribution model. The parameter specifies
	 * the maximum number of distinct elements that will be counted in this
	 * model.
	 * 
	 * @param maxElements
	 */
	public NominalDistributionModel( int maxElements ){
		this.max = maxElements;
		this.counts = new LinkedHashMap<T,Integer>();
		this.count = 0;
	}


	/**
	 * @see stream.model.SelectiveDescriptionModel#describe(java.lang.Object)
	 */
	@Override
	public Double describe(Serializable parameter) {
		Integer cnt = counts.get( parameter );
		if( cnt == null )
			return 0.0d;
		return cnt.doubleValue() / count;
	}


	/**
	 * Add a new value to the model.
	 * @param newVal
	 */
	public void update( T newVal ){
		if( newVal == null ){
			log.warn( "Skipping 'null' value!" );
			return;
		}

		synchronized( counts ){
			Integer cnt = counts.get( newVal );
			if( cnt == null ){
				cnt = new Integer(1);
			} else
				cnt = new Integer( cnt.intValue() + 1 );

			counts.put( newVal, cnt );
			count++;
		}
	}

	/* (non-Javadoc)
	 * @see stream.model.DistributionModel#getHistogram()
	 */
	public Map<T,Double> getHistogram(){
		Map<T,Double> map = new LinkedHashMap<T,Double>();
		for( T key : counts.keySet() )
			map.put( key, counts.get(key).doubleValue() );
		return map;
	}


	/* (non-Javadoc)
	 * @see stream.model.DistributionModel#getCount()
	 */
	public Integer getCount(){
		return count;
	}

	public Set<T> getElements(){
		return counts.keySet();
	}
	
	
	public Integer getCount( T value ){
		Integer cnt = counts.get( value );
		if( cnt == null )
			return 0;
		return cnt;
	}
	
	
	/**
	 * <p>
	 * This method trancates the size of this distribution-model by removing
	 * elements until only the given maximum number of elements resides in the
	 * count-map.
	 * </p>
	 * <p>
	 * Specifying any value &lt 1 for <code>maxElements</code> will completely
	 * prune all elements.
	 * </p>  
	 * 
	 * @param maxElements
	 */
	public void truncate( int maxElements ){
		log.trace( "Truncating distribution to {} elements", maxElements );
		if( maxElements < 1 ){
			count = 0;
			counts.clear();
			return;
		}

		synchronized( counts ){
			List<T> elements = new ArrayList<T>( counts.keySet() );
			Collections.sort( elements, new DistributionComparator( counts ) );

			log.trace( "Sorted elements: {}", elements );
			int removed = 0;
			for( int i = 0; counts.size() > maxElements; i++ ){
				Integer cnt = counts.remove( elements.get(i) );
				count = count - cnt;
				removed++;
			}
			log.debug( "removed {} elements", removed );
		}
	}


	/**
	 * This comparator can be used for sorting elements in ascending order, based
	 * on their frequencies.
	 */
	class DistributionComparator implements Comparator<T> {
		Map<T,Integer> counts;

		public DistributionComparator( Map<T,Integer> counts ){
			this.counts = counts;
		}

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(T arg0, T arg1) {
			Integer c0 = counts.get( arg0 );
			if( c0 == null )
				c0 = 0;

			Integer c1 = counts.get( arg1 );
			if( c1 == null )
				c1 = 0;

			int rc = c0.compareTo( c1 );
			if( rc == 0 ){
				return arg0.toString().compareTo( arg1.toString() );
			}

			return rc;
		}
	}


	/**
	 * @see stream.model.Distribution#prob(java.io.Serializable)
	 */
	@Override
	public Double prob(T value) {
		
		Integer cnt = counts.get( value );
		if( cnt == null )
			return 0.0d;
		
		return cnt.doubleValue() / count.doubleValue();
	}
}