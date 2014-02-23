package org.streaminer.stream.sampler;

import java.util.Random;

/**
 * Also called Binomial sampling. For each sample, roll the dice and pass/fail.
 * Known as not very stable, and does not give a fixed number of samples.
 * There are tricks to avoid doing random each time, but this is not really important.
 */
public class BernoulliSampler implements ISampler {
    private final double percent;
    private final Random rnd;
    private Double nextRnd = null;

    public BernoulliSampler(double percent) {
        this.percent = percent / 100;
        rnd = new Random();
    }

    public BernoulliSampler(double percent, Random rnd) {
        this.percent = percent;
        this.rnd = rnd;
        stage();
    }

    public boolean next()  {
        boolean val = check();
        stage();
        return val;
    }

    private boolean check() {
        return nextRnd < percent;
    }

    private void stage() {
        nextRnd = rnd.nextDouble();
    }

    public void setSeed(long seed) {
        rnd.setSeed(seed);
    }
}