package org.streaminer.util.hash.function;

import java.io.Serializable;
import java.util.Random;

public class SimpleHashFunction<T> implements HashFunction<T>, Serializable {
    /** The unique class ID */
    private static final long serialVersionUID = -946774756839033767L;
    private long domain;
    private Random random = new Random();

    public SimpleHashFunction(long domain) {
        this.domain = domain;
    }

    @Override
    public long hash(T x) {
        return Math.abs((x.hashCode()*random.nextInt())%domain);
    }
}