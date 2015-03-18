/*
 * Copyright Gradiant (http://www.gradiant.org) 2014
 * 
 * APACHE LICENSE v2.0
 * 
 * Author: Dr. Luis Rodero-Merino (lrodero@gradiant.org)
 */
package org.streaminer.util;

public class ByteUtil {
    
    public final static boolean isZero(byte[] array) {
        if(array == null)
            throw new IllegalArgumentException("Cannot check if a null array is full of zeros");
        for(byte b: array)
            if(b != 0)
                return false;
        return true;
    }

    /**
     * Shift byte array a certain number of positions to left (where the minus significant bit is at the right end), and fill
     * with 0's as the array is moved. Up to 7 bits (positions) can be shifted.
     * @param array
     * @param positions
     * @return
     */
    public final static byte[] shiftLeftAndFill(byte[] array, int positions) {
        if(array == null)
            throw new IllegalArgumentException("Cannot shift a null byte array");
        if(positions < 0)
            throw new IllegalArgumentException("Cannot shift a negative number of positions");
        if(positions >= 8)
            throw new IllegalArgumentException("Weird error, should not be asking for shifting more than 7 positions, but " + positions + " are asked for");
        byte[] result = new byte[array.length];
        byte mask = (byte) (((byte)0xff) << (8-positions));
        for(int i = array.length-1; i >=0; i--) { // Traversing array from left to right
            result[i] = (byte)(array[i] << positions);
            if(i==0) {
                break;
            }
            // 'Retrieving' bits from following byte at the right, so they are not lost
            byte fromFoll = (byte)(array[i-1] & mask);
            fromFoll = (byte) ((fromFoll&0xff) >>> (8-positions)); // The 0xff mask is to prevent the '>>>' operator to make appear some 1's...
            result[i] = (byte) (result[i] | fromFoll);
        }
        return result;
    }

    /**
     * Shift byte array a certain number of positions to right (where the minus significant bit is at the right end), and fill
     * with 0's as the array is moved. Up to 7 bits (positions) can be shifted.
     * @param array
     * @param positions
     * @return
     */
    public final static byte[] shitfRightAndFill(byte[] array, int positions) {
        if(array == null)
            throw new IllegalArgumentException("Cannot shift a null byte array");
        if(positions < 0)
            throw new IllegalArgumentException("Cannot shift a negative number of positions");
        if(positions >= 8)
            throw new IllegalArgumentException("Weird error, should not be asking for shifting more than 7 positions, but " + positions + " are asked for");
        byte[] result = new byte[array.length];
        byte mask = (byte)((0x01 << positions)-1);
        for(int i = 0; i <= array.length - 1; i++) { // Traversing array from right to left
            result[i] = (byte)((array[i]&0xff) >>> positions); 
            if(i < array.length - 1) // Getting bits from following byte at the left
                result[i] = (byte)(result[i] | (byte) (((byte)(array[i+1] & mask)) << (8-positions)));
        }
        return result;
    }
    
    public final static void insertZeroIn(byte[] array, int bitPos) {
        if(array == null)
            throw new IllegalArgumentException("Cannot insert zeros in a null array");
        if(bitPos < 0)
            throw new IllegalArgumentException("Cannot insert zero in a negative position (byte array index)");
        if(bitPos >= array.length * 8)
            throw new IllegalArgumentException("Cannot insert zero in position (index) " + bitPos + ", byte array length is " + array.length*8 + " in bits");
        int bytePos = bitPos / 8;
        int posInByte = bitPos % 8;
        byte mask = (byte) ~(byte)(0x01 << posInByte);
        array[bytePos] = (byte)(array[bytePos] & mask);
    }
    
    public final static String readableByteArray(byte[] array) {
        if(array == null)
            return "[NULL]";
        String result = "[";
        for(int i = array.length-1; i >=0; i--) {
            result += readableByte(array[i]);
            if(i > 0)
                result += ("|");
        }
        result += "]";
        return result;
    }
    
    public final static String readableByteArrayI(int val) { // Most significative first (at the left size)
        String result = "[";
        int mask = 0xff;
        for(int i = 0; i < 4; i++) {
            long masked = (val & (mask << ((3 - i))*8));
            masked = (masked >>> ((3-i))*8);
            result += readableByte((byte)(masked&0xff));
            if(i < 3)
                result += "|";
        }
        result += "]";
        return result;        
    }
    
    public final static String readableByteArrayL(long val) { // Most significative first (at the left size)
        String result = "[";
        long mask = 0xffL;
        for(int i = 0; i < 8; i++) {
            long masked = (val & (mask << ((7 - i))*8));
            masked = (masked >>> ((7-i))*8);
            result += readableByte((byte)masked);
            if(i < 7)
                result += "|";
        }
        result += "]";
        return result;        
    }
    
    public final static String readableByte(byte b) {
        String a = Integer.toBinaryString(256 + (int) b);
        return (a.substring(a.length() - 8));
    }
}