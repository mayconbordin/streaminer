package org.streaminer.stream.frequency;

import org.streaminer.util.hash.function.HashFunction;
import org.streaminer.util.hash.function.TwoUniversalHashFunction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * <p>
 * {@link Learner}-part of the implementation of the CountMinSketch algorithm from the paper
 * 'An improved data stream summary: the count-min sketch and its
 * 	applications' written by 
 * 'Cormode, G. and Muthukrishnan, S. (2003)'.
 * </p>
 * 
 * @author Marcin Skirzynski (main work), Benedikt Kulmann (modifications)
 */
public class CountMinSketch<T> extends CountSketch<T> {

    /**
     * <p>
     * Constructor of the CountMinSketch algorithm. This construction
     * can take quite a long time since the construction of the hashfunctions
     * is rather time-consuming.
     * </p>
     *
     * @param domain The (estim.) domain, i.e. how many different items are expected
     * @param numberOfHashFunctions The number of hashfunctions which determine a bucket
     * @param numberOfBuckets The number of buckets where a counter will be maintained
     * @param k parameter for the top-k variant. If you want to disable
     * the top-k overhead, than set k to 0 or lower
     */
    public CountMinSketch(int domain, int nrOfHashFunctions, int nrOfbuckets, int k) {
        super(domain, nrOfHashFunctions, nrOfbuckets, k);
    }
    
    /**
     * <p>
     * We only need the h-hash functions since
     * the CountMinSketch algorithm always increments
     * the values in the data.
     * </p>
     *
     * @param domain the (estim.) domain, i.e. how many different items are expected
     * @param nrOfHashFunctions the number of hashfunctions which determine a bucket
     * @param nrOfbuckets the number of buckets where a counter will be maintained
     */
    protected void initializeHashes(int domain, int nrOfHashFunctions, int nrOfbuckets) {
        h = new ArrayList<HashFunction<T>>();
        s = new ArrayList<HashFunction<T>>();

        for (int i = 0; i < nrOfHashFunctions; i++) {
            h.add(new TwoUniversalHashFunction<T>(domain, nrOfbuckets));
        }
    }
    
    /**
     * <p>
     * Updating the data. For each hashfunction the corresponding
     * bucket will be incremented by one.
     * </p>
     */
    @Override
    protected boolean updateData(T item) {
        for (int i = 0; i < h.size(); i++) {
            int hi = (int) h.get(i).hash(item);
            data[i][hi] += 1;
        }

        return k <= 0;
    }

    /**
     * <p>
     * Estimates the frequency by returning the
     * smallest frequency value for each hasfunction index.
     * </p>
     */
    @Override
    public long estimateFrequency(T item) {
        Collection<Integer> values = new ArrayList<Integer>();
        for (int i = 0; i < h.size(); i++) {
            int hi = (int) h.get(i).hash(item);
            values.add(data[i][hi]);
        }

        return Collections.min(values);
    }
}
