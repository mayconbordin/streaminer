/**
 * 
 */
package org.streaminer.stream.classifier;

import org.streaminer.stream.data.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This classifier predicts a random class.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public class MultiRandomClassifier 
	extends AbstractClassifier<Data, Map<String,String>> 
{
	/** The unique class ID */
	private static final long serialVersionUID = 3687537399872562759L;
	static Logger log = LoggerFactory.getLogger( MultiRandomClassifier.class );

	Random rnd = new Random();

	/* The label attribute to learn from */
	String labelAttribute = null;
	
	/* The classes from which to choose one for prediction */ 
	Map<String,Set<String>> classes = new HashMap<String,Set<String>>();
	
	/**
	 * @return the labelAttribute
	 */
	public String getLabelAttributes() {
		return labelAttribute;
	}

	/**
	 * @param labelAttribute the labelAttribute to set
	 */
	public void setLabelAttributes(String labelAttribute) {
		this.labelAttribute = labelAttribute;
		
		if( labelAttribute.indexOf( "," ) >= 0 ){
			for( String label : labelAttribute.split( "," ) ){
				String l = label.trim();
				if( ! "".equals( l ) ){
					classes.put( l, new HashSet<String>() );
				}
			}
		}
	}

	/**
	 * @see stream.learner.AbstractClassifier#learn(java.lang.Object)
	 */
	@Override
	public void learn(Data item) {
		
		if( classes.isEmpty() ){
			for( String key : item.keySet() )
				classes.put( key, new HashSet<String>() );
		}
		
		for( String labelAttribute : classes.keySet() ){
			
			Set<String> classLabels = classes.get( labelAttribute );
			if( classLabels == null )
				classLabels = new HashSet<String>();
			
			String clazz = "" + item.get( labelAttribute );
			if( !classes.get(labelAttribute).contains( clazz ) )
				classes.get(labelAttribute).add( clazz );
		}
	}

	/**
	 * @see stream.learner.AbstractClassifier#predict(java.lang.Object)
	 */
	@Override
	public Map<String,String> predict(Data item) {
		Map<String,String> pred = new LinkedHashMap<String,String>();
		
		if( classes.isEmpty() )
			return pred;
		
		for( String key : classes.keySet() ){
			String prediction = guess( key );
			pred.put(key, prediction );
		}

		return pred;
	}
	
	protected String guess( String label ){
		int random = Math.abs( rnd.nextInt() );
		List<String> list = new ArrayList<String>( classes.get( label ) );
		String guess = list.get( random % list.size() );
		log.debug( "Guessing {} => {}", list, guess );
		return guess;
	}
}