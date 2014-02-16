package org.streaminer.util.hash.factory;

import org.streaminer.util.hash.function.HashFunction;
import org.streaminer.util.hash.function.SimpleHashFunction;

/**
 * <p></p>
 *
 * @author Marcin Skirzynski
 * @param <T>
 */
public class SimpleHashFactory<T> implements HashFunctionFactory<T>{
    /** The unique class ID  */
    private static final long serialVersionUID = 1893281035106218246L;

    @Override
    public HashFunction<T> build(final long domain) {
        return new SimpleHashFunction(domain);
    }
}
