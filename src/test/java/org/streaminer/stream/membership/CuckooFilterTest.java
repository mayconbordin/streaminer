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

package org.streaminer.stream.membership;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import org.streaminer.util.ByteUtil;

/**
 *
 * @author mayconbordin
 */
public class CuckooFilterTest {
    
    public CuckooFilterTest() {
    }

    /**
     * Test of contains method, of class CuckooFilter.
     */
    @Test
    public void testAll() {
        for(int i=0; i < 10000; i++)
            if(!testFilter())
                fail("Test failed");
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        for(int i=0; i < 10000; i++)
            if(!testFilter())
                break;
    }
    
    private static boolean testFilter() {
        CuckooFilter filter = new CuckooFilter(16, 1000);
        System.out.println("\n===============================");
        System.out.println("Table size: " + filter.table.size());

        System.out.println("RANDOM INSERTIONS");
        Random random = new Random();
        Set<Integer> bag = new HashSet<Integer>();
        for(int i = 0; i < 1000; i++) {
            Integer o = new Integer(i);
            boolean insert = random.nextBoolean();
            if(insert) {
                if(filter.addWithConfirmation(o))
                    bag.add(o);
                else {
                    System.out.println("ERROR COULD NOT ADD " + o);
                    return false;
                }
            }
        }
        if(filter.isFull())
            System.out.println("FILTER IS FULL");
        else
            System.out.println("FILTER IS NOT FULL");
        
        System.out.println("CHECKING CONTENTS AFTER RANDOM INSERTIONS (BAG SIZE IS " + bag.size() + ")");
        for(Integer i: bag)
            if(!filter.membershipTest(i)) {
                System.out.println("ERROR!");
                return false;
            }

        byte[] tableCpBfDel = Arrays.copyOf(filter.table.getTable(), filter.table.getTableLength());
        
        System.out.println("RANDOM DELETIONS");
        Iterator<Integer> iter = bag.iterator();
        while(iter.hasNext()) {
            Integer i = iter.next();
            boolean remove = random.nextBoolean();
            if(remove) {
                filter.delete(i);
                iter.remove();
            }
        }
        
        System.out.println("CHECKING CONTENTS AFTER RANDOM DELETIONS (BAG SIZE IS " + bag.size() + ")");
        for(Integer i: bag)
            if(!filter.membershipTest(i)) {
                System.out.println("ERROR, FILTER DOES NOT CONTAIN " + i);
                CuckooFilter.ItemInfo info = filter.itemInfoObj(i);
                System.out.println(info);
                System.out.println("filter["+info.index+"]:"+ByteUtil.readableByteArray(filter.table.get(info.index)) + "; filter["+info.index2+"]:"+ByteUtil.readableByteArray(filter.table.get(info.index2)));
                System.out.println("filterBfDe["+info.index+"]:"+ByteUtil.readableByteArray(new byte[]{tableCpBfDel[info.index]}) + "; filterBfDe["+info.index2+"]:"+ByteUtil.readableByteArray(new byte[]{tableCpBfDel[info.index2]}));
                return false;
            }
        
        System.out.println("EVERYTHING FINE!");
        return true;
    }
    
}
