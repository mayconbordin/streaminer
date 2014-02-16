/**
 * 
 */
package org.streaminer.stream.mapper;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 *
 */
public class SelectFeaturesMapper implements IMapper<Map<String,Object>, Map<String,Object>> {
	Logger log = LoggerFactory.getLogger( SelectFeaturesMapper.class );
	Set<String> include = new TreeSet<String>();
	
	public SelectFeaturesMapper( Collection<String> includes ){
		include = new TreeSet<String>( includes );
	}
	

	/**
	 * @see stream.data.mapper.Mapper#map(java.lang.Object)
	 */
	@Override
	public Map<String, Object> map(Map<String, Object> input) throws Exception {
		Set<String> remove = new TreeSet<String>();
		for( String key : input.keySet() )
			if( ! include.contains(key) )
				remove.add( key );
		
		for( String key : remove )
			input.remove( key );
		log.debug( "Removed {} features from datum: {}", remove.size(), remove );
		return input;
	}
}