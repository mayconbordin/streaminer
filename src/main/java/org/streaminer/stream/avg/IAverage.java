package org.streaminer.stream.avg;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public interface IAverage {
    public void add(double value);
    public double getAverage();
    public void clear();
}
