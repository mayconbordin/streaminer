package org.streaminer.util.hash;

/**
 *
 * @author mayconbordin
 */
public class SpookyHash extends Hash {
    
    private static SpookyHash _instance = new SpookyHash();
  
    public static Hash getInstance() {
      return _instance;
    }

    @Override
    public int hash(byte[] bytes, int length, int seed) {
        return (int)SpookyHash32.hash(bytes, 0, length, new long[]{(long)seed, (long)seed});
    }

    @Override
    public int hash(Object o) {
        if (o == null) {
            return 0;
        } else if (o instanceof String) {
            final byte[] bytes = ((String) o).getBytes();
            return hash(bytes);
        } else if (o instanceof byte[]) {
            final byte[] bytes = (byte[]) o;
            return hash(bytes);
        } else if (o instanceof long[]) {
            long seed = seedLong();
            return (int) SpookyHash32.hash((long[])o, new long[]{seed, seed});
        }

        return hash(o.toString());
    }

    @Override
    public long hash64(Object o) {
        if (o == null) {
            return 0l;
        } else if (o instanceof String) {
            final byte[] bytes = ((String) o).getBytes();
            return hash64(bytes);
        } else if (o instanceof byte[]) {
            final byte[] bytes = (byte[]) o;
            return hash64(bytes);
        } else if (o instanceof long[]) {
            return SpookyHash64.hash((long[])o, seedLong());
        }

        return hash64(o.toString());
    }

    @Override
    public long hash64(byte[] bytes, int length, int seed) {
        return SpookyHash64.hash(bytes, 0, length, seed);
    }
    
}
