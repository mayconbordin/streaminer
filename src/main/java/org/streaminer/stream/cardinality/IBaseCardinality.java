package org.streaminer.stream.cardinality;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public interface IBaseCardinality {
    /**
     * @param o stream element
     * @return false if the value returned by cardinality() is unaffected by the appearance of o in the stream.
     */
    boolean offer(Object o);
    
    /**
     * @return the number of unique elements in the stream or an estimate thereof
     */
    long cardinality();
}
