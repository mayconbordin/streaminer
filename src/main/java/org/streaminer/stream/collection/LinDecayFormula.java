package org.streaminer.stream.collection;

public class LinDecayFormula implements DecayFormula {

    private Double lifeTime;

    public LinDecayFormula(Number lifeTimeInSeconds) {
        lifeTime = Double.valueOf(lifeTimeInSeconds.longValue()) * 1000;
    }

    public Double evaluate(Double value, Double t) {
        if (t < 0 || t > lifeTime ) {
            return -0.1;
        } else {
            return value * (1 - t / lifeTime);
        }
    }

}
