/**
 * 
 */
package org.streaminer.stream.classifier.bayes;

import org.streaminer.stream.data.Data;
import org.streaminer.stream.model.NominalDistributionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class implements a NaiveBayes learner for nominal values. It includes
 * a limitation on the number of distinct values counted for each observed
 * variable.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public class BoundedBayes extends NaiveBayes {

	/** The unique class ID */
	private static final long serialVersionUID = -3975602278242211790L;

	/* The global logger for this class */
	static Logger log = LoggerFactory.getLogger( BoundedBayes.class );
	
	/* This parameter determines the size of the distribution counters */
	Integer limit = -1;
	
	
	/**
	 * @return the limit
	 */
	public Integer getLimit() {
		return limit;
	}

	/**
	 * This limit determines the maximum number of elements which will be counted
	 * in the distributions.
	 * 
	 * @param limit the limit to set
	 */
	public void setLimit(Integer limit) {
		this.limit = limit;
	}
	

	/**
	 * Applies the super method of the NaiveBayes class and truncates the distribution models
	 * to the given limit, afterwards.
	 * 
	 * @see stream.learner.NaiveBayes#learn(stream.data.Data)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void learn(Data item) {
		super.learn(item);
		
		if( limit < 1 || limit == Integer.MAX_VALUE )
			return;
		
		for( String key : this.distributions.keySet() ){
			if( distributions.get( key ) instanceof NominalDistributionModel ){
				NominalDistributionModel<String> ndm = (NominalDistributionModel<String>) distributions.get(key);
				ndm.truncate( limit );
			}
		}
	}
}