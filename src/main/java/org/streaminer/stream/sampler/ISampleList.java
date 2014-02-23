
package org.streaminer.stream.sampler;

import java.util.Collection;

public interface ISampleList<T> {
    public void sample(T t);
    public void sample(T... t);
    public Collection<T> getSamples();
    public int getSize();
}
