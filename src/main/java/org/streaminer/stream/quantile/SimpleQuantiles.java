package org.streaminer.stream.quantile;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * This implementation computes approximate phi-quantiles that differ at most epsilon*N ranks from the element
 * that is the real quantile. epsilon is known a priori and affects N, which is the minimum size of the set of
 * observations. To get a hunch how many examples are need I'm providing some examples:
 * <ul>
 * <li> epsilon = 0.25 - buffer size = 48</li>
 * <li> epsilon = 0.10 - buffer size = 100</li>
 * <li> epsilon = 0.05 - buffer size = 190</li>
 * <li> epsilon = 0.01 - buffer size = 910</li>
 * </ul>
 * <b>You can use this class to estimate quantiles, as long as the distribution of observations doesn't have a 
 * heavy tail.</b>
 * 
 * @author Markus Kokott
 *
 */
public class SimpleQuantiles implements IQuantiles<Double> {
    private boolean initialPhase;
    private Integer bufferSize;
    private CopyOnWriteArrayList<Double> buffer;
    
    /**
     * This value specifies the error bound.
     */
    private double epsilon;
    
    /**
     * Standard constructor...
     * 
     * @param epsilon This quantile estimator determines a phi-quantile that differs at most epsilon*N positions
     * from the real quantile 
     */
    public SimpleQuantiles (double epsilon) {
        if (epsilon <= 0 || epsilon >= 1) {
            throw new RuntimeException("An appropriate epsilon value must lay between 0 and 1.");
        }

        this.epsilon = epsilon;
        this.bufferSize = this.computeBufferSize();
        this.initialPhase = true;
        this.buffer = new CopyOnWriteArrayList<Double>();
    }
        
    @Override
    public void offer(Double value) {
        this.buffer.add(value);
		
        if (this.buffer.size() >= this.bufferSize) {
            this.initialPhase = false;
            this.buffer.remove(0);
        }
    }

    @Override
    public Double getQuantile(double q) throws QuantilesException {
        if (this.buffer.size() != this.bufferSize) {
            if (this.bufferSize == 0) {
                    return Double.NaN;
            }
            return this.buffer.get((int) Math.floor(q*this.buffer.size()));
        }
        Integer lowerBound = this.computeLowerBound(q);
        Integer upperBound = this.computeUpperBound(q);

        LinkedList<Double> copyOfBuffer = new LinkedList<Double>();
        Iterator<Double> tempBuffer = this.buffer.iterator();

        while (tempBuffer.hasNext()) {
            copyOfBuffer.add(tempBuffer.next());
        }

        Collections.sort(copyOfBuffer);
        return copyOfBuffer.get((int) Math.floor(lowerBound + upperBound) / 2);
    }
    
    /**
     * Before a quantile can be computed, the first buffer has to be filled. During that initial phase
     * every call of {@link #getQuantile(double)} will return {@link Double#NaN}. You can call this method 
     * to check whether the initial phase has ended or not.
     * 
     * @return <code>true</code> if no quantiles can be computed at the moment.
     */
    public boolean isInInitialPhase(){
        return this.initialPhase;
    }

    /**
     * The size of the buffer depends on the given epsilon. This method computes an appropriate size 
     * for the buffer.
     * 
     * @return an <code>int</code> value that serves as the maximum length for the buffer
     */
    private int computeBufferSize() {
        Double temp = Math.ceil(9/(epsilon*(1-epsilon)));
        return temp.intValue();
    }

    /**
     * Because the quantile lays in an interval there have to be bounds on the ranks. This method computes the lower one.
     * 
     * @param phi The specific quantile.
     * @return A lower bound for the rank of the wanted quantile.
     */
    private Integer computeLowerBound(double phi){
        double p = 1 - (1-this.epsilon) / 2;
        if (epsilon < 0.5){
            p = 1 - this.epsilon / 2;
        }
        double t = Math.sqrt(-2*Math.log(1-p));
        double gaussianPQuantile = t - (2.515517 + 0.802853*t + 0.010328*Math.pow(t, 2)) / (1 + 1.432788*t + 0.189269*Math.pow(t, 2) + 0.001308*Math.pow(t, 3));
        Double bound = 1d;
        bound = Math.ceil(this.bufferSize * phi - gaussianPQuantile * Math.sqrt(this.bufferSize * phi * (1-phi)));
        if (bound <= 0) {
            bound = 1d;
        }
        return bound.intValue();
    }

    /**
     * Because the quantile lays in an interval there have to be bounds on the rank. This method computes the upper one.
     * 
     * @param phi The specific quantile.
     * @return An upper bound for the rank of the wanted quantile.
     */
    private Integer computeUpperBound(double phi){
        double p = 1 - (1-this.epsilon) / 2;
        if (epsilon < 0.5) {
            p = 1 - this.epsilon / 2;
        }
        double t = Math.sqrt(-2*Math.log(1-p));
        double gaussianPQuantile = t - (2.515517 + 0.802853*t + 0.010328*Math.pow(t, 2)) / (1 + 1.432788*t + 0.189269*Math.pow(t, 2) + 0.001308*Math.pow(t, 3));
        Double bound = 1d;
        bound = Math.ceil(this.bufferSize * phi + gaussianPQuantile * Math.sqrt(this.bufferSize * phi * (1-phi)));
        if (bound >= this.bufferSize) {
            bound = 1d;
        }
        return bound.intValue();
    }
    
}
