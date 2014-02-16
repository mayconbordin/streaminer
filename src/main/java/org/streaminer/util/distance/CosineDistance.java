package org.streaminer.util.distance;

import java.util.LinkedList;
import java.util.List;

public class CosineDistance {

	public static <T extends List<Double>> Double distanceBetween(T firstVector, T secondVector){
		
		if (firstVector.size() != secondVector.size()) {
			throw new RuntimeException("Vectors must have the same dimension");
		}
		
		Double numerator = 0d;
		Double dimSumFirst = 0d;
		Double dimSumSecond = 0d;
		for (int i = 0; i < firstVector.size(); i++){
			numerator += firstVector.get(i) * secondVector.get(i);
			dimSumFirst += Math.pow(firstVector.get(i), 2);
			dimSumSecond += Math.pow(secondVector.get(i), 2);
		}
		
		Double cosine = numerator / Math.sqrt(dimSumFirst + dimSumSecond);
		return (1 - cosine);
	}
	
	public static <T extends List<Double>> T getFarestVector(List<T> vectors, T comparator){
		
		Double distance = Double.MIN_VALUE;
		T farest = vectors.get(0);
		
		for (int i = 0; i < vectors.size(); i++){
			if (CosineDistance.distanceBetween(comparator, vectors.get(i)) > distance){
				farest = vectors.get(i);
				distance = CosineDistance.distanceBetween(comparator, vectors.get(i));
			}
		}
			
		return farest;
	}
	
	public static <T extends List<Double>> T getNearestVector(List<T> vectors, T comparator){
		Double distance = Double.MAX_VALUE;
		T nearest = vectors.get(0);
		
		for (int i = 0; i < vectors.size(); i++){
			if (CosineDistance.distanceBetween(comparator, vectors.get(i)) < distance){
				nearest = vectors.get(i);
				distance = CosineDistance.distanceBetween(comparator, vectors.get(i));
			}
		}
			
		return nearest;
	}
	
	public static <T extends List<Double>> List<T> getPairWithSmallestDistance(List<T> vectors) {
		
		if (vectors.size() < 2){
			throw new RuntimeException("List of vectors must at least contain two vectors");
		}
		
		LinkedList<T> bestPair = new LinkedList<T>();
		T comparator;
		double minimalDistance = Double.MAX_VALUE;
		
		for (int i = 0; i < vectors.size() - 1; i++){
			comparator = vectors.get(i);
			
			for (int j = i+1; j < vectors.size(); j++){
				
				if(CosineDistance.distanceBetween(comparator, vectors.get(j)) < minimalDistance){
					minimalDistance = CosineDistance.distanceBetween(comparator, vectors.get(j));
					
					bestPair = new LinkedList<T>();
					bestPair.add(comparator);
					bestPair.add(vectors.get(j));
				}
			}
		}
		
		return bestPair;
	}
	
	public static <T extends List<Double>> List<T> getPairWithFurthestDistance(List<T> vectors) {
		LinkedList<T> worstPair = new LinkedList<T>();
		T comparator;
		double maximumDistance = Double.MIN_VALUE;
		
		for (int i = 0; i < vectors.size() - 1; i++){
			comparator = vectors.get(i);
			
			for (int j = i+1; j < vectors.size(); j++){
				
				if (CosineDistance.distanceBetween(comparator, vectors.get(j)) > maximumDistance){
					maximumDistance = CosineDistance.distanceBetween(comparator, vectors.get(j));
					
					worstPair = new LinkedList<T>();
					worstPair.add(comparator);
					worstPair.add(vectors.get(j));
				}
			}
		}
		
		return worstPair;
	}
}
