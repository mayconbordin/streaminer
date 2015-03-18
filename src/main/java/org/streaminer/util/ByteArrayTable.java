/*
 * Copyright Gradiant (http://www.gradiant.org) 2014
 * 
 * APACHE LICENSE v2.0
 * 
 * Author: Dr. Luis Rodero-Merino (lrodero@gradiant.org)
 */
package org.streaminer.util;

import java.util.Arrays;
/**
 * Table to store data in a byte array. A ByteBuffer could be used if we were sure that data items size will be
 * an integer number of bytes.
 * 
 * @author Luis Rodero-Merino
 *
 */
public class ByteArrayTable {

    private int bitsPerBucket;
    private int buckets;
    protected byte[] table = null;
    
    public int size() {
        return buckets;
    }
    
    public ByteArrayTable(int buckets, int bitsPerBucket) {
        
        if(buckets <= 0)
            throw new IllegalArgumentException("Cannot create a table with a non-positive number of buckets");
        if(bitsPerBucket <= 0)
            throw new IllegalArgumentException("Cannot create a table with a non-positive number of bits per bucket");
        
        this.bitsPerBucket = bitsPerBucket;
        this.buckets = buckets;
        int tableSize = (int) Math.ceil(bitsPerBucket * buckets / 8.0D); 
        table = new byte[tableSize];
        
    }
    
    public boolean isItemInPos(byte[] item, int itemPos) {

        if(item.length != bytesPerBucket())
            throw new IllegalArgumentException("A data item must be an array of size " + bytesPerBucket() + " in bytes, to store the " + bitsPerBucket + " bits per bucket");
        if(itemPos >= buckets)
            throw new IllegalArgumentException("Cannot get item from position " + itemPos + ", valid range is [0," + (buckets-1) + "]");
        if(itemPos < 0)
            throw new IllegalArgumentException("Cannot get item from a negative position " + itemPos + ", valid range is [0," + (buckets-1) + "]");
        
        byte[] data = get(itemPos);
        
        for(int i = 0; i < data.length; i++)
            if(data[i] != item[i])
                return false;
        
        return true;
    }
    
    public byte[] get(int itemPos) {

        if(itemPos >= buckets)
            throw new IllegalArgumentException("Cannot get item from position " + itemPos + ", valid range is [0," + (buckets-1) + "]");
        if(itemPos < 0)
            throw new IllegalArgumentException("Cannot get item from a negative position " + itemPos + ", valid range is [0," + (buckets-1) + "]");
        
        // Locating affected bytes in table
        int firstByteInd = itemPos * bitsPerBucket / 8;
        int lastByteInd  = ((itemPos + 1) * bitsPerBucket - 1) / 8;
        
        byte[] item = new byte[lastByteInd-firstByteInd+1];
        System.arraycopy(table, firstByteInd, item, 0, item.length);

        // Shifting to the left to align the item array with the bytes in the table, new positions are also filled with 0's.
        int firstBitInFirstByteInd = itemPos * bitsPerBucket % 8;
        item = ByteUtil.shitfRightAndFill(item, firstBitInFirstByteInd);
        
        // Removing leading byte if needed
        if(item.length == (bytesPerBucket()+1))
            item = Arrays.copyOfRange(item, 0, item.length-1);
        
        // Just a small check...
        if(item.length != bytesPerBucket())
            throw new InternalError("Created an item with a number of bytes " + item.length + " that differes from the size of buckets " + bytesPerBucket());
        
        // Removing leading bits that come from following item in table
        if(bitsPerBucket % 8 != 0) {
            byte mask = (byte)((0x01 << (bitsPerBucket % 8))-1);
            item[item.length-1] &= mask;
        }
        
        return item;
    }
    
    public void insert(byte[] item, int itemPos) {
        
        if(item.length != bytesPerBucket())
            throw new IllegalArgumentException("A data item must be an array of size " + bytesPerBucket() + " in bytes, to store the " + bitsPerBucket + " bits per bucket");
        if(itemPos >= buckets)
            throw new IllegalArgumentException("Cannot insert item at position " + itemPos + ", valid range is [0," + (buckets-1) + "]");
        if(itemPos < 0)
            throw new IllegalArgumentException("Cannot insert item at a negative position " + itemPos + ", valid range is [0," + (buckets-1) + "]");
        
        // Locating affected bytes in table
        int firstByteInd = itemPos * bitsPerBucket / 8;
        int lastByteInd  = ((itemPos + 1) * bitsPerBucket - 1) / 8;
         
        // We'll create a copy of the item array that will be combined with AND operation with the corresponding bytes in the table
        byte[] itemCp = new byte[lastByteInd-firstByteInd+1];
        System.arraycopy(item, 0, itemCp, 0, item.length);
        
        // Just one small check, the amount of affected bytes must be equal to the item size or be one byte greater (should never happen otherwise but anyway)
        if(item.length != itemCp.length && (item.length != (itemCp.length - 1)))
            throw new InternalError("Affected bytes are in positions [" + firstByteInd + "," + lastByteInd + "], " + (itemCp.length+1) + " bytes affected in total, but item is " + item.length + " bytes large");
        
        // Not all bits in the data item must be added to the table, only those that account for a bucket. The rest (which are the most significant ones, i.e. the ones at the left size),
        // will be all replaced by 0's (it will be handy later on).
        int lastByteMaskSize = item.length * 8 - bitsPerBucket; // E.g. we need three 0's in the mask
        byte lastByteMask = (byte)((0xff >> (lastByteMaskSize))); // Building the mask 00011111
        itemCp[item.length-1] = (byte)(itemCp[item.length-1] & lastByteMask); // Applying the mask in the last byte _coming from the original data item_
        if(itemCp.length > item.length) // If an extra byte had to be created, filling it with 0's as well
            itemCp[itemCp.length-1] = (byte)0x00; 
        
        // Now shifting to the left to align the item array with the bytes in the table, new positions are also filled with 0's.
        int firstBitInFirstByteInd = itemPos * bitsPerBucket % 8;
        itemCp = ByteUtil.shiftLeftAndFill(itemCp, firstBitInFirstByteInd);
        
        // Setting to 0's all bits in table that are going to be replaced (i.e. the bits of the corresponding bucket)
        delete(itemPos);
        
        // Finally, combining the affected bytes in the table with the item array
        for(int i = 0; i < itemCp.length; i++)
            itemCp[i] = (byte)(itemCp[i] | table[i+firstByteInd]);
        
        System.arraycopy(itemCp, 0, table, firstByteInd, itemCp.length);
        
    }
    
    public void delete(int itemPos) {

        if(itemPos >= buckets)
            throw new IllegalArgumentException("Cannot delete item in position " + itemPos + ", valid range is [0," + (buckets-1) + "]");
        if(itemPos < 0)
            throw new IllegalArgumentException("Cannot delete item in a negative position " + itemPos + ", valid range is [0," + (buckets-1) + "]");
        
        for(int i = itemPos*bitsPerBucket; i < (itemPos+1)*bitsPerBucket; i++)
            ByteUtil.insertZeroIn(table, i);
    }
    
    private int bytesPerBucket() {
        return (int) Math.ceil(bitsPerBucket / 8.0D);
    }
    
    public int getTableLength() {
        return table.length;
    }
    
    public byte[] getTable() {
        return table;
    }

}