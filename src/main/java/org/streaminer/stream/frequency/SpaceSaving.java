package org.streaminer.stream.frequency;

import org.streaminer.stream.frequency.util.CountEntryWithMaxError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.streaminer.stream.frequency.util.CountEntry;

/**
 * Implementation of the Space-Saving algorithm described in the paper
 * "Efficient Computation of Frequent and Top-k Elements in Data Streams" 
 * written by 'Ahmed Metwally', 'Divyakant Agrawal' and 'Amr El Abbadi'
 * 
 * @author Lukas Kalabis
 * @param <T> The type of object that will be stored
 */
public class SpaceSaving<T> extends BaseFrequency<T> {
    private double support = 0.1d;
    private double error   = 0.1d;
    private int    counter = 1000;
    
    /**
     * The data structure which holds all counting information.
     */
    private final List<Bucket<List<T>>> dataStructure;
        
    /**
     * The total count of all counted elements in the stream so far.
     */
    protected long elementsCounted;

    private boolean guaranteed = true;
    
    /**
     * Creates a new instance of SpaceSaving
     * @param counters number of available counters
     * @param support min-support for the frequency
     * @param maxError maximum error
     */
    public SpaceSaving(int counters, double support, double maxError) {
        super(support);
        
        if (support <= 0 || support >= 1) {
    	    throw new IllegalArgumentException("Support has to be > 0 and < 1.");
    	}
        
    	if (counters <= 0 ) {
            throw new IllegalArgumentException("Counters has to be > 0");
    	}
        
    	this.counter = counters;
    	this.support = support;
    	this.error   = maxError;
        
        elementsCounted = 0L;
        dataStructure = new LinkedList<Bucket<List<T>>>();
        
        for (int i = 0; i < this.counter; i++) {
            dataStructure.add(new Bucket<List<T>>(new LinkedList<T>(), 0, 0));
    	}
    }

    @Override
    public boolean add(T item, long incrementCount) {
        if (containsItem(item)) {
            incrementCount(item, incrementCount);
            return false;
        } else {
            // Replace the item with the lowest count with the new one.  
            insertItem(item, incrementCount);
            return true;
        }
    }

    @Override
    public long estimateCount(T item) {
        if (dataStructure.contains(item)) {
            return getBucketForItem(item).frequency;
        }
        return 0L;
    }
    
    public boolean contains(T item) {
        return dataStructure.contains(item);
    }

    @Override
    public long size() {
        return elementsCounted;
    }
    
    @Override
    public Set<T> keySet() {
        return null;
    }
    
    public List<CountEntry<T>> getFrequentItems(double minSupport) {
        List<CountEntry<T>> result = new ArrayList<CountEntry<T>>();
        int j = 1;
        
        for (Bucket<List<T>> b : dataStructure) {
            if (((b.frequency - error) > (minSupport * elementsCounted)) && (j <= dataStructure.size())) {
                for(int i = 0; i < b.item.size(); i++) {
                    result.add(new CountEntry<T>(b.item.get(i), b.frequency));
                }
                
                if ((b.frequency - error) < (minSupport * elementsCounted)) {
                    guaranteed = false;
                }
            }
            j++;
        }
        return result;
    }
    
    /**
     * Shows if the frequent Items are still in the guaranteed
     * bounds of the algorithm.
     * @return
     */
    public boolean getGuaranteed() {
        return guaranteed;
    }

    
    private boolean containsItem(T item){
    	Bucket<List<T>> bucket = getBucketForItem(item);
        return bucket != null;
    }
    
    /**
     * <p>Increment the count frequency of the provided item by 1.</p>
     * <p>
     * First it checks two thinks.
     * <li>Is the Bucket which count has to be updated the last bucket?</li>
     * <li>Is the new frequency of the Bucket the same as the neighbor bucket frequency?</li>
     * If one of this is true the bucket with the least hits will be deleted and the new item will
     * get this bucket.<br/>
     * Otherwise: The item will be added into the neighbor bucket and removed from the original bucket.
     * </p>
     * <p>
     * In the end the whole data structure is sorted
     * </p>
     *
     * @param item The item whose frequency shall be incremented.
     * @param incrementCount The number that will be added to the item count.
     */
    private void incrementCount(T item, long incrementCount) {
    	Bucket<List<T>> firstBucket = getBucketForItem(item);
    	long bucketCount = firstBucket.frequency + incrementCount;
    	boolean replaceOldBucket = false;
        
    	if (dataStructure.indexOf(firstBucket) == dataStructure.size()-1) {
            replaceOldBucket = true;
    	} else if (dataStructure.get(dataStructure.indexOf(firstBucket)+1).frequency != bucketCount) {
            replaceOldBucket = true;
    	} else {
            Bucket<List<T>> neighborBucket = dataStructure.get(dataStructure.indexOf(firstBucket)+1);
            neighborBucket.item.add(item);
            firstBucket.item.remove(item);
    	}
        
    	if (replaceOldBucket) {
            Bucket<List<T>> bucket = dataStructure.get(0);
            long oldMaxError = bucket.getMaxError();
            bucket.item.clear();
            bucket.item.add(item);
            bucket.frequency += incrementCount;
            bucket.setMaxError(oldMaxError);
    	} else {
            firstBucket.frequency = bucketCount;
        }
        
    	elementsCounted++;
    	sortDataStructure();
    }
    
    /**
    * <p>
    * This method insert a new, not yet seen item, into the data structure.
    * <br />
    * The bucket with the least hits will be cleared and the new item will get this
    * bucket. Also the frequency will be increment.
    * <br />
    * In the last step the whole data structure will be sorted
    * </p>
    * @param item The item that is inserted into the model.
    */
    private void insertItem(T item, long incrementCount) {
    	Bucket<List<T>> bucket = dataStructure.get(0);
    	bucket.item.clear();
        bucket.item.add(item);
        bucket.frequency += incrementCount;
        elementsCounted++;
        sortDataStructure();    		
    }
    
    /**
     * <p>
     * This method sort the data structure by the frequency.
     * </p>
     */
    private void sortDataStructure() {
    	Collections.sort(dataStructure, new Comparator<Bucket<List<T>>>() {
            @Override
            public int compare(Bucket<List<T>> o1, Bucket<List<T>> o2) {
                    return Long.valueOf(o1.frequency).compareTo(o2.frequency);
            }
	});
    }
    
    /**
     * <p>
     * This method returns the bucket which contains the item or null
     * if the items is in no bucket.
     * </p>
     * @param item The item which you search for in all buckets 
     * @return Bucket with the item or null if Element is not enclosed.
     */
    private Bucket<List<T>> getBucketForItem(T item) {
    	for (Bucket<List<T>> b : dataStructure) {
            if (b.item.contains(item)){
                return b;
            }
    	}
    	return null;
    }
    
    private class Bucket<T> extends CountEntryWithMaxError<T> {
        private static final long serialVersionUID = 1L;

        public Bucket(T item, long frequency, long maxError){
            super(item, frequency, maxError);
        }

        public void setMaxError(long maxError){
            this.maxError = maxError;
        }
        public long getMaxError(){
            return this.maxError;
        }
    }
}
