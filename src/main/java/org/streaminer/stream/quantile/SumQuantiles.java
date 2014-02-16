package org.streaminer.stream.quantile;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SumQuantiles implements IQuantiles<Double>, Serializable {
    private static final long serialVersionUID = 7331604622548281844L;
    private int slidingWindowSize = 5000;
    private int maxBucketCount = 10;
    private int elementPerBucket  = slidingWindowSize / maxBucketCount;
    private int biggestSeenElement = 0;
    private Bucket newestBucket = null;
    private List<Bucket> buckets;
    
    public SumQuantiles(int slidingWindowSize, int bucketCount) {
        this.slidingWindowSize = slidingWindowSize;
        this.maxBucketCount = bucketCount;
        elementPerBucket  = slidingWindowSize / maxBucketCount;
        buckets = new CopyOnWriteArrayList<Bucket>();
        addNewBucket();
    }
    
    @Override
    public void offer(Double value) {
        biggestSeenElement = Math.max(biggestSeenElement, value.intValue());
        newestBucket.learn(value);
        if (newestBucket.isFull()) {
            addNewBucket();
        }
    }

    @Override
    public Double getQuantile(double q) throws QuantilesException {
        int overallElementCount = 0;
		
        for (Bucket bucket : buckets) {
            overallElementCount += bucket.getElementCount();
        }

        int wantedRank = (int)((double)overallElementCount * q);
        int sum = 0;

        //System.out.println("--------------------------------------" );
        //System.out.println("ElementCount " + overallElementCount);
        //System.out.println("wantedRank " + wantedRank);

        for (int i = 0; i < biggestSeenElement; i++) {
            long predict = getAllBucketPrediction(i);
            sum += predict;
//			System.out.println(sum);
            if (sum >= wantedRank) {				
                return (double)i;
            }
        }
        return 0.0;
    }
    
    private void addNewBucket() {
        Bucket newBucket = new Bucket(); 
        buckets.add(newBucket);	
        newestBucket = newBucket;
        deleteExcessiveBuckets();
        //System.out.println("new");
    }
    
    /**
     * delete oldest {@link Bucket}  while we have too many of them.
     */
    private void deleteExcessiveBuckets() {
        while (buckets.size() > maxBucketCount) {			
            buckets.remove(0);
        }
    }
    
    private int getAllBucketPrediction(int item) {
        int prediction = 0;
        for (Bucket bucket : buckets) {
            prediction += bucket.predict(item);
        }
        return prediction;
    }
    
    private class Bucket implements Serializable {
        private static final long serialVersionUID = -2211156505869843563L;
        private int elementCount = 0;
        private Map<String,Integer> counterMap ;

        public Bucket() {
            counterMap = new ConcurrentHashMap<String, Integer>();		 
        }

        public long predict(int item){			 
            String asString = ((Integer)item).toString();
            if (counterMap.containsKey(asString)) {
                return counterMap.get(asString);
            }
            return 0;			 
        }

        public void learn(Double item) {
            int value = item.intValue();
            String asString = ((Integer)value).toString();
            if (counterMap.containsKey(asString)) {
                int counter = counterMap.get(asString);
                counter++;
                counterMap.put(asString, counter);
            } else {
                counterMap.put(asString, 1);
            }
            elementCount++;
        }

        public int getElementCount() {
            return elementCount;
        }

        public boolean isFull() {
            return elementCount >= elementPerBucket;
        }

        @Override
        public String toString() {
            System.out.println("--------------------------------------" );
            String out = "Bucket: \n";
            for(String key : counterMap.keySet()) {
                out = out + key + "  " +counterMap.get(key) + "\n";
            }
            return out;
        }
    }
    
}
