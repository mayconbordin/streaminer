/**
 * 
 */
package org.streaminer.stream.mapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author chris
 *
 */
public class AddMinMax implements
		IMapper<Map<String, Object>, Map<String,Object>> {

	Set<String> features;
	
	Map<String,Double> maxima = new HashMap<String,Double>();
	Map<String,Double> minima = new HashMap<String,Double>();
	
	
	public AddMinMax(){
		
	}
	
	
	/* (non-Javadoc)
	 * @see stream.data.mapper.DataMapper#map(java.lang.Object)
	 */
	@Override
	public Map<String, Object> map(Map<String, Object> input) throws Exception {
		
		detectNumericFeatures( input );
		
		for( String f : features ){
			if( input.containsKey( f ) ){
				try {
					Double val = (Double) input.get( f );
					Double max = maxima.get( f );
					if( max == null )
						max = val;
					else
						max = Math.max( max, val );
					maxima.put( f, max );
					
					Double min = minima.get( f );
					if( min == null )
						min = val;
					else
						min = Math.min( min, val );
					minima.put( f, min );
					input.put( "MAX(" + f + ")", max );
					input.put( "MIN(" + f + ")", min );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return input;
	}
	
	
	public void detectNumericFeatures( Map<String,Object> datum ){
		if( features == null )
			features = new HashSet<String>();
		
		for( String key : datum.keySet() ){
			if( ! features.contains( key ) && datum.get(key).equals( Double.class ) ){
				features.add( key );
			}
		}
	}
}