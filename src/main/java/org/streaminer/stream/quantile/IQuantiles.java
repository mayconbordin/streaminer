package org.streaminer.stream.quantile;

public interface IQuantiles<T>
{
    void offer(T value);

    T getQuantile(double q) throws QuantilesException;
}
