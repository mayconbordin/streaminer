/**
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

import org.streaminer.stream.frequency.util.Counter;
import java.io.ByteArrayInputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.streaminer.util.DoublyLinkedList;
import org.streaminer.util.ExternalizableUtil;
import org.streaminer.util.ListNode2;
import org.streaminer.util.Pair;
import java.util.Collections;
import org.streaminer.stream.frequency.util.CountEntry;

/**
 * Based on the <i>Space-Saving</i> algorithm and the <i>Stream-Summary</i>
 * data structure as described in:
 * <i>Efficient Computation of Frequent and Top-k Elements in Data Streams</i>
 * by Metwally, Agrawal, and Abbadi
 *
 * @param <T> type of data in the stream to be summarized
 */
public class StreamSummary<T> implements ITopK<T> {
    protected int capacity;
    private HashMap<T, ListNode2<Counter<T>>> counterMap;
    protected DoublyLinkedList<Bucket> bucketList;
    
    /**
     * For de-serialization
     */
    public StreamSummary() {
    }

    /**
     * @param capacity maximum size (larger capacities improve accuracy)
     */
    public StreamSummary(int capacity) {
        this.capacity = capacity;
        counterMap = new HashMap<T, ListNode2<Counter<T>>>();
        bucketList = new DoublyLinkedList<Bucket>();
    }

    public int getCapacity() {
        return capacity;
    }

    /**
     * Algorithm: <i>Space-Saving</i>
     *
     * @param item stream element (<i>e</i>)
     * @return false if item was already in the stream summary, true otherwise
     */
    @Override
    public boolean add(T item) {
        return add(item, 1);
    }

    /**
     * Algorithm: <i>Space-Saving</i>
     *
     * @param element
     * @return false if item was already in the stream summary, true otherwise
     */
    @Override
    public boolean add(T element, long incrementCount) {
        return offerReturnAll(element, incrementCount).left;
    }

    /**
     * @param item stream element (<i>e</i>)
     * @param incrementCount
     * @return item dropped from summary if an item was dropped, null otherwise
     */
    public T offerReturnDropped(T item, int incrementCount) {
        return offerReturnAll(item, incrementCount).right;
    }

    /**
     * @param item stream element (<i>e</i>)
     * @param incrementCount
     * @return Pair<isNewItem, itemDropped> where isNewItem is the return value of offer() and itemDropped is null if no item was dropped
     */
    public Pair<Boolean, T> offerReturnAll(T item, long incrementCount) {
        ListNode2<Counter<T>> counterNode = counterMap.get(item);
        boolean isNewItem = (counterNode == null);
        T droppedItem = null;
        if (isNewItem) {
            if (size() < capacity) {
                counterNode = bucketList.enqueue(new Bucket(0)).getValue().counterList.add(new Counter<T>(bucketList.tail(), item));
            } else {
                Bucket min = bucketList.first();
                counterNode = min.counterList.tail();
                Counter<T> counter = counterNode.getValue();
                droppedItem = counter.getItem();
                counterMap.remove(droppedItem);
                counter.setItem(item);
                counter.setError(min.count);
            }
            counterMap.put(item, counterNode);
        }

        incrementCounter(counterNode, incrementCount);

        return new Pair<Boolean, T>(isNewItem, droppedItem);
    }

    protected void incrementCounter(ListNode2<Counter<T>> counterNode, long incrementCount) {
        Counter<T> counter = counterNode.getValue();       // count_i
        ListNode2<Bucket> oldNode = counter.getBucketNode();
        Bucket bucket = oldNode.getValue();         // Let Bucket_i be the bucket of count_i
        bucket.counterList.remove(counterNode);            // Detach count_i from Bucket_i's child-list
        counter.incrementCount(incrementCount);

        // Finding the right bucket for count_i
        // Because we allow a single call to increment count more than once, this may not be the adjacent bucket. 
        ListNode2<Bucket> bucketNodePrev = oldNode;
        ListNode2<Bucket> bucketNodeNext = bucketNodePrev.getNext();
        while (bucketNodeNext != null) {
            Bucket bucketNext = bucketNodeNext.getValue(); // Let Bucket_i^+ be Bucket_i's neighbor of larger value
            if (counter.getCount() == bucketNext.count) {
                bucketNext.counterList.add(counterNode);    // Attach count_i to Bucket_i^+'s child-list
                break;
            } else if (counter.getCount() > bucketNext.count) {
                bucketNodePrev = bucketNodeNext;
                bucketNodeNext = bucketNodePrev.getNext();  // Continue hunting for an appropriate bucket
            } else {
                // A new bucket has to be created
                bucketNodeNext = null;
            }
        }

        if (bucketNodeNext == null) {
            Bucket bucketNext = new Bucket(counter.getCount());
            bucketNext.counterList.add(counterNode);
            bucketNodeNext = bucketList.addAfter(bucketNodePrev, bucketNext);
        }
        counter.setBucketNode(bucketNodeNext);

        //Cleaning up
        if (bucket.counterList.isEmpty())           // If Bucket_i's child-list is empty
        {
            bucketList.remove(oldNode);         // Detach Bucket_i from the Stream-Summary
        }
    }

    @Override
    public List<CountEntry<T>> peek(int k) {
        List<CountEntry<T>> list = new ArrayList<CountEntry<T>>(k);

        for (ListNode2<Bucket> bNode = bucketList.head(); bNode != null; bNode = bNode.getPrev()) {
            Bucket b = bNode.getValue();
            for (Counter<T> c : b.counterList) {
                if (list.size() == k) {
                    Collections.sort(list);
                    return list;
                }
                list.add(new CountEntry<T>(c.getItem(), c.getCount()));
            }
        }

        Collections.sort(list);
        return list;
    }

    public List<Counter<T>> topK(int k) {
        List<Counter<T>> topK = new ArrayList<Counter<T>>(k);

        for (ListNode2<Bucket> bNode = bucketList.head(); bNode != null; bNode = bNode.getPrev()) {
            Bucket b = bNode.getValue();
            for (Counter<T> c : b.counterList) {
                if (topK.size() == k) {
                    return topK;
                }
                topK.add(c);
            }
        }

        return topK;
    }

    /**
     * @return number of items stored
     */
    public long size() {
        return counterMap.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (ListNode2<Bucket> bNode = bucketList.head(); bNode != null; bNode = bNode.getPrev()) {
            Bucket b = bNode.getValue();
            sb.append('{');
            sb.append(b.count);
            sb.append(":[");
            for (Counter<T> c : b.counterList) {
                sb.append('{');
                sb.append(c.getItem());
                sb.append(':');
                sb.append(c.getError());
                sb.append("},");
            }
            if (b.counterList.size() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("]},");
        }
        if (bucketList.size() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(']');
        return sb.toString();
    }
    
    public class Bucket {
        protected DoublyLinkedList<Counter<T>> counterList;
        private long count;

        public Bucket(long count) {
            this.count = count;
            this.counterList = new DoublyLinkedList<Counter<T>>();
        }
    }
}
