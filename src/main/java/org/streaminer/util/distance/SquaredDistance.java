package org.streaminer.util.distance;

import java.util.LinkedList;
import java.util.List;

/**
 * This class gives some static methods concerning squared distance of vectors. A vector has to be a
 * {@link List} of {@link Double}. Moreover vectors must have the same dimension to be compared so a
 * {@link RuntimeException} will be thrown if dimensions of vectors don't fit.
 * 
 * @author Markus Kokott
 *
 */
public class SquaredDistance{

	/**
	 * The squared distance between two vectors can be computed using this method.
	 * @param <T> extends {@link List} of {@link Double}
	 * @param firstVector - {@link List} of {@link Double} representing a vector
	 * @param secondVector - {@link List} of {@link Double} representing a vector
	 * @return distance of two vectors represented by a {@link Double} value
	 * @throws <code>RuntimeException</code> if dimension of <b>firstVector</b> and <b>secondVector</b> are
	 * different 
	 */
	public static <T extends List<Double>> Double distanceBetween(T firstVector, T secondVector) {
		
		if (firstVector.size() != secondVector.size()) {
			throw new RuntimeException("Vectors must have the same dimension");
		}
		double distance = 0d;
		
		for (int i = 0; i < firstVector.size(); i++){
			distance += Math.pow(firstVector.get(i) - secondVector.get(i), 2); 
		}
		return Math.sqrt(distance);
	}

	/**
	 * Given a comparator and a list of vectors this method determines which vector has the greatest
	 * distance to the comparator.
	 * @param <T> extends {@link List} of {@link Double}
	 * @param vectors - a {@link List} containing {@link List}s of {@link Double} each representing a vector
	 * @param comparator - {@link List} of {@link Double} representing a vector
	 * @return {@link List} of {@link Double} which represents the vector with furthest distance to
	 * <code>comparator</code>
	 * @throws <code>RuntimeException</code> if dimension of <b>firstVector</b> and <b>secondVector</b> are
	 * different
	 */
	public static <T extends List<Double>> T getFarestVector(List<T> vectors, T comparator) {
		double maximalDistance = Double.MIN_VALUE;
		int index = 0;
		
		for (int i = 0; i < vectors.size(); i++){
			
			if (SquaredDistance.distanceBetween(vectors.get(i), comparator) > maximalDistance){
				maximalDistance = SquaredDistance.distanceBetween(vectors.get(i), comparator);
				index = i;
			}
		}
		return vectors.get(index);
	}

	/**
	 * Given a comparator and a list of vectors this method determines which vector is closest to
	 * the comparator.
	 * @param <T> extends {@link List} of {@link Double}
	 * @param vectors - a {@link List} containing {@link List}s of {@link Double} each representing a vector
	 * @param comparator - {@link List} of {@link Double} representing a vector
	 * @return {@link List} of {@link Double} which represents the vector with smallest distance to
	 * <code>comparator</code>
	 * @throws <code>RuntimeException</code> if dimension of <b>firstVector</b> and <b>secondVector</b> are
	 * different
	 */
	public static <T extends List<Double>> T getNearestVector(List<T> vectors, T comparator) {
		double minimalDistance = Double.MAX_VALUE;
		int index = 0;
		
		for (int i = 0; i < vectors.size(); i++){
			
			if (SquaredDistance.distanceBetween(vectors.get(i), comparator) < minimalDistance){
				minimalDistance = SquaredDistance.distanceBetween(vectors.get(i), comparator);
				index = i;
			}
		}
		return vectors.get(index);
	}

	/**
	 * This method returns the pair out of a list of vectors that have the closest distance.
	 * @param <T> extends {@link List} of {@link Double}
	 * @param vectors - a {@link List} containing {@link List}s of {@link Double} each representing a vector
	 * @return {@link List} containing two {@link List} of {@link Double} representing the two closest
	 * vectors
	 * @throws <code>RuntimeException</code> if dimension of <b>firstVector</b> and <b>secondVector</b> are
	 * different or list of vectors contains less than two elements
	 */
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
				
				if(SquaredDistance.distanceBetween(comparator, vectors.get(j)) < minimalDistance){
					minimalDistance = SquaredDistance.distanceBetween(comparator, vectors.get(j));
					
					bestPair = new LinkedList<T>();
					bestPair.add(comparator);
					bestPair.add(vectors.get(j));
				}
			}
		}
		
		return bestPair;
	}

	/**
	 * This method returns the pair out of a list of vectors that have the furthest distance.
	 * @param <T> extends {@link List} of {@link Double}
	 * @param vectors - a {@link List} containing {@link List}s of {@link Double} each representing a vector
	 * @return {@link List} containing two {@link List} of {@link Double} representing the two vectors
	 * with greatest distance.
	 * @throws <code>RuntimeException</code> if dimension of <b>firstVector</b> and <b>secondVector</b> are
	 * different or list of vectors contains less than two elements
	 */
	public static <T extends List<Double>> List<T> getPairWithFurthestDistance(List<T> vectors) {
		LinkedList<T> worstPair = new LinkedList<T>();
		T comparator;
		double maximumDistance = Double.MIN_VALUE;
		
		for (int i = 0; i < vectors.size() - 1; i++){
			comparator = vectors.get(i);
			
			for (int j = i+1; j < vectors.size(); j++){
				
				if (SquaredDistance.distanceBetween(comparator, vectors.get(j)) > maximumDistance){
					maximumDistance = SquaredDistance.distanceBetween(comparator, vectors.get(j));
					
					worstPair = new LinkedList<T>();
					worstPair.add(comparator);
					worstPair.add(vectors.get(j));
				}
			}
		}
		
		return worstPair;
	}
}
