/**
 * 
 */
package org.streaminer.stream.mapper;

import org.streaminer.stream.data.Data;
import java.util.Map;

/**
 * <p>
 * This class implements a mapper that will extract a <i>single</i> real-valued
 * attribute from a stream. If the data processed by this mapper does not contain
 * such an attribute, then <code>Double.NaN</code> is returned.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 */
public class ExtractDouble implements IMapper<Data, Double> {

	String attribute;
	
	public ExtractDouble(){
		this.attribute = null;
	}
	
	public ExtractDouble( String attribute ){
		this.attribute = attribute;
	}
	

	/**
	 * @see stream.data.mapper.Mapper#map(java.lang.Object)
	 */
	@Override
	public Double map(Data input) throws Exception {
		if( attribute == null )
			return returnFirstDouble( input );
		
		return (Double) input.get( attribute );
	}
	
	
	protected Double returnFirstDouble( Map<String,?> input ){
		for( String key : input.keySet() ){
			if( input.get( key ).getClass().equals( Double.class ) )
				return (Double) input.get( key );
		}
		
		return Double.NaN;
	}
}