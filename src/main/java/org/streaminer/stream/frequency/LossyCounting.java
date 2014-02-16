package org.streaminer.stream.frequency;

import org.streaminer.stream.frequency.util.CountEntry;
import org.streaminer.stream.frequency.util.CountEntryWithMaxError;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Implementation of the Lossy Counting algorithm described in the paper
 * "Approximate Frequency Counts over Data Streams" written by 'Rajeev Motwani' and
 * 'Gurmeet Singh Manku'.
 * </p>
 * 
 * @author Marcin Skirzynski (main work), Benedikt Kulmann (modifications)
 * @param <T>
 */
public class LossyCounting<T> extends BaseFrequency<T> {
    /**
     * The window size which will be set at
     * the beginning and will never change
     */
    private int windowSize;

    /**
     * The number of the current window
     * beginning with 0
     */
    private long currentWindow;

    /**
     * The maximum error set be the user at the
     * beginning.
     */
    private double error;
    
    /**
     * The data structures which holds all
     * counting information.
     */
    private Map<T, CountEntryWithMaxError<T>> dataStructure;

    /**
     * The total count of all counted elements
     * in the stream so far.
     */
    private long elementsCounted;
        
    /**
     * <p>
     * Constructs an instance of the LossyCounting algorithm
     * with the specified maximum error bound, which can not
     * be changed.
     * </p>
     *
     * @param maxError the maximum error bound
     */
    public LossyCounting(double maxError) {
        if (maxError < 0 || maxError > 1) {
            throw new IllegalArgumentException("Maximal error needs to be a double between 0 and 1");
        }

        this.windowSize = (int) Math.ceil(1 / maxError);
        this.currentWindow = 1;
        this.elementsCounted = 0;
        this.error = maxError;
        this.dataStructure = new ConcurrentHashMap<T, CountEntryWithMaxError<T>>();

        updateCurrentWindow();
    }

    @Override
    public boolean add(T item, long incrementCount) {
        boolean newItem = true;
        
        if (containsItem(item)) {
            incrementCount(item, incrementCount);
            newItem = false;
        } else {
            insertItem(item, incrementCount, currentWindow - 1);
        }

        updateCurrentWindow();
        
        if (elementsCounted % windowSize == 0) {
            compress();
        }
        
        return newItem;
    }

    /**
     * <p>
     * Returns the estimated frequency of the given element.
     * </p>
     *
     * <p>
     * The LossyCounting algorithm compresses the internal data structure which means
     * that an element will be deleted if it doesn't emerge frequently enough.
     * That means that even when the element appeared in the stream
     * the estimated frequency can be 0.
     * </p>
     *
     * @param item the item for which the estimated frequency will be returned
     * @return the estimated frequency of the given item
     */
    @Override
    public long estimateCount(T item) {
        if (dataStructure.containsKey(item)) {
            return dataStructure.get(item).frequency;
        }
        return 0L;
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
        List<CountEntry<T>> result = new ArrayList<CountEntry<T>>();
        for (T element : dataStructure.keySet()) {
            CountEntry<T> entry = dataStructure.get(element);
            if (entry.frequency >= (minSupport - error) * elementsCounted) {
                result.add(entry);
            }
        }
        return result;
    }
    
    /**
     * <p>
     * Compresses the data structure. Will be called automatically
     * by the count method, when a new window is reached.
     * </p>
     */
    private void compress() {
        Collection<T> markedToRemove = new ArrayList<T>();
        for (T element : dataStructure.keySet()) {
            CountEntryWithMaxError<T> entry = dataStructure.get(element);
            if (entry.frequency + entry.maxError < currentWindow) {
                markedToRemove.add(element);
            }
        }
        for (T element : markedToRemove) {
            dataStructure.remove(element);
        }
    }

    /**
     * <p>
     * Updates the current window
     * </p>
     */
    private void updateCurrentWindow() {
        this.currentWindow = (int) Math.ceil(elementsCounted / (double) windowSize);
    }
    
    private boolean containsItem(T item) {
        return dataStructure.containsKey(item);
    }

    private void incrementCount(T item, long incrementCount) {
        dataStructure.get(item).frequency += incrementCount;
        elementsCounted++;
    }

    private void insertItem(T item, long initialFrequency, long maxError) {
	dataStructure.put(item, new CountEntryWithMaxError<T>(item, initialFrequency, maxError));
        elementsCounted++;
    }
}
