package org.streaminer.stream.quantile.rss;

import java.io.Serializable;


/**
 * This class represents a dyadic tree of {@link Subset} , it also manages them.
 * 
 * @author Carsten Przyluczky
 *
 */
public class SubsetTree implements Serializable {
	private static final long serialVersionUID = 1L;

	private Subset levels[]; 
	
	float epsilon = 0.0f;
	float delta = 0.0f;	
	int maxValue = 0;
	int elementCount = 0;
	
	// ---------------------------------------------------------------------------------- constructor
	/**
	 * constructor
	 * 
	 * @param delta failure probability
	 * @param epsilon specify the accuracy
	 * @param maxValue determines the size of the universe
	 */
	public SubsetTree(float epsilon, float delta, int maxValue){
		this.delta = delta;
		this.epsilon = epsilon;
		this.maxValue = maxValue;
		
		createLevels();
	}
	
	 
	// ---------------------------------------------------------------------------------- main functions
	/**
	 * Calculates the needed level count and creates them.
	 */
	private void createLevels(){
		int logOfValue = (int) (Math.log10((double)maxValue) / Math.log10(2.0));		
		levels = new Subset[logOfValue + 2];
		
		for(int i = 0; i <= logOfValue + 1  ; i++){
			levels[i] = new Subset(i,maxValue);
		}
	}
	
	/**
	 * Adds specified value to all subsets
	 * 
	 * @param value the value of the new element
	 */
	public void addElement(double value){
		elementCount++;
		for(Subset level : levels){
			level.addElement(value);
		}	
	}
	
	 
	
	@Override
	public String toString() {
		String output = "";
		for(Subset level : levels){
			output  = output + level;
		}
		return output;
	}
		
	/**
	 * estimates an interval based on the rss technique
	 * 
	 * @param interval the interval that should be estimated
	 * @return the estimation for the interval
	 */
	public Double estimateIntervall(Interval interval) {		
		int intervalWidth = interval.getUpperBound() - interval.getLowerBound() + 1;
		
		for(Subset level : levels){
			if(intervalWidth == level.getIntercalWidth()){
				if(level.canHandleInterval(interval.getLowerBound(), interval.getUpperBound())){
						int valueCount = level.getValuesInSubsetCount(); 
						return 2.0 * (double)valueCount - (double)elementCount;
				}					
			}
		}	
		return (double)RSSQuantiles.CANT_ESTIMATE;
	}
	
}
