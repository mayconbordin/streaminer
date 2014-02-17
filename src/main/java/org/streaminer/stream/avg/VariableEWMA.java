/*
 * The MIT License
 *
 * Copyright (c) 2013 VividCortex
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.streaminer.stream.avg;

/**
 * VariableEWMA represents the exponentially weighted moving average of a series of
 * numbers. Unlike SimpleEWMA, it supports a custom age, and thus uses more memory.
 * 
 * Original code: https://github.com/VividCortex/ewma
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
     * @param age The age is related to the decay factor alpha by the formula 
     * given for the DECAY constant. It signifies the average age of the samples 
     * as time goes to infinity.
     */
    public VariableEWMA(double age) {
        decay = 2 / (age + 1);
    }

    /**
     * Add adds a value to the series and updates the moving average.
     * @param value The value to be added
     */
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

    /**
     * @return The current value of the average, or 0.0 if the series hasn't warmed up yet.
     */
    @Override
    public double getAverage() {
        if (average <= WARMUP_SAMPLES) {
            return 0.0;
	}

	return average;
    }
}
