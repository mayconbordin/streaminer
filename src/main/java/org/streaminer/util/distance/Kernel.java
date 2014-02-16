package org.streaminer.util.distance;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Provides methods for calculating distances between two examples.
 * 
 * @author Helge Homburg
 */
public class Kernel implements Serializable{

	private static final long serialVersionUID = 1791524268878957934L;

	/** Use inner product as distance measure*/
	public final static int INNER_PRODUCT = 1;
	
	private int kernelType;
	
	public Kernel(int kernelType) {
		this.kernelType = kernelType;
	}
	
	public Double getDistance(double[] a, double[] b) {
		if (a.length != b.length) {
			return null;
		} else {
			return calculateDistance(a, b);
		}
	}
	
	private Double calculateDistance(double[] a, double[] b) {
		
		Double result = null;
		
		switch (kernelType) {
		
			case INNER_PRODUCT: {
				
				double innerProduct = 0.0;
				for (int i = 0; i < a.length; i++) {
					innerProduct += a[i] * b[i];
				}
				result = new Double(innerProduct);
			}
		}
		
		return result;
	}
	
	public Double getDistance( Map<String,Double> a, Map<String,Double> b ){
		
		Set<String> keys = a.keySet();
		if( a.size() > b.size() )
			keys = b.keySet();
		
		Double sum = 0.0d;
		for( String key : keys ){
			
			Double a_i = a.get( key );
			Double b_i = b.get( key );
		
			if( a_i == null )
				a_i = 0.0d;
			
			if( b_i == null )
				b_i = 0.0d;
			
			sum += (a_i * b_i);
		}
		
		return sum;
	}
}