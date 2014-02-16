/**
 * 
 */
package org.streaminer.stream.classifier;

import org.streaminer.stream.learner.LearnerUtils;
import org.streaminer.stream.data.Data;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * <p>
 * This classifier predicts a random class.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public class RandomClassifier extends AbstractClassifier<Data, String> {
	/** The unique class ID */
	private static final long serialVersionUID = 3687537399872562759L;

	Random rnd = new Random();

	/* The label attribute to learn from */
	String labelAttribute = null;
	
	/* The classes from which to choose one for prediction */ 
	List<String> classes = new LinkedList<String>();
	
	/**
	 * @return the labelAttribute
	 */
	public String getLabelAttribute() {
		return labelAttribute;
	}

	/**
	 * @param labelAttribute the labelAttribute to set
	 */
	public void setLabelAttribute(String labelAttribute) {
		this.labelAttribute = labelAttribute;
	}

	/**
	 * @see stream.learner.AbstractClassifier#learn(java.lang.Object)
	 */
	@Override
	public void learn(Data item) {
		
		if( labelAttribute == null )
			labelAttribute = LearnerUtils.detectLabelAttribute( item );
		
		String clazz = "" + item.get( labelAttribute );
		if( !classes.contains( clazz ) )
			classes.add( clazz );
	}

	/**
	 * @see stream.learner.AbstractClassifier#predict(java.lang.Object)
	 */
	@Override
	public String predict(Data item) {
		if( classes.isEmpty() )
			return "null";
		
		int random = Math.abs( rnd.nextInt() );
		return classes.get( random % classes.size() );
	}
}