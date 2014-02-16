/*
 * Copyright (C) 2011 Clearspring Technologies, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.streaminer.stream.frequency.topk;

import java.util.List;
import org.streaminer.stream.frequency.IBaseFrequency;
import org.streaminer.stream.frequency.util.CountEntry;

public interface ITopK<T> extends IBaseFrequency<T> {
    /**
     * Get the k most frequent elements.
     * @param k The maximum number of elements to be returned
     * @return A list of the most frequent items, ordered in descending order of frequency.
     */
    public List<CountEntry<T>> peek(int k);
}
