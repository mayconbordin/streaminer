package org.streaminer.util.hash.function;

import org.streaminer.util.math.Prime;
import java.io.Serializable;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Computes a two universal hash function lik it was described in the paper
 * 'Universal classes of hash functions' written by 'Carter, J. L. and Wegman, M. N. (1977)'.
 *  </p>
 *
 * @author Marcin Skirzynski
 *
 * @param <T>
 */
public class TwoUniversalHashFunction<T> implements HashFunction<T>, Serializable {

    private static final long serialVersionUID = -5451250100120165585L;
    private final long coDomain;
    private final long prime;

    private final long a;
    private final long b;

    private transient final Map<T, Long> indices = new ConcurrentHashMap<T, Long>();
    private long lastIndex = 0;

    public TwoUniversalHashFunction( int domain, int coDomain ) {
        this.coDomain = coDomain;
        this.prime = Prime.getRandom(domain, 2*(long)domain);

        Random rnd = new Random();
        long aTemp = 0;
        while (aTemp == 0) {
            aTemp = rnd.nextLong();
        }
        this.a = aTemp;
        this.b = -this.prime + (int)(Math.random() * ((this.prime + this.prime) + 1));

    }

    @Override
    public long hash(T x) {
        if( !this.indices.containsKey(x) ) {
            this.indices.put(x, ++this.lastIndex);
        }
        
        long xi = this.indices.get(x);
        return Math.abs(((this.a*xi + this.b) % this.prime)%this.coDomain);
    }
}
