
package org.streaminer.stream.change;

/**
 * Non-parametric CUSUM change detection algorithm.
 * Source code: https://github.com/blockmon/blockmon
 * 
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class CusumNP extends AbstractCusum {
    private int meanWindow;
    private double offset;
    private double[] lastValues;
    private int lastValueIndex = 0;
    private int lastValuesCount = 0;

    /**
     * Constructor
     *
     * @param threshold The minimum sum to throw an alarm
     * @param meanWindow Number of last values with which the mean should be evaluated
     * @param offset Value to add to the difference in the score computation
     */
    public CusumNP(double threshold, int meanWindow, double offset) {
        super(threshold);
        this.meanWindow = meanWindow;
        this.offset = offset;
        
        lastValues = new double[meanWindow];
    }
    
    
    
    @Override
    protected double computeScore(double value) {
        // compute the mean
        double mean;
        if (lastValuesCount > 0) {
            mean = 0;
            for (int i=0; i<lastValuesCount; i++)
                mean += lastValues[i];
            mean /= (double)lastValuesCount;
        } else {
            mean = value;
        }
        
        // compute the score
        double score = value - mean - offset;
        
        // Add the current value to compute the next mean
        lastValues[lastValueIndex] = value;
        lastValueIndex = (lastValueIndex + 1) % meanWindow;
        if (lastValuesCount < meanWindow)
            lastValuesCount++;
        
        return score;
    }

    @Override
    protected void resetScore() {
        lastValueIndex  = 0;
        lastValuesCount = 0;
    }
    
}
