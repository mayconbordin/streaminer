/**
 * 
 */
package org.streaminer.stream.classifier.bayes;

import org.streaminer.stream.frequency.LossyCounting;
import org.streaminer.stream.model.Distribution;
import org.streaminer.stream.model.StreamDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class extends the NaiveBayes implementation but overrides the methods which
 * are responsible for creating the distribution estimators. This implementation of
 * NaiveBayes provides several strategies for estimating an attribute's distribution,
 * i.e. different approximative counters for nominal attributes, quantile estimators
 * for numerical attributes, etc. 
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public class LossyBayes extends NaiveBayes {

	/** The unique class ID */
	private static final long serialVersionUID = -3975602278242211790L;

	/* The global logger for this class */
	static Logger log = LoggerFactory.getLogger( LossyBayes.class );
	
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
	 * @see stream.learner.NaiveBayes#createNominalDistribution()
	 */
	@Override
	public Distribution<String> createNominalDistribution() {
		
		Double eps = getEpsilon();
		if( eps == null ){
			eps = 0.01;
			log.warn( "No value set for parameter 'epsilon', using default: {}", eps );
		}
		LossyCounting<String> lossyCounting = new LossyCounting<String>( eps );
		return new StreamDistribution<String>( lossyCounting );
	}

	
	/**
	 * @see stream.learner.NaiveBayes#createNumericalDistribution()
	 */
	@Override
	public Distribution<Double> createNumericalDistribution() {
		return super.createNumericalDistribution();
	}
}