package org.streaminer.stream.sampling;

import java.util.Collection;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 * @param <T>
 */
public interface ISampler<T> {
    public void put(T t);
    public void put(T... t);
    public Collection<T> get();
    public int getSize();
}
