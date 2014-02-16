package org.streaminer.stream.frequency;

import org.streaminer.stream.frequency.util.CountEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the "Sticky Sampling" algorithm as described in the paper
 * "Approximate Frequency Counts over Data Streams" written by Gurmeet Singh Manku
 * and Rajeev Motwani
 *
 * @author Benedikt Kulmann
 */
public class StickySampling<T> extends BaseFrequency<T> {
    private double support;
    private double error;
    
    /**
     * <p>Elements which are not yet existing in the data structure will be added to it
     * with probablity <code>1/r</code>.</p>
     *
     * <p><code>(samplingRate * t)</code> is the number of items
     * until the {@link #adaptNewSamplingRate()} method will be invoked since its last invocation.</p>
     */
    private long samplingRate;

    /**
     * <p>Calculated at object creation time<br />
     * have a look at the {@link #samplingRate} for further documentation</p>
     */
    private final double t;

    /**
     * <p>Counter for the current sampling interval so that it is possible to determine whether
     * the {@link #adaptNewSamplingRate()} method has to be invoked.</p>
     */
    private long windowCount;

    /**
     * <p>The length of the current "sampling window", determined with <code>(samplingRate * t)</code></p>
     */
    private long windowLength;
    
    /**
     * <p>The data structure which holds all counting information.</p>
     */
    private final Map<T, CountEntry<T>> dataStructure;

    /**
     * The total count of all counted elements
     * in the stream so far.
     */
    private long elementsCounted;
    
    /**
     * <p>Creates a new instance of StickySampling.</p>
     *
     * @param support The threshold whether an element is frequent or not. Has to be out of (0,1).
     * @param error An epsilon for the threshold. Has to be out of (0,1).
     * @param probabilityOfFailure Probability for an item to fail to fulfill the three quality characteristics of this algorithm. Has to be out of (0,1).
     */
    public StickySampling(double support, double error, double probabilityOfFailure) {
        super(support);
        
        if (support <= 0 || support >= 1) {
	    throw new IllegalArgumentException("Support has to be > 0 and < 1.");
	}
	if (error <= 0 || error >= 1) {
	    throw new IllegalArgumentException("Error has to be > 0 and < 1.");
	}
        if (probabilityOfFailure <= 0 || probabilityOfFailure >= 1) {
            throw new IllegalArgumentException("Probability of failure has to be > 0 and < 1.");
        }

        this.support = support;
        this.error = error;
        this.samplingRate = 1;
        this.t = (1 / error) * Math.log(1 / (support * probabilityOfFailure));
        this.windowCount = 0;
        this.windowLength = (long)(2 * t);//only on initialization. Later this is calculated by (samplingRate * t)
        
        this.elementsCounted = 0;
        this.dataStructure = new ConcurrentHashMap<T, CountEntry<T>>();
    }
    
    @Override
    public boolean add(T item, long incrementCount) {
        boolean newItem = true;
        
        if (containsItem(item)) {
            incrementCount(item, incrementCount);
            newItem = false;
        } else {
            if(sample()) {
                insertItem(item, incrementCount);
            }
        }
        windowCount++;

        if (changeOfSamplingRateNeeded()) {
            changeSamplingRate();
            adaptNewSamplingRate();
        }
        
        return newItem;
    }

    @Override
    public long estimateCount(T item) {
        if (dataStructure.containsKey(item)) {
            return dataStructure.get(item).frequency;
        } else {
            return 0L;
        }
    }
    
    public boolean contains(T item) {
        return dataStructure.containsKey(item);
    }

    @Override
    public long size() {
        return elementsCounted;
    }

    @Override
    public Set<T> keySet() {
        return dataStructure.keySet();
    }
    
    public List<CountEntry<T>> getFrequentItems(double minSupport) {
        List<CountEntry<T>> frequentItems = new ArrayList<CountEntry<T>>();

        for (CountEntry<T> entry : dataStructure.values()) {
            if (isFrequent(entry.frequency)) {
                frequentItems.add(entry);
            }
        }

        return frequentItems;
    }
    
    /**
     * <p>Returns whether the provided frequency is a frequent one in terms of sticky sampling.</p>
     *
     * @param frequency The frequency which shall be tested
     * @return True if the frequency would classify an item as frequent in terms of sticky sampling, false otherwise
     */
    public boolean isFrequent(long frequency) {
        return frequency >= (support - error) * elementsCounted;
    }
    
    /**
     * <p>Decision whether a new item should be put into the data structure.</p>
     *
     * @return Whether an item should be put into the data structure
     */
    private boolean sample() {
        return Math.random() <= 1 / (double)samplingRate;
    }

    /**
     * <p>If the end of the current "sampling window" is reached, reset the current sampling window counter
     * and increment the sampling rate.</p>
     *
     * @return Whether a change of the current sampling rate is needed
     */
    private boolean changeOfSamplingRateNeeded() {
        return windowCount == windowLength;
    }

    /**
     * <p>Makes changes to the sampling rate.</p>
     *
     * <p>Changes to the data structure will be performed by {@link #adaptNewSamplingRate()}</p>
     */
    private void changeSamplingRate() {
        windowCount = 0;
        samplingRate *= 2;
        windowLength = (long)(samplingRate * t);
    }

    /**
     * <p>Diminish counts and remove elements which reach a count of 0.</p>
     *
     * <p>This transforms the data structure such that it contains elements
     * which would also have been sampled with the new sampling rate, only.</p>
     *
     * <p>The modification of the sampling rate itself is performed by {@link #changeSamplingRate()}.</p>
     */
    private void adaptNewSamplingRate() {
        for (T item : dataStructure.keySet()) {
            while(tossCoin()) {
                decrementCount(item);
                if(frequencyIsZero(item)) {
                        removeItem(item);
                        break;
                }
            }
        }
    }

    /**
     * <p>50:50 random event</p>
     * 
     * @return Result of 50:50 random event
     */
    private boolean tossCoin() {
        return Math.random() < 0.5;
    }
    
    /**
     * <p>Removes the {@link CountEntry} associated with the provided item from the internal
     * data structure.</p>
     *
     * @param itemToRemove The item whose {@link CountEntry} shall be removed
     */
    private void removeItem(T itemToRemove) {
        dataStructure.remove(itemToRemove);
    }

    /**
     * <p>Returns whether the internal data structure contains a counter for the provided item.</p>
     *
     * @param item The item in question
     * @return True if the internal data structure contains a counter for the provided item, false otherwise.
     */
    private boolean containsItem(T item) {
        return dataStructure.containsKey(item);
    }

    /**
     * <p>Increment the count frequency of the provided item by 1.</p>
     *
     * @param item The item whose frequency shall be incremented by 1.
     */
    private void incrementCount(T item, long incrementCount) {
        dataStructure.get(item).frequency += incrementCount;
        elementsCounted++;
    }

    /**
     * <p>Decrements the count frequency of the provided item by 1. Used within the
     * {@link StickySampling#adaptNewSamplingRate()} method of the algorithm.</p>
     *
     * @param item The item whose count frequency shall be decremented by 1.
     */
    private void decrementCount(T item) {
        dataStructure.get(item).frequency--;
    }

    /**
     * <p>Returns whether the count frequency of the provided item corresponds to 0
     * (i.e. frequency == 0 or item doesn't exist within the internal data structure).</p>
     *
     * @param item The item in question.
     * @return true if the count frequency of the provided item corresponds to 0
     */
    private boolean frequencyIsZero(T item) {
        return !dataStructure.containsKey(item) || dataStructure.get(item).frequency == 0;
    }

    /**
     * Inserts the provided item into the internal data structure with an initial count of 1.
     *
     * @param item The item which shall be inserted into the internal data structure
     */
    private void insertItem(T item, long incrementCount) {
        dataStructure.put(item, new CountEntry<T>(item, incrementCount));
        elementsCounted++;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("StickySamplingModel[");
        for (T key : dataStructure.keySet()) {
            sb.append(dataStructure.get(key)).append(";");
        }
        sb.append("]");
        return sb.toString();
    }
}