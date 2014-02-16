/**
 * 
 */
package org.streaminer.stream.classifier.tree;

import java.io.Serializable;

/**
 * <p>
 * This class implements a container for statistics that need to be maintained
 * for each node in a regression-tree.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 *
 */
public class RegressionTreeStatistics implements NodeInfo, ChernoffStatistics {

	/** The unique class ID */
	private static final long serialVersionUID = 8932713211467722273L;

	/* The split value of this node */
	Serializable value;
	
	/** number of elements with lower or equal feature value  */
	Double elements;

	/** sum of target value with lower or equal feature value  */
	Double ySum;

	/**  squared sum of target value with lower or equal feature value  */
	Double ySumSquared;

	
	public RegressionTreeStatistics(){
		elements = 1.0d;
		ySum = 0.0d;
		ySumSquared = 0.0d;
	}
	
	
	/**
	 * 
	 * @param val
	 * @param gamma
	 */
	public void update(String val ){
		throw new RuntimeException( "Nominal attributes are not supported!" );
	}


	/**
	 * updates statistics of this node
	 */
	public void update( Double value ){
		elements += 1.0d;
		ySum += value;
		ySumSquared +=  (value * value);
	}

	
	public Double getStandardDeviation(){
		double tmp = 1/elements * ( ySumSquared - 1/elements * Math.pow( ySum, 2 ) );
		if(tmp < 0)
			return 0.0d;
		
		return Math.sqrt(tmp);
	}


	/**
	 * @see stream.learner.tree.ChernoffStatistics#getEpsilon()
	 */
	@Override
	public Double getEpsilon() {
		return null;
	}


	/**
	 * @see stream.learner.tree.ChernoffStatistics#getMean()
	 */
	@Override
	public Double getMean() {
		return ySum / elements;
	}


	/**
	 * @see stream.learner.tree.ChernoffStatistics#getNumberOfExamples()
	 */
	@Override
	public Double getNumberOfExamples() {
		return elements;
	}
}