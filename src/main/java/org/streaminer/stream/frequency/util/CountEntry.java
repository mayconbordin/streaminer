package org.streaminer.stream.frequency.util;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Standard data structure for a single item in the context of a counting algorithm.</p>
 *
 * <p>Contains fields for the item itself and its frequency.</p>
 *
 * @author Benedikt Kulmann, office@kulmann.biz
 * @param <T>
 */
public class CountEntry<T> implements Serializable, Cloneable, Comparable<CountEntry> {

    private transient static final Logger LOG = LoggerFactory.getLogger(CountEntry.class);

    private static final long serialVersionUID = 1L;

    /**
     * The item this {@link CountEntry} is associated with.
     */
    public T item;

    /**
     * The frequency of this {@link CountEntry}s item within a counting algorithm.
     */
    public long frequency;

    /**
     * <p>Constructs a new instance of {@link CountEntry}.</p>
     *
     * @param item The item this {@link CountEntry} represents
     * @param frequency An initial count frequency. For the default initial frequency
     * use {@link #CountEntry(java.lang.Object)}
     */
    public CountEntry(T item, long frequency) {
        this.item = item;
        this.frequency = frequency;
    }

    /**
     * <p>Constructs a new instance of {@link CountEntry}.</p>
     *
     * @param item The item this {@link CountEntry} represents
     */
    public CountEntry(T item) {
        this(item, 0L);
    }

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }

    public long getFrequency() {
        return frequency;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CountEntry[item=" + item + ", freq=" + frequency + "]";
    }

    /**
     * {@inheritDoc}
     * @return 
     * @throws java.lang.CloneNotSupportedException 
     */
    @Override
    public CountEntry<T> clone() throws CloneNotSupportedException {
        try {
            CountEntry<T> clone = (CountEntry<T>)super.clone();
            return clone;
        } catch(CloneNotSupportedException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    public int compareTo(CountEntry o) {
        long x = o.getFrequency();
        long y = frequency;
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (!(obj instanceof CountEntry)) {
            return false;
        }
        
        CountEntry other = (CountEntry) obj;
        return this.item.equals(other.item);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.item != null ? this.item.hashCode() : 0);
        return hash;
    }

    
}
