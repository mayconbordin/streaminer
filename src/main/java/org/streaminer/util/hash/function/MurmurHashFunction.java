package org.streaminer.util.hash.function;

import org.streaminer.util.hash.MurmurHash;

/**
 *
 * @author maycon
 */
public class MurmurHashFunction<T> implements HashFunction<T> {

    public long hash(T x) {
        return MurmurHash.getInstance().hash64(x);
    }
    
}
