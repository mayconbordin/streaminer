package org.streaminer.stream.quantile.rss;

import org.streaminer.stream.quantile.IQuantiles;
import org.streaminer.stream.quantile.QuantilesException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A stream version of the rss technique presented by Anna Gilbert,
 * Yannis Kotidis, S. Muthukrishanan, and Matrin Strauss in the paper
 * "How to summarize the Universe".
 * 
 * @author Carsten Przyluczky
 *
 */
public class RSSQuantiles implements IQuantiles<Double>, Serializable {
    private static final long serialVersionUID = -7491178942147615981L;
	
    public final static int CANT_ESTIMATE = -1;
    public static int ELEMENTS_PER_BUCKET = 200;
    private static int MAX_BUCKET_COUNT = 5;
    private int maxValue; // represents |U| 

    private List<Bucket> buckets;
    private Bucket newestBucket = null;
    private float epsilon;
    private float delta;
    
    /**
     * The constructor invokes all data-structure creation 
     * 
     * @param epsilon precision
     * @param delta error-probability
     * @param maxValue the maximum value the will be handled by this algorithm (|U|)
     */
    public RSSQuantiles(float epsilon, float delta, int maxValue) {
        this.epsilon = epsilon;
        this.delta = delta;
        this.maxValue = maxValue;

        buckets = new CopyOnWriteArrayList<Bucket>();
        addNewBucket();
    }
        
    @Override
    public void offer(Double value) {
        newestBucket.process(Math.ceil(value));
        if(newestBucket.IsFull()){			
            addNewBucket();
        }		
        deleteExcessiveBuckets();
    }

    @Override
    public Double getQuantile(double q) throws QuantilesException {
        int overallBucketCount = overallBucketCount();
        int wantedRank = (int)((float)overallBucketCount * (float)q - (float)overallBucketCount * epsilon);

        // this loop creates dyadic intervals 0..i and lets all buckets evaluate them.
        // then it tests if we have reatched the wanted rank with our sum.
        for (int i = 0; i < maxValue;i++) {
            LinkedList<Interval> intervals = collectNeededIntervalls(i);			
            double intervalSum = 0;
            for (Bucket bucket : new LinkedList<Bucket>(buckets)) {
                intervalSum += Math.abs(bucket.estimateIntervals(intervals));
            }
            if (intervalSum > wantedRank) {
                return (double)i;
            }
        }

        return (double)CANT_ESTIMATE;
    }
    
    /**
     * create a list of dyadic intervals that will, added together, describe the rank
     * 
     * @param rank
     */
    private LinkedList<Interval> collectNeededIntervalls(int rank) {
        LinkedList<Interval> intervals = new LinkedList<Interval>();
        int log2 = 0;
        int chunk = 0;
        int lowerBound = 0;
        int upperBound = 0;

        if (rank == 0) {
            intervals.add(new Interval(0, 0));		
        } else {
            rank++; // we need to count the 0 extra

            while (rank > 0) {
                log2 = (int) (Math.log10((double)rank) / Math.log10(2.0));
                chunk = (int)Math.pow(2.0d, (double)log2);
                upperBound = lowerBound + chunk - 1;
                intervals.add(new Interval(lowerBound, upperBound));
                lowerBound = upperBound + 1;
                rank -= chunk;
            }
        }
        return intervals;
    }

    /**
     * returns the sum of all {@link Bucket}  estimations
     * 
     * @return the sum of all {@link Bucket}  estimations
     */
    private int overallBucketCount(){
        int count = 0;
        for (Bucket bucket : buckets){
            count += bucket.getElementCount();
        }
        return count;
    }
    
    /**
     * create a new {@link Bucket} 
     */
    private void addNewBucket(){
        Bucket newBucket = new Bucket(epsilon, delta, maxValue); 
        buckets.add(newBucket);	
        newestBucket = newBucket;
    }

    /**
     * delete oldest {@link Bucket}  while we have too many of them.
     */
    private void deleteExcessiveBuckets(){
        while (buckets.size() > MAX_BUCKET_COUNT) {			
            buckets.remove(0);
        }
    }

    public void setElementsPerBucket(int newCount) {
        ELEMENTS_PER_BUCKET = newCount;
    }

    public int getElementsPerBucket() {
        return ELEMENTS_PER_BUCKET;
    }

    public void setMaxBucketCount(int newCount) {
        MAX_BUCKET_COUNT = newCount;
        deleteExcessiveBuckets();
    }

    public int getMaxBucketCount() {
        return MAX_BUCKET_COUNT;
    }
    
}
