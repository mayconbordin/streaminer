package org.streaminer.stream.membership;

import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streaminer.util.hash.MurmurHash3;

/**
 * Implementation of a Time-Decaying Bloom Filter.
 * 
 * Reference:
 *   Dautrich Jr, Jonathan L., and Chinya V. Ravishankar. "Inferential time-decaying
 *   Bloom filters." Proceedings of the 16th International Conference on Extending 
 *   Database Technology. ACM, 2013.
 * 
 * Source code: https://github.com/mynameisfiber/fuggetaboutit
 * 
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class TimingBloomFilter extends TimerTask {
    private static final Logger LOG = LoggerFactory.getLogger(TimingBloomFilter.class);
    private static final int ENTRIES_PER_8BYTE = 1;
    
    private int capacity;
    private int numBytes;
    private int numHashes;
    private int decayTime;
    private int ringSize;
    private int dN;
    private int numNonZero;
    
    private int[] data;
    
    private double error;
    private double secondsPerTick;
    
    private Timer timer;
    
    private MurmurHash3 hash;
    private int seed;

    public TimingBloomFilter(int capacity, int decayTime) {
        this(capacity, decayTime, 0.005);
    }
    
    public TimingBloomFilter(int capacity, int decayTime, double error) {
        this.capacity = capacity;
        this.decayTime = decayTime;
        this.error = error;
        
        initialize();
    }
    
    
    private void initialize() {
        hash = new MurmurHash3();
        seed = (int) System.nanoTime();
        timer = new Timer(this.getClass().getName() + "Timer");
        
        numNonZero = 0;
        
        numBytes = (int)(-capacity * Math.log(error) / Math.pow(Math.log(2), 2)) + 1;
        numHashes = (int)(numBytes / capacity * Math.log(2)) + 1;
                        
        data = new int[(int)Math.ceil(numBytes / ENTRIES_PER_8BYTE)];

        ringSize = (1 << (8 / ENTRIES_PER_8BYTE)) - 1;
        dN = ringSize / 2;
        secondsPerTick = decayTime / (double)dN;
    }
    
    public void startDecay() {
        long timePerDecay = (long) (secondsPerTick * 1000);
        timer.scheduleAtFixedRate(this, timePerDecay, timePerDecay);
    }
    
    public void decay() {
        run();
    }

    @Override
    public void run() {
        int[] tick = tickRange();
        
        numNonZero = 0;
        
        for (int i=0; i<numBytes; i++) {
            if (data[i] != 0)
                if (!testInterval(tick, data[i]))
                    data[i] = 0;
                else
                    numNonZero += 1;
        }
    }
    
    private int[] indexes(Object key) {
        long[] h = hash.hash64(key, seed);

        int[] indexes = new int[numHashes];
        for (int i = 0; i < numHashes; i++) {
            indexes[i] = (int) Math.abs((h[0] + i * h[1]) % numBytes);
        }
        
        return indexes;
    }
    
    private int tick(long timestamp) {
        return (int)((timestamp / secondsPerTick) % ringSize) + 1;
    }
    
    private long time() {
        return System.currentTimeMillis()/1000;
    }
    
    private int[] tickRange() {
        int tickMax = tick(time());
        int tickMin = (tickMax - dN - 1) % ringSize + 1;
        return new int[]{tickMin, tickMax};
    }
    
    public void add(Object key) {
        add(key, time());
    }
        
    public void add(Object key, long timestamp) {
        int tick = tick(timestamp);
        
        if (timestamp < time() - decayTime)
            return;
        
        for (int index : indexes(key)) {
            numNonZero += (data[index] == 0) ? 1 : 0;
            data[index] = tick;
        }
    }
    
    private boolean testInterval(int[] ticks, int data) {
        if (ticks[0] < ticks[1])
            return (ticks[0] < data && data <= ticks[1]);
        else
            return (data != 0 && !(ticks[1] < data && data <= ticks[0]));
    }
    
    public boolean membershipTest(Object key) {
        int[] ticks = tickRange();

        for (int index : indexes(key)) {
            if (!testInterval(ticks, data[index]))
                return false;
        }
        
        return true;
    }

    public double getError() {
        return error;
    }
    
    
}
