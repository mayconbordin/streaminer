package org.streaminer.stream.cardinality;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Flajolet-Martin algorithm approximates the number of unique objects in a 
 * stream or a database in one pass.
 * 
 * Reference:
 *   Flajolet, Philippe, and G. Nigel Martin. "Probabilistic counting algorithms
 *   for data base applications." Journal of computer and system sciences 31.2 
 *   (1985): 182-209.
 * 
 * Source code: https://github.com/rbhide0/Columbus
 * 
 * @author Ravi Bhide
 */
public class FlajoletMartin {
    private static final double PHI = 0.77351D;
    private int numHashGroups;
    private int numHashFunctionsInHashGroup;
    private HashFunction[][] hashes;

    private int bitmapSize;
    private boolean[][][] bitmaps;

    private long numWords;

    public FlajoletMartin(int bitmapSize, int numHashGroups, int numHashFunctionsInEachGroup) {
        this.numHashGroups = numHashGroups;
        this.numHashFunctionsInHashGroup = numHashFunctionsInEachGroup;
        this.bitmapSize = bitmapSize;

        bitmaps = new boolean[numHashGroups][numHashFunctionsInEachGroup][bitmapSize];
        hashes = new HashFunction[numHashGroups][numHashFunctionsInEachGroup];

        generateHashFunctions();
    }

    private void generateHashFunctions() {
        Map<Integer, Collection<Integer>> mnMap = new HashMap<Integer, Collection<Integer>>();
        for (int i=0; i<numHashGroups; i++) {
            for (int j=0; j<numHashFunctionsInHashGroup; j++) {
                hashes[i][j] = generateUniqueHashFunction(mnMap);
            }
        }
    }

    private HashFunction generateUniqueHashFunction(Map<Integer, Collection<Integer>> mnMap) {
        // Get odd numbers for both m and n.
        int m = 0;
        do {
            m = (int) (Integer.MAX_VALUE * Math.random());
        } while (m % 2 == 0);

        // Get pairs that we haven't seen before.
        int n = 0;
        do {
            n = (int) (Integer.MAX_VALUE * Math.random());
        } while ((n % 2 == 0) || contains(mnMap, m, n));

        // Make a note of the (m, n) pair, so we don't use it again.
        Collection<Integer> valueCollection = mnMap.get(m);
        if (valueCollection == null) {
            valueCollection = new HashSet<Integer>();
            mnMap.put(m, valueCollection);
        }
        valueCollection.add(n);

        // Generate hash function with the (m, n) pair.
        // System.out.println("Generating hashFunction with (m=" + m + ", n=" + n + ")");
        return new HashFunction(m, n, bitmapSize);
    }

    private static boolean contains(Map<Integer, Collection<Integer>> map, int m, int n) {
        Collection<Integer> valueList = map.get(m);
        return (valueList != null) && (valueList.contains(n));
    }

    public boolean offer(Object o) {
        boolean affected = false;

        for (int i=0; i<numHashGroups; i++) {
            for (int j=0; j<numHashFunctionsInHashGroup; j++) {
                HashFunction f = hashes[i][j];
                long v = f.hash(o);
                int index = rho(v);
                if (!bitmaps[i][j][index]) {
                    bitmaps[i][j][index] = true;
                    affected = true;
                }
            }
        }

        return affected;
    }

    public long cardinality() {
        List<Double> averageR = new ArrayList<Double>();
        for (int i=0; i<numHashGroups; i++) {
            int sumR = 0;
            for (int j=0; j<numHashFunctionsInHashGroup; j++) {
                sumR += (getFirstZeroBit(bitmaps[i][j]));
            }
            averageR.add(sumR * 1.0 / numHashFunctionsInHashGroup);
        }

        // Find the median R and estimate unique items
        Collections.sort(averageR);
        double r = 0;
        int averageRMid = averageR.size() / 2;
        if (averageR.size() % 2 == 0) {
            r = (averageR.get(averageRMid) + averageR.get(averageRMid+1))/2;
        } else {
            r = averageR.get(averageRMid + 1);
        }

        return (long) (Math.pow(2, r) / PHI);
    }

    private int rho(long v) {
        int rho = 0;
        for (int i=0; i<bitmapSize; i++) { // size of long=64 bits.
            if ((v & 0x01) == 0) {
                v = v >> 1;
                rho++;
            } else {
                break;
            }
        }
        return rho == bitmapSize ? 0 : rho;
    }

    private static int getFirstZeroBit(boolean[] b) {
        for (int i=0; i<b.length; i++) {
            if (b[i] == false) {
                return i;
            }
        }
        return b.length;
    }

    private static class HashFunction {
        private int m_m;
        private int m_n;
        private int m_bitmapSize;
        private long m_pow2BitmapSize;

        public HashFunction(int m, int n, int bitmapSize) {
            if (bitmapSize > 64) {
                throw new IllegalArgumentException("bitmap size should be at max. 64");
            }
            this.m_m = m;
            this.m_n = n;
            m_bitmapSize = bitmapSize;

            m_pow2BitmapSize = 1 << m_bitmapSize;
        }

        public long hash(Object o) {
            if (o instanceof String)
                return hash(((String) o).hashCode());
            if (o instanceof Number)
                return hash(String.valueOf(o).hashCode());
            return hash(o.hashCode());
        }

        public long hash(long hashCode) {
            return m_m + m_n * hashCode;
        }
    }
}