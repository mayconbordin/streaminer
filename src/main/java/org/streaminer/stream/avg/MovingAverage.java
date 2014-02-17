package org.streaminer.stream.avg;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A simple moving average is a method for computing an average of a stream of 
 * numbers by only averaging the last P numbers from the stream, where P is 
 * known as the period.
 * 
 * Source code: <http://rosettacode.org/wiki/Averages/Simple_moving_average>.
 */
public class MovingAverage implements IAverage {
    private final Queue<Double> window = new LinkedList<Double>();
    private final int period;
    private double sum;
 
    /**
     * @param period A value greater than zero that determines the number of elements
     *               that will be used to calculate the average
     * @throws RuntimeException If the period is less than or equal to zero
     */
    public MovingAverage(int period) {
        if (period <= 0)
            throw new RuntimeException("Period must be a positive integer");
        this.period = period;
    }
 
    
    public void add(double num) {
        sum += num;
        window.add(num);
        if (window.size() > period) {
            sum -= window.remove();
        }
    }
 
    public double getAverage() {
        if (window.isEmpty()) return 0; // technically the average is undefined
        return sum / window.size();
    }

    public void clear() {
        window.clear();
        sum = 0;
    }
 
    
}