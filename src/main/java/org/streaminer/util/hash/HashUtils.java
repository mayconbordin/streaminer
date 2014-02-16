package org.streaminer.util.hash;

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
}
