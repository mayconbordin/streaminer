package org.streaminer.stream.frequency.decay;

public interface DecayFormula {

    public Double evaluate(Double value, Double t);
}
