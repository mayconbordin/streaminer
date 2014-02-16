/**
 * 
 */
package org.streaminer.stream.classifier.bayes;

import org.streaminer.stream.frequency.StickySampling;
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
public class StickySamplingBayes extends NaiveBayes {

	/** The unique class ID */
	private static final long serialVersionUID = -3975602278242211790L;

	/* The global logger for this class */
	static Logger log = LoggerFactory.getLogger( StickySamplingBayes.class );
	
	Double support;
	Double error;
	Double probabilityOfFailure;
	
	
	/**
	 * @return the support
	 */
	public Double getSupport() {
		return support;
	}

	/**
	 * @param support the support to set
	 */
	public void setSupport(Double support) {
		log.info( "Setting support to {}", support );
		this.support = support;
	}

	/**
	 * @return the error
	 */
	public Double getError() {
		return error;
	}


	/**
	 * @param error the error to set
	 */
	public void setError(Double error) {
		log.info( "Setting error to {}", error );
		this.error = error;
	}


	/**
	 * @return the probabilityOfFailure
	 */
	public Double getProbabilityOfFailure() {
		return probabilityOfFailure;
	}


	/**
	 * @param probabilityOfFailure the probabilityOfFailure to set
	 */
	public void setProbabilityOfFailure(Double probabilityOfFailure) {
		this.probabilityOfFailure = probabilityOfFailure;
	}


	/**
	 * @see stream.learner.NaiveBayes#createNominalDistribution()
	 */
	@Override
	public Distribution<String> createNominalDistribution() {
		log.info( "Creating new CountSketchDistribution..." );
		
		Double sup = 0.01;
		if( getSupport() != null )
			sup = getSupport();
		else
			log.warn( "Parameter 'support' not specified, using default: {}", sup );
		
		Double err = 0.01;
		if( getError() != null )
			err = getError();
		else
			log.warn( "Parameter 'error' not specified, using default: {}", err );
		
		Double pof = 0.01;
		if( getProbabilityOfFailure() != null ){
			pof = getProbabilityOfFailure();
		} else
			log.warn( "Parameter 'probabilityOfFailure' not specified, using default: {}", pof );
		
		StickySampling<String> stickySampling = new StickySampling<String>( sup, err, pof );
		return new StreamDistribution<String>( stickySampling );
	}

	
	/**
	 * @see stream.learner.NaiveBayes#createNumericalDistribution()
	 */
	@Override
	public Distribution<Double> createNumericalDistribution() {
		return super.createNumericalDistribution();
	}
}