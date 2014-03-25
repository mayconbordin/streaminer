package org.streaminer.stream.frequency.decay;

public class LogDecayFormula implements DecayFormula {

    private Double lifeTime;

    public LogDecayFormula(long lifeTimeInSeconds) {
        lifeTime = Double.valueOf(lifeTimeInSeconds) * 1000;
    }

    public Double evaluate(Double value, Double t) {
        if (t < 0 || t >= lifeTime ) {
            return 0.0;
        } else {
            return value + 1 - Math.pow(Math.E, Math.log(value + 1)/lifeTime*t);
        }
    }

}
