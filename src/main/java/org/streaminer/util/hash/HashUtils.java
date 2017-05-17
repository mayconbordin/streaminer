package org.streaminer.util.hash;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class HashUtils {
    public static final int MOD = 2147483647;
    public static final int HL = 31;
    
    /**
     * return a hash of x using a and b mod (2^31 - 1) may need to do another mod 
     * afterwards, or drop high bits depending on d, number of bad guys
     * 2^31 - 1 = 2147483647
     * @param a
     * @param b
     * @param x
     * @return 
     */
    public static long hash31(long a, long b, long x) {
        long result = (a * x) + b;
        result = ((result >> HL) + result) & MOD;

        return result;
    }

    /**
     * returns values that are 4-wise independent by repeated calls to the 
     * pairwise indpendent routine. 
     * @param a
     * @param b
     * @param c
     * @param d
     * @param x
     * @return 
     */
    public static long fourwise(long a, long b, long c, long d, long x) {
        long result = hash31(hash31(hash31(x,a,b),x,c),x,d);
        return result;
    }
    
    // Murmur is faster than an SHA-based approach and provides as-good collision
    // resistance.  The combinatorial generation approach described in
    // https://www.eecs.harvard.edu/~michaelm/postscripts/tr-02-05.pdf
    // does prove to work in actual tests, and is obviously faster
    // than performing further iterations of murmur.
    public static int[] getHashBuckets(String key, int hashCount, int max) {
        byte[] b;
        try {
            b = key.getBytes("UTF-16");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return getHashBuckets(b, hashCount, max);
    }

    static int[] getHashBuckets(byte[] b, int hashCount, int max) {
        int[] result = new int[hashCount];
        int hash1 = MurmurHash.getInstance().hash(b, b.length, 0);
        int hash2 = MurmurHash.getInstance().hash(b, b.length, hash1);
        for (int i = 0; i < hashCount; i++) {
            result[i] = Math.abs((hash1 + i * hash2) % max);
        }
        return result;
    }
}
