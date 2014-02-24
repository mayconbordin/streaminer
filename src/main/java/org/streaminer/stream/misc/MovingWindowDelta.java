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

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Delta over the most recent k sample periods.
 *
 * If you use this class with a counter, you can get the cumulation of counts in a sliding window.
 *
 * One sample period is the time in between doSample() calls.
 *
 * @author Feng Zhuge
 */
public class MovingWindowDelta<T extends Number> {
    private static final int DEFAULT_WINDOW_SIZE = 60;
    private final LinkedBlockingDeque<Long> deltaSeries;
    private long sumDelta = 0l;
    private long lastInput = 0l;
    
    public MovingWindowDelta() {
        this(DEFAULT_WINDOW_SIZE);
    }
    
    public MovingWindowDelta(int windowSize) {
        if (windowSize < 1)
            throw new IllegalArgumentException("windowSize should be greater than zero");
        
        deltaSeries = new LinkedBlockingDeque<Long>(windowSize);
    }
    
    public void add(T num) {
        long lastDelta = 0l;
        if (deltaSeries.remainingCapacity() == 0) {
          lastDelta = deltaSeries.removeFirst();
        }

        long newInput = num.longValue();
        long newDelta = newInput - lastInput;
        lastInput = newInput;

        deltaSeries.addLast(newDelta);

        sumDelta += newDelta - lastDelta;
    }
    
    public long getDelta() {
        return sumDelta;
    }
}
