/*
 * The MIT License
 *
 * Copyright 2014 Maycon Viana Bordin <mayconbordin@gmail.com>.
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

package org.streaminer.stream.membership;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.streaminer.util.hash.Hash;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class BloomFilterTest {
    public BloomFilter bf;
    static final int ELEMENTS = 10000;
    
    public BloomFilterTest() {
        bf = new BloomFilter(ELEMENTS, 32, Hash.MURMUR_HASH);
        assertNotNull(bf);
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of add method, of class BloomFilter.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        
        for (int i = 0; i < 100; i++) {
            String val = UUID.randomUUID().toString();
            Key k = new Key(val.getBytes());
            bf.add(k);
            assertTrue(bf.membershipTest(k));
        }
    }
}
