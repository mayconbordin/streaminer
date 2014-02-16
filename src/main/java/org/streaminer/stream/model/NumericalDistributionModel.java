/**
 * 
 */
package org.streaminer.stream.model;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * This class implements a histogram-model observed from a data stream.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public class NumericalDistributionModel 
	implements SelectiveDescriptionModel<Double, Double>, Distribution<Double> {

	/** The unique class ID  */
	private static final long serialVersionUID = -4642672370564928117L;
	static Logger log = LoggerFactory.getLogger( NumericalDistributionModel.class );
	
	Integer count = 0;
	Double span = 1.0d;
	Double interval = 0.1d;
	Double lowerBound = 0.0d;
	Integer[] histogram;
	
	
	/**
	 * Creates a new distribution model. The specified parameter determines
	 * the interval of the bins, that are created.
	 * 
	 * @param interval
	 */
	public NumericalDistributionModel( Integer binCount, Double span ){
		interval = span / binCount.doubleValue();
		log.info( "bin-interval is {}", interval );
		histogram = new Integer[ binCount ];
		
		for( int i = 0; i < histogram.length; i++ )
			histogram[i] = 0;
	}
	
	
	/**
	 * @see stream.model.SelectiveDescriptionModel#describe(java.lang.Object)
	 */
	@Override
	public Double describe(Double parameter) {
		int idx = findInterval( parameter );
		return histogram[idx].doubleValue();
	}


	/**
	 * Add a new value to the model.
	 * @param newVal
	 */
	public void update( Double newVal ){
		Double val = normalize( newVal );
		int idx = findInterval( val );
		//log.info( "interval for value {} is {}", val, idx );
		histogram[idx]++;
		count++;
	}
	

	/**
	 * Find the interval for the given double value.
	 * 
	 * @param val
	 * @return
	 */
	protected int findInterval( Double val ){
		for( int i = 0; i < histogram.length; i++ ){
			if( (lowerBound + i * this.interval) >= val )
				return Math.max( 0, i - 1 );
		}
		return histogram.length - 1;
	}
	
	
	public Double getInterval(){
		return this.interval;
	}
	
	public Set<Double> getElements(){
		Set<Double> el = new TreeSet<Double>();
		for( int i = 0; i < histogram.length; i++ )
			el.add( histogram[i].doubleValue() );
		return el;
	}
	
	public Map<Double,Double> getHistogram(){
		Map<Double,Double> d = new TreeMap<Double,Double>();
		Double total = count.doubleValue();
		if( total == 0.0d )
			total = 1.0d;
		
		for( int i = 0; i < histogram.length; i++ )
			d.put( histogram[i].doubleValue(),  histogram[i].doubleValue() / total );
		
		return d;
	}
	
	
	public Double normalize( Double val ){
		return val / span;
	}


	/**
	 * @see stream.model.Distribution#getCount()
	 */
	@Override
	public Integer getCount() {
		return count;
	}


	/**
	 * @see stream.model.Distribution#prob(java.io.Serializable)
	 */
	@Override
	public Double prob(Double value) {
		int i = this.findInterval( value );
		Double cnt = this.histogram[i].doubleValue();
		return cnt / count.doubleValue();
	}


	/**
	 * @see stream.model.Distribution#getCount(java.io.Serializable)
	 */
	@Override
	public Integer getCount(Double value) {
		int i = this.findInterval( value );
		return this.histogram[i];
	}
}