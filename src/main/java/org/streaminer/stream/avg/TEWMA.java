
package org.streaminer.stream.avg;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Time Exponential Weighted Moving Average implements a smoothed rate meter using
 * a CBF and a timestamp array it follows the TEWMA rules.
 * 
 * Reference:
 *  Martin, Ruediger, and Michael Menth. "Improving the Timeliness of Rate 
 *   Measurements." In MMB, pp. 145-154. 2004.
 * 
 * Translated from a C++ implementation from the 
 * <a href="https://github.com/blockmon/blockmon">Blockmon</a> project.
 * 
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class TEWMA {
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private MessageDigest sha1;
    
    private double beta;
    private double logBeta;
    private int nhash;
    private int shash;
    private double w;
    private int digest;
    private double[] counters;
    private long[] timers;

    /**
     * Creates a new data structure with nhash hash functions, each size shash bits.
     * @param nhash Number of hash functions
     * @param shash Size in bits of each hash digest (multiple of 8)
     * @param beta Smoothing parameter
     * @param w Time unit (in seconds)
     */
    public TEWMA(int nhash, int shash, double beta, double w) {
        if (w < 1) {
            throw new IllegalArgumentException("w should be greater than zero.");
        }
        if (nhash < 1) {
            throw new IllegalArgumentException("nhash should be greater than zero.");
        }
        if (shash < 1 || shash%8 != 0) {
            throw new IllegalArgumentException("shash should be greater than zero and multiple of eight.");
        }
        
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("SHA-1 algorithm not found", ex);
        }
        
        this.nhash = nhash;
        this.shash = shash;
        this.beta = beta;
        this.w = w;
        
        digest = 0x00000001 << shash;
        logBeta = -Math.log(beta);
        
        counters = new double[digest];
        timers = new long[digest];
    }
    
    /**
     * Add an item to the data structure.
     * @param item The item to be added
     * @param quantity The value of the item
     * @param timestamp The epoch time in seconds for the item
     * @return The new value of the item
     */
    public double add(Object item, double quantity, long timestamp) {
        // compute the hash functions
        int[] idx = indexes(item);
        
        double minCounter = getMinCounter(idx, timestamp);
        
        // compute the new value including the insertion and update 
	// the filter by waterfilling the new counter array bins
        double newCounter = minCounter + quantity * logBeta;
        
        if (newCounter > 1.0e99)
            throw new AssertionError("newCounter shouldn't be greater than 1.0e99.");
        
        for (int i=0; i<nhash; i++)
            if (counters[idx[i]] < newCounter)
                counters[idx[i]] = newCounter;
        
        return newCounter;
    }
    
    /**
     * Return the current value for the item.
     * @param item The item to be checked
     * @param timestamp The epoch time in seconds of the item
     * @return The current value for the item
     */
    public double check(Object item, long timestamp) {
        return getMinCounter(indexes(item), timestamp);
    }
    
    /**
     * Get the currrent value for an item with idx hash functions
     * @param idx The hash functions for the item to be checked
     * @param timestamp The timestamp of the item
     * @return The current value of the item
     */
    protected double getMinCounter(int[] idx, long timestamp) {
        double minCouter = counters[idx[0]];
        double deltat;
        
        // computes the new counter decayed value
        for(int i=0; i<nhash; i++) {
            deltat = (timestamp - timers[idx[i]])/w;
            timers[idx[i]] = timestamp;
            
            if (deltat < 0)
                throw new AssertionError("deltat shouldn't be less than zero.");
            
            counters[idx[i]] *= Math.pow(beta, deltat);
            if (counters[idx[i]] < minCouter)
                minCouter = counters[idx[i]];
        }
        
        return minCouter;
    }
    
    /**
     * Generate the hash functions for an item.
     * @param o The item for which the hash functions will be calculated
     * @return The hash functions for the item
     */
    protected int[] indexes(Object o) {
        int[] indexes = new int[nhash];
        byte[] msgDigest = toSHA1(o);
        int numBytes = shash/8;
        
        int k = 0;
        for (int i=0; i<nhash; i++) {
            byte[] bytes = new byte[numBytes];
            for (int j=numBytes-1; j>=0; j--, k++) {
                if (k >= msgDigest.length) {
                    msgDigest = toSHA1(msgDigest);
                    k = 0;
                }
                
                bytes[j] = msgDigest[k];
            }
            
            String hex = bytesToHex(bytes);
            indexes[i] = Integer.parseInt(hex, 16);
        }
        
        return indexes;
    }
    
    /**
     * Converts an array of bytes into a hexadecimal string.
     * @param bytes The array of bytes to be converted
     * @return A string containing the hex value
     */
    protected String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    /**
     * Converts an object to a byte array to generate the SHA-1 digest. Numbers
     * are converted to {@link String}, strings are then converted to bytes. Any
     * other object will have its hash code converted to string and then to a byte
     * array.
     * @param o
     * @return 
     */
    protected byte[] toSHA1(Object o) {
        if (o instanceof byte[])
            return toSHA1((byte[]) o);
        if (o instanceof String)
            return toSHA1(((String)o).getBytes());
        if (o instanceof Number)
            return toSHA1(String.valueOf(o).getBytes());
        return toSHA1(String.valueOf(o.hashCode()).getBytes());
    }
    
    /**
     * Generates a SHA-1 digest for the given byte array. Each four bytes from the
     * digest are inverted, as in the original code, so that the results could be
     * compared.
     * 
     * @param in The byte array to be hashed
     * @return The byte array with the hash digest
     */
    protected byte[] toSHA1(byte[] in) {
        sha1.reset();
        
        byte[] tmp = sha1.digest(in);
        byte[] msgDigest = new byte[tmp.length];

        // invert the byte array
        int left = 0;
        for (int i=0; i<tmp.length; i++) {
            if (i != 0 && i%4 == 0)
                left += 4;
            msgDigest[i] = tmp[(4 - (i+1-left) + left)];
        }
        
        return msgDigest;
    }
}