/**
 * 
 */
package org.streaminer.stream.classifier.tree;


/**
 * <p>
 * This class implements a split criterion based on the Chernoff bound.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 *
 */
public class ChernoffSplitCriterion<S extends ChernoffStatistics> 
	implements SplitCriterion<S> 
{
	/* */
	Double delta = 0.01d;
	
	/* The required minimum error for the split */
	Double epsilon = 0.1d;
	
	/**
	 * @return the delta
	 */
	public Double getDelta() {
		return delta;
	}


	/**
	 * @param delta the delta to set
	 */
	public void setDelta(Double delta) {
		this.delta = delta;
	}


	public Double chernoffBound( ChernoffStatistics statistics ){
		return Math.sqrt(Math.abs( 3 * statistics.getMean() / statistics.getNumberOfExamples() * Math.log( 2 / this.delta ) ) );
	}


	/**
	 * @see stream.learner.tree.SplitCriterion#requiresSplit(java.lang.Object)
	 */
	@Override
	public boolean requiresSplit(ChernoffStatistics statistics) {
		Double err = chernoffBound( statistics );
		return err > epsilon;
	}
}