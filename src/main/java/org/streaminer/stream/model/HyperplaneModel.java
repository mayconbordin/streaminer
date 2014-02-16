package org.streaminer.stream.model;

import org.streaminer.util.distance.Kernel;
import org.streaminer.stream.learner.LearnerUtils;
import org.streaminer.stream.data.Data;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Data model for separating Hyperplanes.
 * </p>
 *  
 * @author Helge Homburg, Christian Bockermann
 */
public class HyperplaneModel implements PredictionModel<Data, Double> {

	private static final long serialVersionUID = 6049635362534065136L;

	private Kernel kernel;

	Map<String,Double> weights = new LinkedHashMap<String,Double>();

	private double bias;

	public HyperplaneModel(int kernelType) {
		this.kernel = new Kernel(kernelType);
	}

	public void initModel( Map<String,Double> weights, Double bias) {
		this.weights = weights;
		this.bias = bias;
	}

	public Map<String,Double> getWeights() {
		return weights;
	}

	public double getBias() {
		return bias;
	}

	public void setWeights( Map<String,Double> weights) {
		this.weights = weights;
	}

	public void setBias(double bias) {
		this.bias = bias;
	}

	/**
	 * Calculates the distance for each example and delivers a label prediction.
	 */
	@Override
	public Double predict(Data item) {
		Map<String,Double> example = LearnerUtils.getNumericVector( item );

		double distance = 0.0;
		distance = kernel.getDistance( example, weights );
		distance += bias;        	
		if (distance < 0) {
			return 0.0d;
		} else {
			return 1.0d;
		}
	}   

	/**
	 * Delivers a String representation of the hyperplane model
	 */
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		output.append("Hyperplane:\n\n");
		output.append("bias: "+bias+"\n");
		for( String key : weights.keySet() )
			output.append( "weight["+ key +"]: " + weights.get( key ) + "\n");
		return output.toString();		
	}
}      