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
 * A SimpleEWMA represents the exponentially weighted moving average of a
 * series of numbers. It WILL have different behavior than the VariableEWMA
 * for multiple reasons. It has no warm-up period and it uses a constant
 * decay.  These properties let it use less memory.  It will also behave
 * differently when it's equal to zero, which is assumed to mean
 * uninitialized, so if a value is likely to actually become zero over time,
 * then any non-zero value will cause a sharp jump instead of a small change.
 * However, note that this takes a long time, and the value may just
 * decays to a stable value that's close to zero, but which won't be mistaken
 * for uninitialized. See http://play.golang.org/p/litxBDr_RC for example.
 * 
 * Original code: <https://github.com/VividCortex/ewma>.
 * 
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class SimpleEWMA implements IAverage {
    /**
     * By default, we average over a one-minute period, which means the average
     * age of the metrics in the period is 30 seconds.
     */
    protected static final double AVG_METRIC_AGE = 30.0; 
	
    /**
     * The formula for computing the decay factor from the average age comes
     * from "Production and Operations Analysis" by Steven Nahmias.
     */
    protected static final double DECAY = 2 / (AVG_METRIC_AGE + 1);
    
    /**
     * For best results, the moving average should not be initialized to the 
     * samples it sees immediately. The book "Production and Operations 
     * Analysis" by Steven Nahmias suggests initializing the moving average to 
     * the mean of the first 10 samples. Until the VariableEwma has seen this 
     * many samples, it is not "ready" to be queried for the value of the
     * moving average. This adds some memory cost.
     */
    protected static final int WARMUP_SAMPLES = 10;
    
    /**
     * The current value of the average. After adding with add(), this is
     * updated to reflect the average of all values seen thus far.
     */
    protected double average;

    /**
     * Add adds a value to the series and updates the moving average.
     * @param value The value to be added
     */
    public void add(double value) {
        if (average == 0) {
            average = value;
	} else {
            average = (value * DECAY) + (average * (1 - DECAY));
	}
    }

    public double getAverage() {
        return average;
    }

    public void clear() {
        average = 0;
    }
}
