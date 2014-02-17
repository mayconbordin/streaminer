package org.streaminer.stream.avg;

/**
 * Interface for algorithms that calculate the average over a data stream.
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public interface IAverage {
    /**
     * Add a new value to the data structure and updates the average.
     * @param value The value to be added
     */
    public void add(double value);
    
    /**
     * @return Get the current average
     */
    public double getAverage();
    
    /**
     * Reset the data structure.
     */
    public void clear();
}
