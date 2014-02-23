package org.streaminer.stream.cardinality;

import java.io.Serializable;
import java.util.*;
import org.streaminer.util.hash.function.HashFunction;
import org.streaminer.util.hash.function.MurmurHashFunction;

/**
 * Implementation of the BJKST algprothm for distinct counting.
 * 
 * Source code: https://github.com/ananthc/streamstats
 * 
 * Reference:
 *   Bar-Yossef, Ziv, et al. "Counting distinct elements in a data stream." 
 *   Randomization and Approximation Techniques in Computer Science. Springer 
 *   Berlin Heidelberg, 2002. 1-10.
 * 
 * @author ananthc
 */
public class BJKST implements Serializable {
    private static final long serialVersionUID = -2032575802259420762L;

    private int numMedians=25;
    private int sizeOfMedianSet;
    
    private double error = 0.02f;
    
    private List<Integer> limits;
    
    private int bufferSize = 100;
    private List<HashSet<String>> buffers;

    private List<HashFunction<Object>> hHashers;
    private List<HashFunction<Object>> gHashers;

    private int intLength = Integer.toString(Integer.MAX_VALUE).length();
    private String lengthOfIntegerRepresentation = null;

    public BJKST(int numberOfMedianAttempts, int sizeOfEachMedianSet) {
        this.numMedians = numberOfMedianAttempts;
        this.sizeOfMedianSet = sizeOfEachMedianSet;
        init();
    }

    public BJKST(int numberOfMedianAttempts, int sizeOfEachMedianSet, double allowedError) {
        if (allowedError < 0 || allowedError > 1) {
            throw new IllegalArgumentException("Permitted error should be < 1 and in float format");
        }

        this.numMedians = numberOfMedianAttempts;
        this.sizeOfMedianSet = sizeOfEachMedianSet;
        this.error = allowedError;
        init();
    }

    private void init() {
        this.bufferSize =  (int) ((this.sizeOfMedianSet) / Math.pow(this.error,2.0) ) ;
        lengthOfIntegerRepresentation = ("%0" + intLength + "d");
        
        limits   = new ArrayList<Integer>(numMedians);
        buffers  = new ArrayList<HashSet<String>>(numMedians);
        hHashers = new ArrayList<HashFunction<Object>>(numMedians);
        gHashers = new ArrayList<HashFunction<Object>>(numMedians);
        
        for ( int i =0 ; i < numMedians; i++) {
            limits.add(0);
            buffers.add(new HashSet<String>());
            hHashers.add(new MurmurHashFunction<Object>());
            gHashers.add(new MurmurHashFunction<Object>());
        }
    }

    public void offer(Object o) {
        for ( int i =0 ; i < numMedians; i++) {
            String binaryRepr = Long.toBinaryString(hHashers.get(i).hash(o));
            
            int zereosP = binaryRepr.length() - binaryRepr.lastIndexOf('1');
            int currentZ = limits.get(i);
            
            if (zereosP >= currentZ) {
                HashSet<String> currentBuffer = buffers.get(i);
                
                currentBuffer.add(String.format(lengthOfIntegerRepresentation, gHashers.get(i).hash(o)) +
                            String.format(lengthOfIntegerRepresentation, zereosP));

                while (currentBuffer.size() > bufferSize) {
                    currentZ = currentZ + 1;
                    for (Iterator<String> itr = currentBuffer.iterator(); itr.hasNext();) {
                        String element = itr.next();
                        long zeroesOld = Long.parseLong(element.substring(intLength));
                        if (zeroesOld < currentZ) {
                            itr.remove();
                        }
                    }
                }
            }
        }
    }
    
    public long cardinality() {
        HashMap<Integer,Integer> results = new HashMap<Integer,Integer>();
        for ( int i =0 ; i < numMedians; i++) {
            int currentGuess = (int)  (buffers.get(i).size() * Math.pow(2,limits.get(i)));
            if (!results.containsKey(currentGuess)) {
                results.put(currentGuess,1);
            }
            else {
                int currentCount = results.get(currentGuess);
                results.put(currentGuess,(currentCount + 1));
            }
        }
        
        int finalEstimate = 0;
        int highestVote = 0;
        for (Map.Entry<Integer,Integer> pair : results.entrySet()) {
            int possibleAnswer = pair.getValue();
            if (possibleAnswer > highestVote ) {
                highestVote = possibleAnswer;
                finalEstimate = pair.getKey();
            }
        }
        
        return finalEstimate;
    }
}