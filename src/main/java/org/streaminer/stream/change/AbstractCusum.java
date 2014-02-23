package org.streaminer.stream.change;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public abstract class AbstractCusum {
    protected double threshold;
    protected double currentSum = 0;

    /**
     * Constructor
     *
     * @param threshold The minimum change sum to throw an alarm
     */
    public AbstractCusum(double threshold) {
        this.threshold = threshold;
    }
    
    /**
     * Check if the value raises an alarm.
     * Depend on previous values since last reset.
     *
     * @param value The value you want to watch
     * @return if an alarm just got raised
     */
    public boolean check(double value) {
        double score = computeScore(value);
        if (currentSum + score > 0)
            currentSum += score;
        else
            currentSum = 0;
        return currentSum >= threshold;
    }
    
    /**
     * Reset the change detection algorithm.
     */
    public void reset() {
        currentSum = 0;
        resetScore();
    }
    
    /**
     * Compute the score of this value.
     * May depend on previous values since last reset.
     *
     * @param value the value for which the score should be computed
     * @return the score
     */
    protected abstract double computeScore(double value);
    
    /**
     * Reset the score computation algorithm.
     */
    protected abstract void resetScore();
}
