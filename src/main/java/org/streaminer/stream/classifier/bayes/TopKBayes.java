/**
 * 
 */
package org.streaminer.stream.classifier.bayes;

import org.streaminer.stream.frequency.SimpleTopKCounting;
import org.streaminer.stream.model.Distribution;
import org.streaminer.stream.model.StreamDistribution;


/**
 * @author chris
 *
 */
public class TopKBayes extends NaiveBayes {

	/** The unique class ID */
	private static final long serialVersionUID = 6909521190510452944L;

	Integer k;

	/**
	 * @return the k
	 */
	public Integer getK() {
		return k;
	}

	/**
	 * @param k the k to set
	 */
	public void setK(Integer k) {
		this.k = k;
		this.classDistribution = createNominalDistribution(); //new StreamDistribution<String>( new SimpleTopKCounting( getK() ) );
	}

	/**
	 * @see stream.learner.NaiveBayes#createNominalDistribution()
	 */
	@Override
	public Distribution<String> createNominalDistribution() {
		log.debug( "Creating new nominal distribution..." );
		if( getK() == null )
			k = 100;
		
		StreamDistribution<String> sd = new StreamDistribution<String>( new SimpleTopKCounting( getK() ) );
		return sd;
	}
}