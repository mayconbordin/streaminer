package org.streaminer.stream.change;

import cern.colt.Arrays;
import java.util.Random;
import org.streaminer.util.distribution.ZipfDistribution;
import org.streaminer.util.hash.HashUtils;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class StreamGenerator {
    /**
     * The skewness of the zipf distribution to use to generate the streams.
     */
    private double zipfpar = 0.8;
    
    /**
     * The data streams.
     */
    private long[][] streams;
    
    /**
     * Length of the streams.
     */
    private int streamLength = 50000;
    
    /**
     * Number of streams to be generated.
     */
    private int noStreams = 2;
    
    private int offset = 52521;
    
    private Random random = new Random();
    private ZipfDistribution zipf;
    
    public StreamGenerator(int length, int offset, double z, int noStreams) {
        this.streamLength = length;
        this.offset = offset;
        this.noStreams = noStreams;
        
        zipf = new ZipfDistribution(z, length);
        streams = new long[noStreams][length];
    }
    
    public void generate() {
        long a, b, value;
        
        for (int s=0; s<noStreams; s++) {
            a = random.nextLong() % HashUtils.MOD;
            b = random.nextLong() % HashUtils.MOD;
            
            for (int i=0; i<streamLength; i++) {
                // get a value from the zipf dbn, and hash it to a new place
                // use offset to mix things up a bit 
                value = ((offset*i) + HashUtils.hash31(a, b, (int)Math.floor(zipf.nextDouble())))&1048575;;
                
                streams[s][i] = value;
            }
        }
    }
    
    
    // debug -----------------------------------------------------------------//
    public void printStreams() {
        System.out.println("Streams output:");
        
        for (int s=0; s<noStreams; s++) {
            System.out.println("[S" + s + "]" + Arrays.toString(streams[s]));
        }
    }

    // getters and setters ---------------------------------------------------//

    public double getZipfpar() {
        return zipfpar;
    }

    public void setZipfpar(double zipfpar) {
        this.zipfpar = zipfpar;
    }

    public long[][] getStreams() {
        return streams;
    }

    public void setStreams(long[][] streams) {
        this.streams = streams;
    }

    public int getSlen() {
        return streamLength;
    }

    public void setSlen(int slen) {
        this.streamLength = slen;
    }

    public int getNoStreams() {
        return noStreams;
    }

    public void setNoStreams(int noStreams) {
        this.noStreams = noStreams;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    
}
