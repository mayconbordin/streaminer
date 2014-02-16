package org.streaminer.util.distance;

import java.util.List;

public class Radius {
	
	public static <T extends List<Double>> Double calculateRadius(int n, T vector){
		double vector_mean = 0;
		for (int i = 0; i < vector.size(); i++) {
			vector_mean += vector.get(i);
			}
		vector_mean = vector_mean/vector.size();
		
		double radius = 0;
		for (int i = 0; i < vector.size(); i++) {
			radius += Math.sqrt(Math.pow(vector.get(i)-n*vector_mean, 2)/n);
		}		
		return radius/n;		
	}
}
