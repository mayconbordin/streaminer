/*
 * Copyright (c)  2010 Ghais Issa and others. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.streaminer.stream.sampling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.streaminer.stream.sampling.gamma.GammaFunction;

/**
 * Default reservoir implementation.
 * @author Ghais Issa
 * @param <T>
 */
public class ReservoirSampler<T> implements ISampler<T> {
    private static final Random RANDOM = new Random();

    private long skipCount;
    private int currentCount;
    private int size;
    private GammaFunction skipFunction;
    private List<T> items;

    public ReservoirSampler(int size, GammaFunction skipFunction) {
        items = new ArrayList<T>(size);
        this.size = size;
        this.currentCount = 0;
        this.skipCount = 0;
        this.skipFunction = skipFunction;
    }
    
    public void put(T t) {
        if (size != items.size()) {
            items.add(t);
        } else {
            if (skipCount > 0) {
                skipCount--;
            } else {
                items.set(RANDOM.nextInt(size), t);
                skipCount = skipFunction.apply(currentCount);
            }
        }
        
        currentCount++;
    }

    public void put(T... t) {
        for (T item : t) {
            put(item);
        }
    }

    public Collection<T> get() {
        return Collections.unmodifiableCollection(items);
    }

    public int getSize() {
        return size;
    }
    
}
