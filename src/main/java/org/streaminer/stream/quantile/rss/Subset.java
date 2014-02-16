package org.streaminer.stream.quantile.rss;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Random;

/**
 * This class is part of the rss algorithm. It covers a data-structure that represents a
 * set of intervals of a specified dyadic width and covers a range up to a choosable maximum. 
 * 
 * @author Carsten Przyluczky
 *
 */
public class Subset implements Serializable {
	private static final long serialVersionUID = 1L;

	private int valuesInSubsetCount = 0; 
	private int level = 0;
	int intervallWidth = 0;
	int intervalCount = 0;
	int maxValue = 0;
	BitSet intervallBitVector;
	
	// ---------------------------------------------------------------------------------- constructor
	/**
	 * The constructor calculates needed interval count, and selects intervals randomly
	 * 
	 * @param level the dyadic level the subset shall handle
	 * @param maxValue the highest value the subset must represent
	 */
	public Subset(int level, int maxValue){			
		intervallBitVector = new BitSet();					
		this.level = level;		
		intervallWidth = (int)Math.pow(2.0d, (double)level);
		this.maxValue = maxValue;
		int logOfValue = (int) (Math.log10((double)maxValue) / Math.log10(2.0)) + 1;
		while(!selectSubsets((int)Math.pow(2, (double)logOfValue))){
			// just try again
		}	
	}
	
	 
	// ---------------------------------------------------------------------------------- main functions
	/**
	 * this method randomly selects intervals to compose the subset
	 * it permits empty subsets
	 * 
	 * @param newMaxValue
	 * @return true if a subset has been selected, false otherwise
	 */
	boolean selectSubsets(int newMaxValue){
		int lowerBound = 0; 
		//int upperBound = Math.max(intervallWidth,1);  // currently not used
		Random random = new Random();
		boolean bIntervalselected = false;
		intervalCount = (int)((double)(newMaxValue - lowerBound) / (double)intervallWidth); 		

		for(int i = 0; i < intervalCount; i++){
			double event = random.nextDouble();
			if(event >= 0.5){
				intervallBitVector.set(i);
				bIntervalselected = true;
			}
 			lowerBound += intervallWidth;
			//upperBound += intervallWidth;    // currently not used				
		}
		return bIntervalselected;
	}
	
	/**
	 * this method adds a value to the subset
	 * 
	 * @param value the value of the new element
	 */
	public void addElement(double value){
		int lowerBound = 0; 
		int upperBound = Math.max(intervallWidth,1);
		 
		for(int i = 0; i < intervalCount; i++){
			if(intervallBitVector.get(i)){
				if( value >= (double)lowerBound && value <= (double)(upperBound - 1)){
					this.valuesInSubsetCount++;
					//System.out.println("value " + value + " fits into "+interval );
					return;
				}
			}
			lowerBound += intervallWidth;
			upperBound += intervallWidth;				
		}
	}		 

	/**
	 * A better toString, to gain more information
	 */
	@Override
	public String toString() {
		String output = "Subset level = " + level + " valuesInSubsetCount = " + valuesInSubsetCount   + " intervall width " + intervallWidth + "\n intervalls:\n " ;
		int count = 0;
		int lowerBound = 0; 
		int upperBound = Math.max(intervallWidth,1);
		 
		for(int i = 0; i < intervalCount; i++){
			if(intervallBitVector.get(i)){
				output = output  + lowerBound + " - " + (upperBound - 1) + "\n" ;
				count++;
			}
			lowerBound += intervallWidth;
			upperBound += intervallWidth;				
		}
		return output + " count = " + count;
	}
	

	/**
	 * Checks if the specified interval is selected in our subset
	 * 
	 * @param lowerBound the lower bound of the interval the needs to be checked
	 * @param upperBound the upper bound of the interval the needs to be checked
	 */	
	public boolean canHandleInterval(int lowerBound, int upperBound){
		int lower = 0; 
		int upper = Math.max(intervallWidth,1);
		
		for(int i = 0; i < intervalCount; i++){
			if(intervallBitVector.get(i)){
				if( lower == lowerBound && (upper - 1) == upperBound){
					return true;
				}
			}
			lower += intervallWidth;
			upper += intervallWidth;				
		}
		return false;
	}
	

	// ---------------------------------------------------------------------------------- get/set
	 
	public int getValuesInSubsetCount() {
		return valuesInSubsetCount;
	}
	
	
	public int getIntercalWidth(){
		return this.intervallWidth;
	}
}
