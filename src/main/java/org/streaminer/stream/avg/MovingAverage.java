package org.streaminer.stream.avg;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Simple Moving Average
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class MovingAverage implements IAverage {
    private final Queue<Double> window = new LinkedList<Double>();
    private final int period;
    private double sum;
 
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