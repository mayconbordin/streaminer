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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mayconbordin
 */
public class EntropySketchTest {
    
    public EntropySketchTest() {
    }

    @Test
    public void testEntropy() {
        System.out.println("entropy");
        EntropySketch sketch = new EntropySketch(10);
        EntropyExact exact = new EntropyExact();
        
        String s = "hello";
        
        sketch.push(s.getBytes(), 1);
        exact.push(s.getBytes(), 1);
        
        System.out.println("Estimate: " + sketch.entropy());
        System.out.println("Exact: " + exact.entropy());
    }
    
}
