/*
 * The MIT License
 *
 * Copyright 2015 mayconbordin.
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

package org.streaminer.stream.entropy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mayconbordin
 */
public class EntropyExact {
    private Map<String, Double> data;

    public EntropyExact() {
        data = new HashMap<String, Double>();
    }
    
    public void push(byte[] b, int inc) {
        data.put(Arrays.toString(b), (double)inc);
    }
    
    public double entropy() {
        int hm = 0;
        double size = 0.0;
        
        for (double c : data.values()) {
            size += c;
            hm += c * (Math.log(c) / Math.log(2));
        }
        
        return (Math.log(size) / Math.log(2)) - hm/size;
    }
}
