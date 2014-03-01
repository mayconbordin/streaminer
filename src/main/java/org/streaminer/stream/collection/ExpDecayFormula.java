package org.streaminer.stream.collection;

public class ExpDecayFormula implements DecayFormula {

    private Double decayRate;

    public ExpDecayFormula(Number halfLifeInSeconds) {
        decayRate = - Math.log(2) / halfLifeInSeconds.longValue() / 1000;
    }

    public Double evaluate(Double quantity, Double t) {
        return quantity * Math.pow(Math.E, decayRate * t);
    }

}
