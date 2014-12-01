package org.streaminer.stream.change;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Exact solution for finding deltoids, used only for comparing results.
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class ExactSolution {
    /**
     * The fraction above which something is a deltoid (significant change),
     * this is typically small, say 0.01 or 0.001.
     */
    private double phi = 0.005;
    
    private double thresh;
    private double relThresh;
    
    private long[][] c1;
    private long[][] c2;
    
    private int t1;
    private int t2;
    
    private double netchange;
    private long netpos;
    
    private List<Long> absDeltoids;
    private List<Long> relDeltoids;

    public ExactSolution(long[] str1, long[] str2, double phi) {
        this.phi = phi;
        
        // allocate space for the sorted output
        c1 = new long[str1.length + 2][2];
        c2 = new long[str2.length + 2][2];
        
        // first we sort the streams and compact duplicates
        t1 = collect(str1, c1);
        t2 = collect(str2, c2);
    }
    
    public void computeDeltoids() {
        int i=0, j=0;
        computeL1();
        
        System.out.println("netpos="+netpos+"; netchange="+netchange);
        
        // derive the threshold for being a deltoid from the L1 difference
        thresh = ((double) netpos) * phi;
        if (thresh == 0) thresh = 1;
        
        // if desired, can also work out threshold for relative deltoids
        relThresh = netchange * phi;
        
        // create the lists of deltoids
        absDeltoids = new ArrayList<Long>();
        relDeltoids = new ArrayList<Long>();
        
        // compute the difference in count for each item, and test whether 
        // this is greater than the threshold for being a deltoid
        while (i <= t1 && j <= t2) {
            if (c1[i][0] == c2[j][0]) {
                addDeltoid(c1[i][0], c1[i][1] , c2[j][1]);
                i++;
                j++;
            } else if (c1[i][0] < c2[j][0]) {
                addDeltoid(c1[i][0], c1[i][1], 0);
                i++;
            } else if (c1[i][0] > c2[j][0]) {
                addDeltoid(c2[j][0], 0, c2[j][1]);
                j++;
            }
        }
        
        while (j <= t2) {
            addDeltoid(c2[j][0], 0, c2[j][1]);
            j++;
        }
        
        while (i <= t1) {
            addDeltoid(c1[i][0], c1[i][1], 0);
            i++;
        }
        
    }
    
    public void computeL1() {
        int i = 0, j = 0;
        netpos = 0;
        netchange = 0.0;
        double rc;
        
        while (i <= t1 && j <= t2) {
            if (c1[i][0] == c2[j][0]) {
                rc = ((double) c1[i][1]) / ((double) c2[j][1]);
                netchange += rc;
                netpos += Math.abs(c1[i++][1] - c2[j++][1]);
            } else if (c1[i][0] < c2[j][0]) {
                netchange += (double) c1[i][1]; // normalize missing value to 1
                netpos += Math.abs(c1[i++][1]);
            } else if (c1[i][0] > c2[j][0]) {
                // does not count towards netchange
                netpos += Math.abs(c2[j++][1]);
            }
        }
        
        while (j <= t2) {
            netpos += Math.abs(c2[j++][1]);
        }
        
        while (i <= t1) {
            netchange += (double) c1[i][1];
            netpos += Math.abs(c1[i++][1]);
        }
        
        // netpos records the total L1 difference 
        // between the two streams
    }
    
    /**
     * Add an item to the list of deltoids if its count is high enough.
     * @param item
     * @param count1
     * @param count2 
     */
    private void addDeltoid(long item, long count1, long count2) {
        if (Math.abs(count1 - count2) >= thresh)
            absDeltoids.add(item);
        
        if (count2 == 0)
            if ((double) count1 > relThresh)
                relDeltoids.add(item);
        else
            if (((double) count1 / (double) count2) > relThresh)
                relDeltoids.add(item);
    }
    
    private int collect(long[] str, long[][] c) {
        int prevptr = 0, t = 0, collect = 0;
        
        Arrays.sort(str);
        
        for (int i=0; i<str.length; i++) {
            if (str[i] != str[prevptr]) {
                c[t][0] = str[prevptr];
                c[t][1] = collect;
                
                // record the number of times the previous item was seen, and 
                // set up for the next one.
                t++;
                prevptr = i;
                collect = 0;
            }
            
            collect++;
        }
        
        c[t][0] = str[prevptr];
        c[t][1] = collect;
        
        return t;
    }

    public double getPhi() {
        return phi;
    }

    public void setPhi(double phi) {
        this.phi = phi;
    }

    public double getThresh() {
        return thresh;
    }

    public void setThresh(double thresh) {
        this.thresh = thresh;
    }

    public double getRelThresh() {
        return relThresh;
    }

    public void setRelThresh(double relThresh) {
        this.relThresh = relThresh;
    }

    public List<Long> getAbsDeltoids() {
        return absDeltoids;
    }

    public void setAbsDeltoids(List<Long> absDeltoids) {
        this.absDeltoids = absDeltoids;
    }

    public List<Long> getRelDeltoids() {
        return relDeltoids;
    }

    public void setRelDeltoids(List<Long> relDeltoids) {
        this.relDeltoids = relDeltoids;
    }
    
    
}
