// =================================================================================================
// Copyright 2011 Twitter, Inc.
// -------------------------------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this work except in compliance with the License.
// You may obtain a copy of the License in the LICENSE file, or at:
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// =================================================================================================

package org.streaminer.stream.misc;

import com.google.common.base.Ticker;
import java.util.concurrent.LinkedBlockingDeque;
import org.streaminer.util.Pair;

/**
 * Function to compute a windowed per-second rate of a value.
 *
 * @author William Farner
 * @param <T>
 */
public class Rate<T extends Number> {
    private static final int DEFAULT_WINDOW_SIZE = 1;
    private static final double DEFAULT_SCALE_FACTOR = 1;
    private static final long NANOS_PER_SEC = 1000000000;

    private final Ticker ticker;
    private final double scaleFactor;
        
    private final LinkedBlockingDeque<Pair<Long, Double>> samples;
    
    public Rate(int windowSize, double scaleFactor, Ticker ticker) {
        if (scaleFactor == 0)
            throw new IllegalArgumentException("Scale factor must be non-zero!");
        
        this.ticker = ticker;
        this.scaleFactor = scaleFactor;
        samples = new LinkedBlockingDeque<Pair<Long, Double>>(windowSize);
    }
    
    public double add(T newSample) {
        long newTimestamp = ticker.read();

        double rate = 0;
        if (!samples.isEmpty()) {
          Pair<Long, Double> oldestSample = samples.peekLast();

          double dy = newSample.doubleValue() - oldestSample.getSecond();
          double dt = newTimestamp - oldestSample.getFirst();
          rate = dt == 0 ? 0 : (NANOS_PER_SEC * scaleFactor * dy) / dt;
        }

        if (samples.remainingCapacity() == 0) samples.removeLast();
        samples.addFirst(new Pair(newTimestamp, newSample.doubleValue()));
    
        return rate;
    }
}
