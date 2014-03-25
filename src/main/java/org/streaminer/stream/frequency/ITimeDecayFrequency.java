package org.streaminer.stream.frequency;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 * @param <K>
 */
public interface ITimeDecayFrequency<K> {
    public void add(K item, long qtd, long timestamp);
    public double estimateCount(K item, long timestamp);
}
