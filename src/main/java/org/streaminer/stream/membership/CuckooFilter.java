/*
 * Copyright Gradiant (http://www.gradiant.org) 2014
 * 
 * APACHE LICENSE v2.0
 * 
 * Author: Dr. Luis Rodero-Merino (lrodero@gradiant.org)
 */
package org.streaminer.stream.membership;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import org.streaminer.util.ByteArrayTable;
import org.streaminer.util.ByteUtil;

/**
 * Cuckoo filter implementation. A cuckoo filter is a data structure for membership
 * control, akin to bloom filters. But unlike bloom filters, deletions are allowed.
 * 
 * False positives are possible, false negatives are not (as long as there is
 * not collision... if two objects have the same fingerprint and are assigned
 * the same positions then conflicts can arise).
 * @author lrodero
 *
 */
public class CuckooFilter implements IFilter<Object> {
   
    private static final int MAX_TRIES_WHEN_ADDING = 500;
    
    private MessageDigest sha1 = null;
    private int fingerprintSize = 0;
    private byte fingerprintLastByteMask = (byte)0xff;
    protected ByteArrayTable table = null;
    private ItemInfo lastVictim = null;

    /**
     * 
     * @param fingerprintSize Number of bits for each fingerprint (value that represents an item)
     * @param maxItems Max amount of items we expect in the filter. In fact the underlying array size will be greater than this.
     */
    public CuckooFilter(int fingerprintSize, int maxItems) {

        if(fingerprintSize <= 0)
            throw new IllegalArgumentException("Fingerprint size must be a positive number, received " + fingerprintSize);
        if(fingerprintSize > 16 * 8)
            throw new IllegalArgumentException("Fingerprint size cannot be greater than " + 16 * 8  +" , received " + fingerprintSize);
        
        this.fingerprintSize = fingerprintSize;
        if(fingerprintSize % 8 != 0) { // Must add some leading 0's in the most significant byte of the mask
            int zeros = 8 - (fingerprintSize % 8);
            fingerprintLastByteMask = (byte)((0x01 << zeros) - 1);
        }
        
        try {
            sha1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("All Java implementations should carry an implementation of SHA1, however it cannot be found!");
        }
        
        // Table size must be a power of 2 and greater than the max number of items
        int tableSize = 1;
        while(tableSize < maxItems)
            tableSize <<= 1;
        // If there is not enough 'space to spare', we increase the table size
        if(maxItems*1.0D/tableSize > 0.96)
            tableSize <<= 1;
        
        table = new ByteArrayTable(tableSize, fingerprintSize);
    }
    
    /**
     * It will return {@code true} if the signature of the given object is found in the filter. But remember that false positives are possible!
     * @param o
     * @return
     */
    public boolean membershipTest(Object o) {
        ItemInfo info = itemInfoObj(o);
        
        if(lastVictim != null)
            if(Arrays.equals(info.fingerprint, lastVictim.fingerprint))
                return true;
        
        if(Arrays.equals(info.fingerprint, table.get(info.index)))
            return true;
        if(Arrays.equals(info.fingerprint, table.get(info.index2)))
            return true;
        
        return false;
    }
    
    public boolean isFull() {
        return lastVictim != null;
    }
    
    /**
     * It will return {@code true} if the given object {@code o} has been included, so future calls to {@link #contains(o)} will return {@code true}.
     * This method returns {@code false} if the filter is too full.
     * @param o
     * @return
     */
    public boolean addWithConfirmation(Object o) {
        if(o == null)
            throw new IllegalArgumentException("Cannot add a null object");
        return addItem(itemInfoObj(o));
    }
    
    public void add(Object o) {
        if(o == null)
            throw new IllegalArgumentException("Cannot add a null object");
        addItem(itemInfoObj(o));
    }
        
    private boolean addItem(ItemInfo info) {

        // Is already there? If so, we return true
        if(Arrays.equals(info.fingerprint, table.get(info.index)))
            return true;
        if(Arrays.equals(info.fingerprint, table.get(info.index2)))
            return true;
        
        if(lastVictim != null) // Table is already too full
            return false;
        
        if(ByteUtil.isZero(table.get(info.index))) {
            table.insert(info.fingerprint, info.index);
            return true;
        }
        
        int destination = info.index2;
        byte[] fingerprint = info.fingerprint;
        int tries = 0;
        while(++tries <= MAX_TRIES_WHEN_ADDING) {
            byte[] oldFingerprint = table.get(destination);
            table.insert(fingerprint, destination);
            if(ByteUtil.isZero(oldFingerprint))
                return true;
            fingerprint = oldFingerprint;
            destination = altIndex(fingerprint, destination);
        }
        
        lastVictim = new ItemInfo();
        lastVictim.fingerprint = fingerprint;
        lastVictim.index = destination;
        lastVictim.index2 = altIndex(fingerprint, destination);

        return true;
    }
    
    /**
     * It will return {@code true} if the element signature was found, {@code false} otherwise. In any case 
     * the signature will be removed if found.
     * @param o
     * @return
     */
    public boolean delete(Object o) {

        if(o == null)
            throw new IllegalArgumentException("Cannot remove a null object");
        
        ItemInfo info = itemInfoObj(o);
        
        
        if(ByteUtil.isZero(table.get(info.index)) && ByteUtil.isZero(table.get(info.index2)))
            return false;
        
        boolean deleted = false;
        if(Arrays.equals(info.fingerprint, table.get(info.index))) {
            table.delete(info.index);
            deleted = true;
        } else if(Arrays.equals(info.fingerprint, table.get(info.index2))) {
            table.delete(info.index2);
            deleted = true;
        }
        
        if(deleted) // There is room again for the victim (if there is any), let's try to insert it 
            if(lastVictim != null) {
                ItemInfo infoVic = new ItemInfo();
                infoVic.fingerprint = Arrays.copyOf(lastVictim.fingerprint, lastVictim.fingerprint.length);
                infoVic.index = lastVictim.index;
                infoVic.index2 = lastVictim.index2;
                lastVictim = null;
                addItem(infoVic);
            }
        
        return deleted;
        
    }
    
    protected class ItemInfo {
        int index = -1;
        int index2 = -1;
        byte[] fingerprint = null;
        @Override
        public String toString() {
            return "i1: " + index + ", i2: " + index2 + ", fingerprint: " + ByteUtil.readableByteArray(fingerprint);
        }
    }
    
    protected ItemInfo itemInfoObj(Object o) {
        int h = o.hashCode();
        byte[] b = new byte[4];
        for(int i=0; i < b.length; i++) {
            b[i] = (byte)(h & 0xff);
            h >>= 8;
        }
        return itemInfo(b);
    }
    
    protected ItemInfo itemInfo(byte[] item) {
        ItemInfo info = new ItemInfo();
        byte[] hash = sha1.digest(item);
        
        // First index
        long val = 0;
        for(int i=0; i < 4; i++) {
            val |= (hash[i] & 0xff); // The '& 0xff' op is because the hash[i] byte is transformed to int by java keeping the sign!, this way we get rid of leading 1's if present
            if(i<3)
                val <<= 8;
        }
        val &= 0x00000000ffffffffL;
        info.index = (int) (val % (long)table.size());
        
        // Fingerprint
        info.fingerprint = new byte[fingerprintSizeInBytes()];
        for(int i=0; i < info.fingerprint.length; i++)
            info.fingerprint[i] = hash[i+4];
        info.fingerprint[info.fingerprint.length-1] &= fingerprintLastByteMask;
        if(ByteUtil.isZero(info.fingerprint)) // Avoiding fingerprints with all zeros (they would be confused with 'no fingerprint' in the table)
            info.fingerprint[0] = 1;
        
        // Second index
        info.index2 = altIndex(info.fingerprint, info.index);
        
        // Just a small check
        if(altIndex(info.fingerprint, info.index2) != info.index)
            throw new InternalError("Generated wrong indexes!");
        
        return info;
    }
    
    private int altIndex(byte[] fingerprint, int index) {
        byte[] hash = sha1.digest(fingerprint);
        long val = 0;
        for(int i=0; i < 4; i++) {
            long mask = 0xffL;
            mask <<= (i*8);
            byte b = (byte)((mask & (long)index) >> (i*8));
            val |= (((hash[i] ^ b)&0xff) << (i*8));
        }
        val &= 0x00000000ffffffffL;
        return (int) (val % (long)table.size());
    }
    
    private int fingerprintSizeInBytes() {
        return (int)Math.ceil(fingerprintSize/8.0D);
    }
}