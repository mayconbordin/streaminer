package org.streaminer.stream.frequency;

import java.util.Random;
import org.streaminer.util.distribution.ZipfDistribution;
import org.streaminer.util.hash.HashUtils;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class StreamGenerator {
    private Random random = new Random();
    private ZipfDistribution zipf;
    
    int[] exact;
    long[] stream;
    long sumsq;
    long distinct;
    int[] quartiles;
    
    private int range;
    private int n;
    private long a;
    private long b;

    public StreamGenerator(double theta, int n, int range) {
        this.n = n;
        this.range = range;
        zipf = new ZipfDistribution(theta, n, range);
        
        exact  = new int[n+1];
        stream = new long[range+1];
        
        a = random.nextInt() % HashUtils.MOD;
        b = random.nextInt() % HashUtils.MOD;
    }
    
    public void generate() {
        long value;
        
        // generate stream
        for (int i=1; i<=range; i++) {
            value =  (HashUtils.hash31(a,b, (long) zipf.nextDouble())&1048575);
            exact[(int)value]++;
            stream[i] = value;
            System.out.println("Stream " + i + " is " + value);
        }
    }
    
    public void exact() {
        sumsq=0;
        distinct=0;
        
        for (int i=0; i<n; i++) {
            sumsq += (long)exact[i] *  (long)exact[i];
            if (exact[i]>0) {
                distinct++;
            }
        }
    }
    
    public int exact(int thresh) {
        quartiles = new int[4];
        
        int hh = 0, sum = -1, j, max;
        j=0; sumsq=0;
        
        for (int i=0; i<n; i++) {
            sum += exact[i];
            sumsq += (long) exact[i] *  (long) exact[i];
            if (exact[i] > 0) max=i;
            while (sum>=0) {
                quartiles[j]=i;
                j++;
                sum -= range/4;
            }
            if (exact[i] >= thresh) {
                hh++;
            }
        }

        return hh;
    }
    
    public void checkOutput(int[] resultlist, int thresh, int hh) {
        int i;
        int correct=0;
        int claimed=0;
        int last=0;
        double recall, precision;

        if (resultlist[0] > 0) {
            for (i=1;i<=resultlist[0]; i++) {
                claimed++;
                if (resultlist[i] != last) {
                    correct += testout(resultlist[i], thresh);
                    last = resultlist[i];
                }
            }
        } else
            claimed = 1;
        
        //if (hh==0)
        //else
        if (hh != 0) {
            recall = 100.0 * ((double) correct)/((double) hh);
            precision = 100.0 * ((double) correct)/((double) claimed);
            System.out.println("Recall=" + recall + "; Precision=" + precision);
        }
    }
    
    private int testout(int item, int thresh) {
        return (exact[item] >= thresh) ? 1 : 0;
    }
    
}
