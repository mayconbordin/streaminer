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
public class RTreeStatistics implements NodeInfo {

	/** The unique class ID */
	private static final long serialVersionUID = 8932713211467722273L;

	/* The split value of this node */
	Serializable value;
	
	/** number of elements with lower or equal feature value  */
	Double leqElements;

	/** sum of target value with lower or equal feature value  */
	Double leqDeltaSum;

	/**  squared sum of target value with lower or equal feature value  */
	Double leqSquaredDeltaSum;

	/** number of elements with greater feature value  */
	Integer greaterElements;

	/** sum of target value with greater feature value */
	Double greaterDeltaSum;

	/** squared sum of target value with greater feature value */
	Double greaterSquaredDeltaSum;

	/** value of standard deviation reduction  */
	Double sdr;

	
	public RTreeStatistics( Double gamma ){
		leqElements = 1.0d;
		leqDeltaSum = gamma;
		leqSquaredDeltaSum = Math.pow(gamma, 2);
		greaterElements = 0;
		greaterDeltaSum = 0.0d;
		greaterSquaredDeltaSum = 0.0d;
		sdr = -1.0d;
	}
	
	
	/**
	 * @return the value
	 */
	public Serializable getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Serializable value) {
		this.value = value;
	}
	
	
	
	
	
	
	/**
	 * 
	 * @param val
	 * @param gamma
	 */
	public void updateNominal(Serializable val, double gamma){
		if(val.equals(value)){
			updateLeqStatistics(gamma);
		} else {
			updateGreaterStatistics(gamma);
		}
	}


	/**
	 * updates statistics of this node
	 */
	void update( Double value, double gamma ){
		if(value.compareTo((Double)this.value) <= 0) {
			updateLeqStatistics(gamma);
		} else {
			updateGreaterStatistics(gamma);
		}
	}

	/**
	 * updates statistics. used if value of actual example is greater then the
	 * value of this node or not equal in case of a nominal feature
	 * @param gamma value of target feature
	 */
	private void updateGreaterStatistics(double gamma){
		greaterElements++;
		greaterDeltaSum += gamma;
		greaterSquaredDeltaSum += Math.pow(gamma, 2);
	}

	/**
	 * updates statistics. used if value of actual example is lower or equal then the
	 * value of this node
	 * 
	 * @param gamma value of target feature
	 */
	private void updateLeqStatistics(double gamma){
		leqElements++;
		leqDeltaSum += gamma;
		leqSquaredDeltaSum += Math.pow(gamma, 2);
	}
	
	
	public Double getStandardDeviation(){
		Double n = this.leqElements.doubleValue() + this.greaterElements;
		
		double tmp = 1/n *(leqSquaredDeltaSum - 1/n * Math.pow(leqDeltaSum, 2));
		if(tmp < 0){
			return 0.0;
		}
		return Math.sqrt(tmp);
	}
}