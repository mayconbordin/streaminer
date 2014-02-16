package org.streaminer.stream.quantile.rss;

/**
 * A helper class to handle intervals nicely
 * 
 * @author Carsten Przyluczky
 *
 */
public class Interval {
	private int lowerBound;
	private int upperBound;
	
	// ---------------------------------------------------------------------------------- constructor
	public Interval(int lowerBound, int upperBound){
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	// ---------------------------------------------------------------------------------- main functions
	/**
	 * This method checks if the element falls in between the ranges of the interval
	 * 
	 *  @param element the element the needs to be checked
	 */
	public boolean contains(double element){
		return element >= (double)lowerBound && element <= (double)upperBound;
	}
	
	/**
	 * Checks if the interval equals another one
	 * 
	 * @param interval the potentially equal interval 
	 * @return true if the intervals are equal, false otherwise
	 */
	public boolean equals(Interval interval){
		return this.getLowerBound() == interval.getLowerBound() && this.getUpperBound() == interval.getUpperBound();
	}

	/**
	 * Checks if the interval equals the interval created by the handed over bounds
	 * 
	 * @param lowerBound the lower bound
	 * @param upperBound the upper bound
	 * @return true if the intervals are equal, false otherwise
	 */
	public boolean equals(int lowerBound, int upperBound){
		return this.getLowerBound() == lowerBound && this.getUpperBound() == upperBound;
	}

	
	@Override
	public String toString() {
		return "Interval [lowerBound=" + lowerBound + ", upperBound="
				+ upperBound + "]";
	}
	
	// ---------------------------------------------------------------------------------- get/set
	public int getUpperBound() {
		return upperBound;
	}
	
	public int getLowerBound() {
		return lowerBound;
	}

	
}
