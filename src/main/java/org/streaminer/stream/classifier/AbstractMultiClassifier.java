/**
 * 
 */
package org.streaminer.stream.classifier;

import org.streaminer.util.QuotedStringTokenizer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author chris
 *
 */
public abstract class AbstractMultiClassifier<D,C> extends AbstractClassifier<D, Map<C,C>> {

	/** The unique class ID */
	private static final long serialVersionUID = 4972506709366344827L;


	/* A list of attributes to focus on, may be null to learn on all attributes */
	protected List<String> attributes = null;


	/**
	 * @return the attributes
	 */
	public String getLabelAttributes() {
		if( attributes == null )
			return "";
		
		return this.join( attributes, "," );
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setLabelAttributes(String attributes) {
		if( attributes.isEmpty() )
			this.attributes = null;
		else
			this.attributes = split( attributes );
	}
	

	/**
	 * A simple convenience method to split a string by comma.
	 * 
	 * @param str
	 * @return
	 */
	protected List<String> split( String str ){
		List<String> tok = QuotedStringTokenizer.splitRespectQuotes( str, ',' );
		for( int i = 0; i < tok.size(); i++ )
			tok.set( i, tok.get(i).trim() );

		return tok;
	}


	/**
	 * A simple convenience method to join a list of strings, interleaving them
	 * with the given glue string.
	 * 
	 * @param strs
	 * @param glue
	 * @return
	 */
	protected String join( List<String> strs, String glue ){
		StringBuffer s = new StringBuffer();
		Iterator<String> it = strs.iterator();
		while( it.hasNext() ){
			s.append( it.next().trim() );
			if( it.hasNext() )
				s.append( "," );
		}
		return s.toString();
	}
}
