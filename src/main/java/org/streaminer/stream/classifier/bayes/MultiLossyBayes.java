/**
 * 
 */
package org.streaminer.stream.classifier.bayes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This implementation of the MultiBayes learner uses the Lossy-Counting Bayes
 * implementation, which counts at fixed memory space.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public class MultiLossyBayes extends MultiBayes {

	/** The unique class ID */
	private static final long serialVersionUID = 1354945765610306076L;

	/* A global logger for this class */
	static Logger log = LoggerFactory.getLogger( MultiLossyBayes.class );
	
	Double epsilon;

	/**
	 * @return the epsilon
	 */
	public Double getEpsilon() {
		return epsilon;
	}

	/**
	 * @param epsilon the epsilon to set
	 */
	public void setEpsilon(Double epsilon) {
		this.epsilon = epsilon;
	}
	

	/**
	 * @see stream.learner.MultiBayes#createBayesLearner(java.lang.String)
	 */
	@Override
	protected NaiveBayes createBayesLearner(String attribute) {
		log.info( "Creating new lossy-bayes for attribute {}", attribute );
		LossyBayes lb = new LossyBayes();
		
		if( getEpsilon() == null ){
			log.warn( "No value set for parameter 'epsilon', using default: {}", getEpsilon() );
			setEpsilon( 0.05 );
		}
		lb.setEpsilon( getEpsilon() );
		lb.setLabelAttribute( attribute );
		return lb;
	}
}