package org.streaminer.stream.cardinality;

import java.util.TreeSet;
import org.streaminer.util.hash.Hash;

/**
 * K-Minimum Values.
 * Python Source Code: https://github.com/mynameisfiber/countmemaybe
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class KMinValues implements IBaseCardinality {
    private TreeSet<Integer> kMin;
    private int k;
    private Hash hasher;

    public KMinValues(int k) {
        this(k, Hash.getInstance(Hash.MURMUR_HASH3));
    }

    public KMinValues(int k, Hash hasher) {
        this.kMin = new TreeSet<Integer>();
        this.k = k;
        this.hasher = hasher;
    }
    
    public boolean offer(Object key) {
        int idx = index(key);
        
        if (kMin.size() < k) {
            if (!kMin.contains(idx)) {
                kMin.add(idx);
                return true;
            }
        } else {
            if (idx < kMin.last())
                if (!kMin.contains(idx)) {
                    kMin.pollLast();
                    kMin.add(idx);
                    return true;
                }
        }
        
        return false;
    }
    
    public long cardinality() {
        if (kMin.size() < k)
            return kMin.size();
        else
            return (long) cardHelp(kMin, k);
    }
    
    public void union(KMinValues... others) {
        int newK = smallestK(others);
        for (KMinValues o : others)
            kMin.addAll(o.kMin);
        
        kMin = new TreeSet<Integer>(kMin.subSet(0, newK));
    }
    
    public double jaccard(KMinValues other) {
        DirectSum ds = directSum(other);
        return ds.n / (1.0 * ds.x.size());
    }
    
    public double cardinalityUnion(KMinValues... others) {
        DirectSum ds = directSum(others);
        double cardX = cardHelp(ds.x, ds.x.size());
        return cardX;
    }
    
    private double cardHelp(TreeSet<Integer> kMin, int k) {
        return ((k - 1.0) * Integer.MAX_VALUE) / (kMin.last());
    }
    
    private boolean inAll(int item, KMinValues... others) {
        for (KMinValues o : others)
            if (!o.kMin.contains(item))
                return false;
        return true;
    }
    
    private DirectSum directSum(KMinValues... others) {
        DirectSum ds = new DirectSum();
        int k = smallestK(others);
        
        for (KMinValues o : others)
            ds.x.addAll(o.kMin);
        
        ds.x = new TreeSet<Integer>(ds.x.subSet(0, k));
        
        for (int item : ds.x)
            if (kMin.contains(item) && inAll(item, others))
                ds.n++;
        
        return ds;
    }
    
    private int smallestK(KMinValues... others) {
        int newK = Integer.MAX_VALUE;
        for (KMinValues o : others) {
            if (o.k < newK)
                newK = o.k;
        }
        return newK;
    }
    
    private int index(Object key) {
        return hasher.hash(key) & Integer.MAX_VALUE;
    }
    
    private static class DirectSum {
        public int n = 0;
        public TreeSet<Integer> x;
    }
}
