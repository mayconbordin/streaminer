package org.streaminer.stream.quantile.rss;

import org.streaminer.stream.quantile.ExactQuantiles;
import org.streaminer.stream.quantile.QuantilesException;
import java.io.Serializable;
import java.util.LinkedList;

/**
 * Buckets are the main ingredient for sliding windows. 
 * They hold a specified number of elements, and join them as one.  
 * 
 * @author Carsten Przyluczky
 */
public class Bucket implements Serializable {
	private static final long serialVersionUID = 1L;

	double epsilon = 0.0f;
	double delta = 0.0f;
	int elementCount = 0;
	int maxValue;
	int subsetCount = 0;
	int elementsInGroupCount = 0;
	int groupCount = 0;

	SubsetTree subsetTrees[];
	
	// ---------------------------------------------------------------------------------- constructor
	public Bucket(float epsilon, float delta, int maxValue){
		this.delta = delta;
		this.epsilon = epsilon;
		this.maxValue = maxValue;
		calculateSubsetCount();
		createNewSubsets();
	}
	
	// ---------------------------------------------------------------------------------- main functions	
	/**
	 * This method creates the needed count of {@link SubsetTree}'s 
	 */
	void createNewSubsets(){	
		subsetTrees = new SubsetTree[subsetCount];
		for(int i = 0; i < subsetCount;i++){
			subsetTrees[i] = new SubsetTree((float)epsilon,(float)delta, maxValue);			
		}
	}
	
	/**
	 * This method adds the item to all of its subsetTrees
	 * 
	 * @param item this item will be processed  i.e. added to the data structure 
	 */
	public void process(double item){
		elementCount++;
		for(SubsetTree subsetTree : subsetTrees){
			subsetTree.addElement(item);			 
		}
	}
			
	/**
	 * This method calculates the needed subset count, to satisfy the needs
	 * for the maximum.  
	 */
	void calculateSubsetCount(){
		double logU = log2(maxValue);
		double term1 = 3.0 * log2(logU / delta);
		double term2 = 8.0 * logU / (epsilon * epsilon);
		elementsInGroupCount = (int)term2 / 100;
		groupCount = (int)term1; 
		subsetCount = groupCount * elementsInGroupCount ;
	}
	
	/**
	 * This method receives a list of intervals, and estimates their element count 
	 * based on the rss technique.
	 * 
	 * @param intervals the list of intervals that should be estimated
	 * @return the final estimation for the bucket
	 */
	public Double estimateIntervals(LinkedList<Interval> intervals ) throws QuantilesException {			
		int subsetTreeIndex = 0;
		ExactQuantiles exactQuantiles = new ExactQuantiles();	
		
		// build the averages of all groups
		for(int g = 0; g < groupCount; g++){			
			double groupAverage = 0.0;
			for(int e = 0; e < elementsInGroupCount;e++){
				double groupEstimation = 0.0f;
				for(Interval interval : intervals){					
					double elementEstimation = subsetTrees[subsetTreeIndex].estimateIntervall(interval);
					
					if(elementEstimation != RSSQuantiles.CANT_ESTIMATE){
						groupEstimation += elementEstimation;						
					}
				}				
				groupAverage += groupEstimation;
				subsetTreeIndex++;
				if(subsetTreeIndex > subsetTrees.length){
					System.out.println(subsetTrees.length + " index = "+ subsetTreeIndex + " count " + subsetCount);
				}
			}
			groupAverage /= (double)elementsInGroupCount;
			exactQuantiles.offer(groupAverage);
		}
				
		// the median of all group results is the answer
		return exactQuantiles.getQuantile(0.5f);
	}
	
	/**
	 * returns true if the bucket exceeded its capacity 
	 * @return true if the bucket exceeded its capacity 
	 */
	public boolean IsFull(){
		return elementCount >= RSSQuantiles.ELEMENTS_PER_BUCKET;
	}
	
	// ---------------------------------------------------------------------------------- getter and setter
	public int getElementCount(){
		 return elementCount;
	}
	
	public static double log2(double number){
		return Math.log10(number)/Math.log10(2d);
	}
}
