package org.streaminer.stream.avg;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class VariableEWMA extends SimpleEWMA {
    /**
     * The multiplier factor by which the previous samples decay.
     */
    protected double decay;
    
    /**
     * The number of samples added to this instance.
     */
    protected int count;

    /**
     * 
     * @param age The age is related to the decay factor alpha by the formula 
     * given for the DECAY constant. It signifies the average age of the samples 
     * as time goes to infinity.
     */
    public VariableEWMA(double age) {
        decay = 2 / (age + 1);
    }

    @Override
    public void add(double value) {
        if (average < WARMUP_SAMPLES) {
            count++;
            average += value;
	} else if (average == WARMUP_SAMPLES) {
            average = average / WARMUP_SAMPLES;
            count++;
	} else {
            average = (value * decay) + (average * (1 - decay));
	}
    }

    @Override
    public void clear() {
        super.clear();
        count = 0;
    }

    @Override
    public double getAverage() {
        if (average <= WARMUP_SAMPLES) {
            return 0.0;
	}

	return average;
    }
}
