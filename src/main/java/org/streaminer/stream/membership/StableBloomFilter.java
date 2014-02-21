package org.streaminer.stream.membership;

import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;

/**
 * Stable Bloom Filter.
 * Similar interface to Google Guava's {@link com.google.common.hash.BloomFilter}.
 * Based on the document linked below, implementation leverages some of the Guava's code
 * from {@link com.google.common.hash.BloomFilter} and {@link com.google.common.hash.BloomFilterStrategies}.
 *
 * @see <a href="http://www.cs.ualberta.ca/~drafiei/papers/DupDet06Sigmod.pdf">
 *      Approximately Detecting Duplicates for Streaming Data using Stable Bloom Filters, by
 *      Fan Deng and Davood Rafiei, University of Alberta</a>
 * 
 * <a href="https://github.com/ru2nuts/stable_bloom_filter">Source code</a>
 */
public class StableBloomFilter<T> implements IFilter<T> {
    private static final int MAX_VAL = 3;//Integer.MAX_VALUE;
    private final int[] cells;
    private final int numHashFunctions;
    private final Funnel<T> funnel;
    private final Murmur128_Mitz_32_Strategy strategy;
    private final int numDecrementCells;

    public StableBloomFilter(int numCells, int numHashFunctions, int numDecrementCells, Funnel<T> funnel) {
        this.numDecrementCells = numDecrementCells;
        this.cells = new int[numCells];
        this.numHashFunctions = numHashFunctions;
        this.funnel = funnel;
        strategy = new Murmur128_Mitz_32_Strategy();
    }

    public boolean membershipTest(T object) {
        return strategy.mightContain(object, funnel, numHashFunctions, cells);
    }

    public void add(T object) {
        decrementCells();
        strategy.put(object, funnel, numHashFunctions, cells);
    }

    private void decrementCells() {
        int min = 0;
        int max = cells.length - 1;
        int decrementPos = min + (int) (Math.random() * ((max - min) + 1));
        for (int i = 0; i < numDecrementCells; i++) {
            if (decrementPos >= cells.length) {
                decrementPos = 0;
            }
            if (cells[decrementPos] > 0) {
                cells[decrementPos] = cells[decrementPos] - 1;
            }
            decrementPos++;
        }
    }

    private static class Murmur128_Mitz_32_Strategy {
        public <T> boolean put(T object, Funnel<? super T> funnel, int numHashFunctions, int[] cells) {
            // TODO(user): when the murmur's shortcuts are implemented, update this code
            long hash64 = Hashing.murmur3_128().newHasher().putObject(object, funnel).hash().asLong();
            int hash1 = (int) hash64;
            int hash2 = (int) (hash64 >>> 32);
            boolean bitsChanged = false;
            for (int i = 1; i <= numHashFunctions; i++) {
                int nextHash = hash1 + i * hash2;
                if (nextHash < 0) {
                    nextHash = ~nextHash;
                }
                int pos = nextHash % cells.length;
                bitsChanged |= (cells[pos] != MAX_VAL);
                cells[pos] = MAX_VAL;
            }
            return bitsChanged;
        }

        public <T> boolean mightContain(T object, Funnel<? super T> funnel, int numHashFunctions, int[] cells) {
            long hash64 = Hashing.murmur3_128().newHasher().putObject(object, funnel).hash().asLong();
            int hash1 = (int) hash64;
            int hash2 = (int) (hash64 >>> 32);
            for (int i = 1; i <= numHashFunctions; i++) {
                int nextHash = hash1 + i * hash2;
                if (nextHash < 0) {
                    nextHash = ~nextHash;
                }
                int pos = nextHash % cells.length;
                if (cells[pos] == 0) {
                    return false;
                }
            }
            return true;
        }
    }
}