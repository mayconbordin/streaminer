package org.streaminer.util.hash.function;

import org.streaminer.util.hash.JenkinsHash;

/**
 *
 * @author maycon
 * @param <T>
 */
public class JenkinsHashFunction<T> implements HashFunction<T> {

    public long hash(T x) {
        return JenkinsHash.getInstance().hash64(x);
    }
    
}
