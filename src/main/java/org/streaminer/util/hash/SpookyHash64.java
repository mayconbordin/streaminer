/*
 * Copyright 2013 David Curtis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.streaminer.util.hash;

/**
 * A variation of {@link SpookyHash} for use in applications that require only 
 * 64-bit hash code values. When compared to {@code SpookyHash}, the following 
 * differences are noted:
 * <ul>
 * <li>The computation is identical to 128-bit SpookyHash; only the method 
 * signatures and the use of the seed value are changed.
 * <li>The seed value for SpookyHash64 is a single long value, which is 
 * duplicated to populate the internal 128-seed value.
 * <li>The 64-bit {@code long} hash code is always returned as the function 
 * value.
 * </ul>
 *
 * @author David Curtis
 * @see SpookyHash
 */
public class SpookyHash64 {

	/* Bit mask for packing bytes. */
	private static final long BYTE_MASK = 0x0FFL;
	
	/* The size of the hash engine internal state, in long words. */
	private static final int NUM_STATE_VARS = 12;
	
	/* A block is the basic unit of data ingestion. */
	private static final int BLOCK_SIZE_BYTES = NUM_STATE_VARS * 8;
	private static final int BLOCK_SIZE_CHARS = NUM_STATE_VARS * 4;
	private static final int BLOCK_SIZE_LONGS = NUM_STATE_VARS;
	
	/* Input arrays of size less than or equal to SMALLHASH_LIMIT_BYTES are 
	 * computed with a different algorithm to avoid the startup cost of the 
	 * full algorithm.
	 */
	private static final int SMALLHASH_LIMIT_BYTES = BLOCK_SIZE_BYTES * 2;
	private static final int SMALLHASH_LIMIT_CHARS = BLOCK_SIZE_CHARS * 2;
	private static final int SMALLHASH_LIMIT_LONGS = BLOCK_SIZE_LONGS * 2;
	/*
	 * From comments in the original C++ version --
	 * "A constant which:
	 *		is not zero
	 *		is odd
	 *		is a not-very-regular mix of 1's and 0's
	 *		does not need any other special mathematical properties"
	 */
	private static final long ARBITRARY_BITS = 0xDEADBEEFDEADBEEFL;

	/*
	 * Default seed value used by non-static hash methods
	 */
	private final long seed;
	
	/**
	 * Packs eight bytes into one long value, in little-endian
	 * order.
	 * 
	 * @param src contains bytes to pack
	 * @param start index of first byte in {@code src} to pack
	 * @return long result containing packed bytes
	 */
	private static long packLong(byte[] src, int start) {
		return (((long)src[start + 7]) & BYTE_MASK) << 56 
				| (((long)src[start + 6]) & BYTE_MASK) << 48 
				| (((long)src[start + 5]) & BYTE_MASK) << 40 
				| (((long)src[start + 4]) & BYTE_MASK) << 32
				| (((long)src[start + 3]) & BYTE_MASK) << 24 
				| (((long)src[start + 2]) & BYTE_MASK) << 16 
				| (((long)src[start + 1]) & BYTE_MASK) << 8  
				| (((long)src[start]) & BYTE_MASK);	
	}

	/**
	 * Packs seven or fewer bytes into one long value, in little-endian 
	 * order.
	 * 
	 * @param src contains bytes to pack
	 * @param start index of first byte in {@code src} to pack
	 * @param length the number of bytes to pack; must be less than 8
	 * and greater than zero.
	 * @return long result containing packed bytes
	 */
	private static long packPartial(byte[] src, int start, int length) {
		long h = 0;
		/**
		 * The case blocks fall through intentionally.
		 */
		switch (length) {
		case 7:			
			h += (((long)src[start + 6]) & BYTE_MASK) << 48;
		case 6:
			h += (((long)src[start + 5]) & BYTE_MASK) << 40;
		case 5:
			h += (((long)src[start + 4]) & BYTE_MASK) << 32;
		case 4:
			h += (((long)src[start + 3]) & BYTE_MASK) << 24;
		case 3:
			h += (((long)src[start + 2]) & BYTE_MASK) << 16;
		case 2:
			h += (((long)src[start + 1]) & BYTE_MASK) << 8;
		case 1:
			h += (((long)src[start]) & BYTE_MASK);
		}
		return h;
	}

	/**
	 * Computes the hash code for a small array of bytes (less than 192 bytes).
	 * This method is called automatically when applicable.
	 * 
	 * @param src contains bytes for which the hash code is computed
	 * @param start the index of the first element in {@code src} to
	 * include in the computation
	 * @param length the number of bytes to include in the computation 
	 * @param seed seed value used to initialize engine state
	 * @return computed hash code
	 * value; on exit, the first two elements contain the computed result 
	 */
	private static long smallHash(byte[] src, int start, int length, long seed) {

		long h0, h1, h2, h3;
		h0 = seed;
		h1 = seed;
		h2 = ARBITRARY_BITS;
		h3 = ARBITRARY_BITS;
		
		int remaining = length;
		int pos = start;

		while (remaining >= 32) {
			h2 += packLong(src, pos);
			h3 += packLong(src, pos+8);
			
	        h2 = (h2 << 50) | (h2 >>> 14);  h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 52) | (h3 >>> 12);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 30) | (h0 >>> 34);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 41) | (h1 >>> 23);  h1 += h2;  h3 ^= h1;
	        h2 = (h2 << 54) | (h2 >>> 10);  h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 48) | (h3 >>> 16);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 38) | (h0 >>> 26);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 37) | (h1 >>> 27);  h1 += h2;  h3 ^= h1;
	        h2 = (h2 << 62) | (h2 >>> 2);   h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 34) | (h3 >>> 30);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 5)  | (h0 >>> 59);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 36) | (h1 >>> 28);  h1 += h2;  h3 ^= h1;	
			
			h0 += packLong(src, pos+16);
			h1 += packLong(src, pos+24);
			pos += 32;
			remaining -= 32;
		}
		
		if (remaining >= 16) {
			h2 += packLong(src, pos);
			h3 += packLong(src, pos+8);
			pos += 16;
	        remaining -= 16;

	        h2 = (h2 << 50) | (h2 >>> 14);  h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 52) | (h3 >>> 12);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 30) | (h0 >>> 34);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 41) | (h1 >>> 23);  h1 += h2;  h3 ^= h1;
	        h2 = (h2 << 54) | (h2 >>> 10);  h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 48) | (h3 >>> 16);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 38) | (h0 >>> 26);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 37) | (h1 >>> 27);  h1 += h2;  h3 ^= h1;
	        h2 = (h2 << 62) | (h2 >>> 2);   h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 34) | (h3 >>> 30);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 5)  | (h0 >>> 59);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 36) | (h1 >>> 28);  h1 += h2;  h3 ^= h1;	
	    }
		
		// assert remaining < 16;
		h3 += ((long)length) << 56;			

		if (remaining >= 8) {
			h2 += packLong(src, pos);
			pos += 8;	remaining -= 8;
			if (remaining > 0) {
				h3 += packPartial(src, pos, remaining);
			}
		} else if (remaining > 0) {
			h2 += packPartial(src, pos, remaining);
		} else {
			h2 += ARBITRARY_BITS;
			h3 += ARBITRARY_BITS;			
		}

        h3 ^= h2;  h2 = (h2 << 15) | (h2 >>> 49);  h3 += h2;
        h0 ^= h3;  h3 = (h3 << 52) | (h3 >>> 12);  h0 += h3;
        h1 ^= h0;  h0 = (h0 << 26) | (h0 >>> 38);  h1 += h0;
        h2 ^= h1;  h1 = (h1 << 51) | (h1 >>> 13);  h2 += h1;
        h3 ^= h2;  h2 = (h2 << 28) | (h2 >>> 36);  h3 += h2;
        h0 ^= h3;  h3 = (h3 << 9)  | (h3 >>> 55);  h0 += h3;
        h1 ^= h0;  h0 = (h0 << 47) | (h0 >>> 17);  h1 += h0;
        h2 ^= h1;  h1 = (h1 << 54) | (h1 >>> 10);  h2 += h1;
        h3 ^= h2;  h2 = (h2 << 32) | (h2 >>> 32);  h3 += h2;
        h0 ^= h3;  h3 = (h3 << 25) | (h3 >>> 39);  h0 += h3;
        h1 ^= h0;  h0 = (h0 << 63) | (h0 >>> 1);   h1 += h0;
		
		return h0;
	}
		
	/**
	 * Computes the hash code for an array of bytes, using the specified seed value.
	 * @param src contains bytes for which the hash code is computed
	 * @param start the index of the first element in {@code src} to
	 * include in the computation; must be non-negative and no larger than 
	 * {@code src.length}
	 * @param length the number of bytes to include in the computation; 
	 * must be non-negative and no larger than {@code src.length - start}
	 * @param seed seed value used to initialize engine state
	 * @return computed hash code
	 * @throws ArrayIndexOutOfBoundsException if the preconditions for 
	 * {@code offset} or {@code length} do not obtain
	 * @throws IllegalArgumentException if {@code length} is negative
	 */
	public static long hash(byte[] src, int start, int length, long seed) {
		if (length < SMALLHASH_LIMIT_BYTES) {
			return smallHash(src, start, length, seed);
		}
		
		long h0, h1, h2, h3, h4, h5, h6, h7, h8, h9, h10, h11;
		h0 = h3 = h6 = h9 = seed;
		h1 = h4 = h7 = h10 = seed;
		h2 = h5 = h8 = h11 = ARBITRARY_BITS;
		
		int remaining = length;
		int pos = start;
		while (remaining >= BLOCK_SIZE_BYTES) {		
			h0 += packLong(src, pos); 
			h2 ^= h10;     
			h11 ^= h0;   
			h0 = (h0 << 11) | (h0 >>> 53);     
			h11 += h1;
			
			h1 += packLong(src, pos+8); 
			h3 ^= h11;     
			h0 ^= h1;     
			h1 = (h1 << 32) | (h1 >>> 32);     
			h0 += h2;

			h2 += packLong(src, pos+16); 
			h4 ^= h0;      
			h1 ^= h2;     
			h2 = (h2 << 43) | (h2 >>> 21);     
			h1 += h3;
		
			h3 += packLong(src, pos+24); 
			h5 ^= h1;      
			h2 ^= h3;     
			h3 = (h3 << 31) | (h3 >>> 33);     
			h2 += h4;

			h4 += packLong(src, pos+32); 
			h6 ^= h2;      
			h3 ^= h4;     
			h4 = (h4 << 17) | (h4 >>> 47);     
			h3 += h5;

			h5 += packLong(src, pos+40); 
			h7 ^= h3;      
			h4 ^= h5;     
			h5 = (h5 << 28) | (h5 >>> 36);     
			h4 += h6;

			h6 += packLong(src, pos+48); 
			h8 ^= h4;      
			h5 ^= h6;     
			h6 = (h6 << 39) | (h6 >>> 25);     
			h5 += h7;

			h7 += packLong(src, pos+56); 
			h9 ^= h5;      
			h6 ^= h7;     
			h7 = (h7 << 57) | (h7 >>> 7);     
			h6 += h8;

			h8 += packLong(src, pos+64); 
			h10 ^= h6;     
			h7 ^= h8;     
			h8 = (h8 << 55) | (h8 >>> 9);     
			h7 += h9;

			h9 += packLong(src, pos+72); 
			h11 ^= h7;     
			h8 ^= h9;     
			h9 = (h9 << 54) | (h9 >>> 10);     
			h8 += h10;

			h10 += packLong(src, pos+80); 
			h0 ^= h8;      
			h9 ^= h10;    
			h10 = (h10 << 22) | (h10 >>> 42);  
			h9 += h11;

			h11 += packLong(src, pos+88); 
			h1 ^= h9;      
			h10 ^= h11;   
			h11 = (h11 << 46) | (h11 >>> 18);  
			h10 += h0;
			
			remaining -= BLOCK_SIZE_BYTES;
			pos += BLOCK_SIZE_BYTES;
		}
					
		// assert remaining < BLOCK_SIZE_BYTES;
		int partialSize = remaining & 7;
		int wholeWords = remaining >>> 3;
		if (partialSize > 0) {
			long partial = packPartial(src, pos + (wholeWords << 3), partialSize);
			switch (wholeWords) {
			case 0:
				h0 += partial;
				break;
			case 1:
				h1 += partial;
				break;
			case 2:
				h2 += partial;
				break;
			case 3:
				h3 += partial;
				break;
			case 4:
				h4 += partial;
				break;
			case 5:
				h5 += partial;
				break;
			case 6:
				h6 += partial;
				break;
			case 7:
				h7 += partial;
				break;
			case 8:
				h8 += partial;
				break;
			case 9:
				h9 += partial;
				break;
			case 10:
				h10 += partial;
				break;
			case 11:
				h11 += partial;
				break;
			}
		}
		switch (wholeWords) { // fall-through is intentional
		case 11:
			h10 += packLong(src, pos+80);
		case 10:
			h9 += packLong(src, pos+72);
		case 9:
			h8 += packLong(src, pos+64);
		case 8:
			h7 += packLong(src, pos+56);
		case 7:
			h6 += packLong(src, pos+48);
		case 6:
			h5 += packLong(src, pos+40);
		case 5:
			h4 += packLong(src, pos+32);
		case 4:
			h3 += packLong(src, pos+24);
		case 3:
			h2 += packLong(src, pos+16);
		case 2:
			h1 += packLong(src, pos+8);
		case 1:
			h0 += packLong(src, pos);
		default:
			break;
		}

		h11 += ((long)remaining) << 56;

		for (int i = 0; i < 3; i++) {
	        h11 += h1;   
	        h2 ^= h11;   
	        h1 = (h1 << 44)  | (h1 >>> 20);
	        h0 += h2;    
	        h3 ^= h0;    
	        h2 = (h2 << 15)  | (h2 >>> 49);
	        h1 += h3;    
	        h4 ^= h1;    
	        h3 = (h3 << 34)  | (h3 >>> 30);
	        h2 += h4;    
	        h5 ^= h2;    
	        h4 = (h4 << 21)  | (h4 >>> 43);
	        h3 += h5;    
	        h6 ^= h3;    
	        h5 = (h5 << 38)  | (h5 >>> 26);
	        h4 += h6;    
	        h7 ^= h4;    
	        h6 = (h6 << 33)  | (h6 >>> 31);
	        h5 += h7;    
	        h8 ^= h5;    
	        h7 = (h7 << 10)  | (h7 >>> 54);
	        h6 += h8;    
	        h9 ^= h6;    
	        h8 = (h8 << 13)  | (h8 >>> 51);
	        h7 += h9;    
	        h10 ^= h7;    
	        h9 = (h9 << 38)  | (h9 >>> 26);
	        h8 += h10;   
	        h11 ^= h8;    
	        h10 = (h10 << 53) | (h10 >>> 11);
	        h9 += h11;   
	        h0 ^= h9;    
	        h11 = (h11 << 42) | (h11 >>> 22);
	        h10 += h0;   
	        h1 ^= h10;   
	        h0 = (h0 << 54)  | (h0 >>> 10);
		}
		return h0;
	}
	
	/**
	 * Packs four characters from a CharSequence into one long value, in little-endian
	 * order. 
	 * 
	 * @param src contains characters to pack
	 * @param start index of first character in {@code src} to pack
	 * @return long result containing packed characters
	 */
	private static long packLong(CharSequence src, int start) {
		return  (((long)src.charAt(start + 3)) << 48) 
				| (((long)src.charAt(start + 2)) << 32)
				| (((long)src.charAt(start + 1)) << 16)
				| ((long)src.charAt(start));
	}

	/**
	 * Packs 3 or fewer characters from a CharSequence into one long value, in little-
	 * endian order. 
	 * 
	 * @param src contains characters to pack
	 * @param start index of first character in {@code src} to pack
	 * @param remaining the number of characters to pack; must be less than 4
	 * and greater than zero.
	 * @return long result containing packed characters
	 */
	private static long packPartial(CharSequence src, int start, int remaining) {
		long h = 0;
		/**
		 * The case blocks fall through intentionally.
		 */
		switch (remaining) {
		case 3:
			h += ((long)src.charAt(start + 2)) << 32;
		case 2:
			h += ((long)src.charAt(start + 1)) << 16;
		case 1:
			h += ((long)src.charAt(start));
		}
		return h;
	}

	/**
	 * Computes the hash code for a small character sequence (less than 96 characters).
	 * This method is called automatically when applicable.
	 * 
	 * @param src contains characters for which the hash code is computed
	 * @param start the index of the first element in {@code src} to
	 * include in the computation
	 * @param length the number of characters to include in the computation 
	 * @param seed seed value used to initialize engine state
	 * @return computed hash code
	 */
	private static long smallHash(CharSequence src, int start, int length, long seed) {

		long h0, h1, h2, h3;
		h0 = seed;
		h1 = seed;
		h2 = ARBITRARY_BITS;
		h3 = ARBITRARY_BITS;
		
		int remaining = length;
		int pos = start;

		while (remaining >= 16) {
			h2 += packLong(src, pos);
			h3 += packLong(src, pos+4);
			
	        h2 = (h2 << 50) | (h2 >>> 14);  h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 52) | (h3 >>> 12);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 30) | (h0 >>> 34);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 41) | (h1 >>> 23);  h1 += h2;  h3 ^= h1;
	        h2 = (h2 << 54) | (h2 >>> 10);  h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 48) | (h3 >>> 16);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 38) | (h0 >>> 26);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 37) | (h1 >>> 27);  h1 += h2;  h3 ^= h1;
	        h2 = (h2 << 62) | (h2 >>> 2);   h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 34) | (h3 >>> 30);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 5)  | (h0 >>> 59);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 36) | (h1 >>> 28);  h1 += h2;  h3 ^= h1;	
			
			h0 += packLong(src, pos+8);
			h1 += packLong(src, pos+12);
			pos += 16;
			remaining -= 16;
		}
		
		if (remaining >= 8) {
			h2 += packLong(src, pos);
			h3 += packLong(src, pos+4);
			pos += 8;
	        remaining -= 8;

	        h2 = (h2 << 50) | (h2 >>> 14);  h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 52) | (h3 >>> 12);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 30) | (h0 >>> 34);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 41) | (h1 >>> 23);  h1 += h2;  h3 ^= h1;
	        h2 = (h2 << 54) | (h2 >>> 10);  h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 48) | (h3 >>> 16);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 38) | (h0 >>> 26);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 37) | (h1 >>> 27);  h1 += h2;  h3 ^= h1;
	        h2 = (h2 << 62) | (h2 >>> 2);   h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 34) | (h3 >>> 30);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 5)  | (h0 >>> 59);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 36) | (h1 >>> 28);  h1 += h2;  h3 ^= h1;	
		}
		
		// assert remaining < 8;
		h3 += ((long)(length << 1)) << 56;			

		if (remaining >= 4) {
			h2 += packLong(src, pos);
			pos += 4;	remaining -= 4;
			if (remaining > 0) {
				h3 += packPartial(src, pos, remaining);
			}
		} else if (remaining > 0){
			h2 += packPartial(src, pos, remaining);
		} else {
			h2 += ARBITRARY_BITS;
			h3 += ARBITRARY_BITS;			
		}

        h3 ^= h2;  h2 = (h2 << 15) | (h2 >>> 49);  h3 += h2;
        h0 ^= h3;  h3 = (h3 << 52) | (h3 >>> 12);  h0 += h3;
        h1 ^= h0;  h0 = (h0 << 26) | (h0 >>> 38);  h1 += h0;
        h2 ^= h1;  h1 = (h1 << 51) | (h1 >>> 13);  h2 += h1;
        h3 ^= h2;  h2 = (h2 << 28) | (h2 >>> 36);  h3 += h2;
        h0 ^= h3;  h3 = (h3 << 9)  | (h3 >>> 55);  h0 += h3;
        h1 ^= h0;  h0 = (h0 << 47) | (h0 >>> 17);  h1 += h0;
        h2 ^= h1;  h1 = (h1 << 54) | (h1 >>> 10);  h2 += h1;
        h3 ^= h2;  h2 = (h2 << 32) | (h2 >>> 32);  h3 += h2;
        h0 ^= h3;  h3 = (h3 << 25) | (h3 >>> 39);  h0 += h3;
        h1 ^= h0;  h0 = (h0 << 63) | (h0 >>> 1);   h1 += h0;
		
		return h0;
	}
	
	/**
	 * Computes the hash code for a character sequence, using the specified 
	 * seed value.
	 * @param src contains characters for which the hash code is computed
	 * @param start the index of the first element in {@code src} to
	 * include in the computation; must be non-negative and no larger than 
	 * {@code src.length()}
	 * @param length the number of characters to include in the computation; 
	 * must be non-negative and no larger than {@code src.length() - start}
	 * @param seed seed value used to initialize engine state
	 * @return computed hash code
	 * @throws ArrayIndexOutOfBoundsException if the preconditions for 
	 * {@code offset} or {@code length} do not obtain
	 * @throws IllegalArgumentException if {@code length} is negative
	 */
	public static long hash(CharSequence src, int start, int length, long seed) {
		if (length < SMALLHASH_LIMIT_CHARS) {
			return smallHash(src, start, length, seed);
		}
		
		long h0, h1, h2, h3, h4, h5, h6, h7, h8, h9, h10, h11;
		h0 = h3 = h6 = h9 = seed;
		h1 = h4 = h7 = h10 = seed;
		h2 = h5 = h8 = h11 = ARBITRARY_BITS;
		
		int remaining = length;
		int pos = start;
		while (remaining >= BLOCK_SIZE_CHARS) {		
			h0 += packLong(src, pos); 
			h2 ^= h10;     
			h11 ^= h0;   
			h0 = (h0 << 11) | (h0 >>> 53);  
			h11 += h1;
			
			h1 += packLong(src, pos+4); 
			h3 ^= h11;     
			h0 ^= h1;     
			h1 = (h1 << 32) | (h1 >>> 32);     
			h0 += h2;

			h2 += packLong(src, pos+8); 
			h4 ^= h0;      
			h1 ^= h2;     
			h2 = (h2 << 43) | (h2 >>> 21);     
			h1 += h3;
		
			h3 += packLong(src, pos+12); 
			h5 ^= h1;      
			h2 ^= h3;     
			h3 = (h3 << 31) | (h3 >>> 33);     
			h2 += h4;

			h4 += packLong(src, pos+16); 
			h6 ^= h2;      
			h3 ^= h4;     
			h4 = (h4 << 17) | (h4 >>> 47);     
			h3 += h5;

			h5 += packLong(src, pos+20); 
			h7 ^= h3;      
			h4 ^= h5;     
			h5 = (h5 << 28) | (h5 >>> 36);     
			h4 += h6;

			h6 += packLong(src, pos+24); 
			h8 ^= h4;      
			h5 ^= h6;     
			h6 = (h6 << 39) | (h6 >>> 25);     
			h5 += h7;

			h7 += packLong(src, pos+28); 
			h9 ^= h5;      
			h6 ^= h7;     
			h7 = (h7 << 57) | (h7 >>> 7);     
			h6 += h8;

			h8 += packLong(src, pos+32); 
			h10 ^= h6;     
			h7 ^= h8;     
			h8 = (h8 << 55) | (h8 >>> 9);     
			h7 += h9;

			h9 += packLong(src, pos+36); 
			h11 ^= h7;     
			h8 ^= h9;     
			h9 = (h9 << 54) | (h9 >>> 10);     
			h8 += h10;

			h10 += packLong(src, pos+40); 
			h0 ^= h8;      
			h9 ^= h10;    
			h10 = (h10 << 22) | (h10 >>> 42);  
			h9 += h11;

			h11 += packLong(src, pos+44); 
			h1 ^= h9;      
			h10 ^= h11;   
			h11 = (h11 << 46) | (h11 >>> 18);  
			h10 += h0;		
			
			remaining -= BLOCK_SIZE_CHARS;
			pos += BLOCK_SIZE_CHARS;
		}
					
		// assert remaining < BLOCK_SIZE_CHARS;
		int partialSize = remaining & 3;
		int wholeWords = remaining >> 2;
		if (partialSize > 0) {
			long partial = packPartial(src, pos + (wholeWords << 2), partialSize);
			switch (wholeWords) {
			case 0:
				h0 += partial;
				break;
			case 1:
				h1 += partial;
				break;
			case 2:
				h2 += partial;
				break;
			case 3:
				h3 += partial;
				break;
			case 4:
				h4 += partial;
				break;
			case 5:
				h5 += partial;
				break;
			case 6:
				h6 += partial;
				break;
			case 7:
				h7 += partial;
				break;
			case 8:
				h8 += partial;
				break;
			case 9:
				h9 += partial;
				break;
			case 10:
				h10 += partial;
				break;
			case 11:
				h11 += partial;
				break;
			}
		}
		switch (wholeWords) { // fall-through is intentional
		case 11:
			h10 += packLong(src, pos+40);
		case 10:
			h9 += packLong(src, pos+36);
		case 9:
			h8 += packLong(src, pos+32);
		case 8:
			h7 += packLong(src, pos+28);
		case 7:
			h6 += packLong(src, pos+24);
		case 6:
			h5 += packLong(src, pos+20);
		case 5:
			h4 += packLong(src, pos+16);
		case 4:
			h3 += packLong(src, pos+12);
		case 3:
			h2 += packLong(src, pos+8);
		case 2:
			h1 += packLong(src, pos+4);
		case 1:
			h0 += packLong(src, pos);
		default:
			break;
		}

		h11 += ((long)remaining<<1) << 56;

		for (int i = 0; i < 3; i++) {
	        h11 += h1;   
	        h2 ^= h11;   
	        h1 = (h1 << 44)  | (h1 >>> 20);
	        h0 += h2;    
	        h3 ^= h0;    
	        h2 = (h2 << 15)  | (h2 >>> 49);
	        h1 += h3;    
	        h4 ^= h1;    
	        h3 = (h3 << 34)  | (h3 >>> 30);
	        h2 += h4;    
	        h5 ^= h2;    
	        h4 = (h4 << 21)  | (h4 >>> 43);
	        h3 += h5;    
	        h6 ^= h3;    
	        h5 = (h5 << 38)  | (h5 >>> 26);
	        h4 += h6;    
	        h7 ^= h4;    
	        h6 = (h6 << 33)  | (h6 >>> 31);
	        h5 += h7;    
	        h8 ^= h5;    
	        h7 = (h7 << 10)  | (h7 >>> 54);
	        h6 += h8;    
	        h9 ^= h6;    
	        h8 = (h8 << 13)  | (h8 >>> 51);
	        h7 += h9;    
	        h10 ^= h7;    
	        h9 = (h9 << 38)  | (h9 >>> 26);
	        h8 += h10;   
	        h11 ^= h8;    
	        h10 = (h10 << 53) | (h10 >>> 11);
	        h9 += h11;   
	        h0 ^= h9;    
	        h11 = (h11 << 42) | (h11 >>> 22);
	        h10 += h0;   
	        h1 ^= h10;   
	        h0 = (h0 << 54)  | (h0 >>> 10);
		}
		return h0;
	}

	/**
	 * Computes the hash code for a small array of long (less than 12 long 
	 * values). This method is called automatically when applicable.
	 * Parameter constraints are enforced by the calling method. 
	 * 
	 * @param src contains long values for which the hash code is computed
	 * @param start index of the first element in {@code src} to
	 * include in the computation
	 * @param length the number of long values to include in the computation 
	 * @param seed seed value used to initialize engine state
	 * @return computed hash code
	 */
	private static long smallHash(long[] src, int start, int length, long seed) {
		
		long h0, h1, h2, h3;
		h0 = seed;
		h1 = seed;
		h2 = ARBITRARY_BITS;
		h3 = ARBITRARY_BITS;
		
		int remaining = length;
		int pos = start;

		while (remaining >= 4) {
			h2 += src[pos];
			h3 += src[pos+1];
			
	        h2 = (h2 << 50) | (h2 >>> 14);  h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 52) | (h3 >>> 12);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 30) | (h0 >>> 34);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 41) | (h1 >>> 23);  h1 += h2;  h3 ^= h1;
	        h2 = (h2 << 54) | (h2 >>> 10);  h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 48) | (h3 >>> 16);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 38) | (h0 >>> 26);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 37) | (h1 >>> 27);  h1 += h2;  h3 ^= h1;
	        h2 = (h2 << 62) | (h2 >>> 2);   h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 34) | (h3 >>> 30);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 5)  | (h0 >>> 59);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 36) | (h1 >>> 28);  h1 += h2;  h3 ^= h1;	
			
			h0 += src[pos+2];
			h1 += src[pos+3];
			pos += 4;
			remaining -= 4;
		}
		
		if (remaining >= 2) {
			h2 += src[pos];
			h3 += src[pos+1];
			pos += 2;
			remaining -= 2;

	        h2 = (h2 << 50) | (h2 >>> 14);  h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 52) | (h3 >>> 12);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 30) | (h0 >>> 34);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 41) | (h1 >>> 23);  h1 += h2;  h3 ^= h1;
	        h2 = (h2 << 54) | (h2 >>> 10);  h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 48) | (h3 >>> 16);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 38) | (h0 >>> 26);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 37) | (h1 >>> 27);  h1 += h2;  h3 ^= h1;
	        h2 = (h2 << 62) | (h2 >>> 2);   h2 += h3;  h0 ^= h2;
	        h3 = (h3 << 34) | (h3 >>> 30);  h3 += h0;  h1 ^= h3;
	        h0 = (h0 << 5)  | (h0 >>> 59);  h0 += h1;  h2 ^= h0;
	        h1 = (h1 << 36) | (h1 >>> 28);  h1 += h2;  h3 ^= h1;	

		}
		
		// assert remaining < 2;
		h3 += ((long)(length<<3)) << 56;

		if (remaining > 0) {
			h2 += src[pos];
		} else {
			h2 += ARBITRARY_BITS;
			h3 += ARBITRARY_BITS;			
		}

        h3 ^= h2;  h2 = (h2 << 15) | (h2 >>> 49);  h3 += h2;
        h0 ^= h3;  h3 = (h3 << 52) | (h3 >>> 12);  h0 += h3;
        h1 ^= h0;  h0 = (h0 << 26) | (h0 >>> 38);  h1 += h0;
        h2 ^= h1;  h1 = (h1 << 51) | (h1 >>> 13);  h2 += h1;
        h3 ^= h2;  h2 = (h2 << 28) | (h2 >>> 36);  h3 += h2;
        h0 ^= h3;  h3 = (h3 << 9)  | (h3 >>> 55);  h0 += h3;
        h1 ^= h0;  h0 = (h0 << 47) | (h0 >>> 17);  h1 += h0;
        h2 ^= h1;  h1 = (h1 << 54) | (h1 >>> 10);  h2 += h1;
        h3 ^= h2;  h2 = (h2 << 32) | (h2 >>> 32);  h3 += h2;
        h0 ^= h3;  h3 = (h3 << 25) | (h3 >>> 39);  h0 += h3;
        h1 ^= h0;  h0 = (h0 << 63) | (h0 >>> 1);   h1 += h0;
		
        return h0;
	}

	/**
	 * Computes the hash code for an array of long, using the specified seed 
	 * value.
	 * @param src contains long values for which the hash code is computed
	 * @param start the index of the first element in {@code src} to
	 * include in the computation; must be non-negative and no larger than 
	 * {@code src.length}
	 * @param length the number of long values to include in the computation; 
	 * must be non-negative and no larger than {@code src.length - start}
	 * @param seed seed value used to initialize engine state
	 * @return computed hash code
	 * @throws ArrayIndexOutOfBoundsException if the preconditions for 
	 * {@code offset} or {@code length} do not obtain
	 * @throws IllegalArgumentException if {@code length} is negative
	 */
	public static long hash(long[] src, int start, int length,  long seed) {
		if (length < SMALLHASH_LIMIT_LONGS) {
			return smallHash(src, start, length, seed);
		}
		long h0, h1, h2, h3, h4, h5, h6, h7, h8, h9, h10, h11;
		h0 = h3 = h6 = h9 = seed;
		h1 = h4 = h7 = h10 = seed;
		h2 = h5 = h8 = h11 = ARBITRARY_BITS;
		int pos = start;
		int remaining = length;
		
		// mix complete blocks:
		while (remaining >= BLOCK_SIZE_LONGS) {
			
			h0 += src[pos]; 
			h2 ^= h10;     
			h11 ^= h0;   
			h0 = (h0 << 11) | (h0 >>> 53);     
			h11 += h1;
			
			h1 += src[pos+1]; 
			h3 ^= h11;     
			h0 ^= h1;     
			h1 = (h1 << 32) | (h1 >>> 32);     
			h0 += h2;

			h2 += src[pos+2]; 
			h4 ^= h0;      
			h1 ^= h2;     
			h2 = (h2 << 43) | (h2 >>> 21);     
			h1 += h3;
		
			h3 += src[pos+3]; 
			h5 ^= h1;      
			h2 ^= h3;     
			h3 = (h3 << 31) | (h3 >>> 33);     
			h2 += h4;

			h4 += src[pos+4]; 
			h6 ^= h2;      
			h3 ^= h4;     
			h4 = (h4 << 17) | (h4 >>> 47);     
			h3 += h5;

			h5 += src[pos+5]; 
			h7 ^= h3;      
			h4 ^= h5;     
			h5 = (h5 << 28) | (h5 >>> 36);     
			h4 += h6;

			h6 += src[pos+6]; 
			h8 ^= h4;      
			h5 ^= h6;     
			h6 = (h6 << 39) | (h6 >>> 25);     
			h5 += h7;

			h7 += src[pos+7]; 
			h9 ^= h5;      
			h6 ^= h7;     
			h7 = (h7 << 57) | (h7 >>> 7);     
			h6 += h8;

			h8 += src[pos+8]; 
			h10 ^= h6;     
			h7 ^= h8;     
			h8 = (h8 << 55) | (h8 >>> 9);     
			h7 += h9;

			h9 += src[pos+9]; 
			h11 ^= h7;     
			h8 ^= h9;     
			h9 = (h9 << 54) | (h9 >>> 10);     
			h8 += h10;

			h10 += src[pos+10]; 
			h0 ^= h8;      
			h9 ^= h10;    
			h10 = (h10 << 22) | (h10 >>> 42);  
			h9 += h11;

			h11 += src[pos+11]; 
			h1 ^= h9;      
			h10 ^= h11;   
			h11 = (h11 << 46) | (h11 >>> 18);  
			h10 += h0;	

			pos += BLOCK_SIZE_LONGS;
			remaining -= BLOCK_SIZE_LONGS;
		}
		
		// remainingBytes < BLOCK_SIZE;
		// end:

		switch (remaining) { // fall-through is intentional
		case 11:
			h10 += src[pos+10];
		case 10:
			h9 += src[pos+9];
		case 9:
			h8 += src[pos+8]; 
		case 8:
			h7 += src[pos+7];
		case 7:
			h6 += src[pos+6];
		case 6:
			h5 += src[pos+5];
		case 5:
			h4 += src[pos+4];
		case 4:
			h3 += src[pos+3];
		case 3:
			h2 += src[pos+2];
		case 2:
			h1 += src[pos+1];
		case 1:
			h0 += src[pos];
		default:
			break;
		}

		h11 += ((long)(remaining << 3)) << 56;

		for (int i = 0; i < 3; i++) {
	        h11 += h1;   
	        h2 ^= h11;   
	        h1 = (h1 << 44)  | (h1 >>> 20);
	        h0 += h2;    
	        h3 ^= h0;    
	        h2 = (h2 << 15)  | (h2 >>> 49);
	        h1 += h3;    
	        h4 ^= h1;    
	        h3 = (h3 << 34)  | (h3 >>> 30);
	        h2 += h4;    
	        h5 ^= h2;    
	        h4 = (h4 << 21)  | (h4 >>> 43);
	        h3 += h5;    
	        h6 ^= h3;    
	        h5 = (h5 << 38)  | (h5 >>> 26);
	        h4 += h6;    
	        h7 ^= h4;    
	        h6 = (h6 << 33)  | (h6 >>> 31);
	        h5 += h7;    
	        h8 ^= h5;    
	        h7 = (h7 << 10)  | (h7 >>> 54);
	        h6 += h8;    
	        h9 ^= h6;    
	        h8 = (h8 << 13)  | (h8 >>> 51);
	        h7 += h9;    
	        h10 ^= h7;    
	        h9 = (h9 << 38)  | (h9 >>> 26);
	        h8 += h10;   
	        h11 ^= h8;    
	        h10 = (h10 << 53) | (h10 >>> 11);
	        h9 += h11;   
	        h0 ^= h9;    
	        h11 = (h11 << 42) | (h11 >>> 22);
	        h10 += h0;   
	        h1 ^= h10;   
	        h0 = (h0 << 54)  | (h0 >>> 10);
		}
		return h0;
	}
	
	/**
	 * Constructs a new hash engine with seed value {0L, 0L}.
	 */
	public SpookyHash64() {
		this(0L);
	}
	
	/**
	 * Constructs a new hash engine with the specified seed value.
	 * @param seed seed value used to initialize the engine
	 */
	public SpookyHash64(long seed) {
		this.seed = seed;
	}
	
	/**
	 * Computes the hash code for a array of bytes, using the specified seed 
	 * value. The computation includes all elements in {@code src}.
	 * @param src array of bytes for which the hash code is computed
	 * @param seed seed value used to initialize engine state
	 * @return computed hash code
	 */
	public static long hash(byte[] src, long seed) {
		return hash(src, 0, src.length, seed);
	}
	
	/**
	 * Computes the hash code for an array of bytes, using the seed
	 * value associated with this instance.
	 * 
	 * @param src contains bytes for which the hash code is computed
	 * @param start the index of the first element in {@code src} to
	 * include in the computation; must be non-negative and no larger than 
	 * {@code src.length}
	 * @param length the number of bytes to include in the computation; 
	 * must be non-negative and no larger than {@code src.length - start}
	 * @return computed hash code
	 * @throws ArrayIndexOutOfBoundsException if the preconditions for 
	 * <code>length</code> or <code>offset</code> do not obtain
	 * @throws IllegalArgumentException if <code>length</code> is negative
	 */
	public long hash(byte[] src, int start, int length) {
		return hash(src, start, length, this.seed);
	}
	
	/**
	 * Computes the hash code for an array of bytes, using the seed
	 * value associated with this instance. 	 
	 * The computation includes all elements in {@code src}.
	 * 
	 * @param src contains bytes for which the hash code is computed
	 * @return computed hash code
	 */
	public long hash(byte[] src) {
		return hash(src, 0, src.length);
	}
		
	/**
	 * Computes the hash code for a character sequence, using the specified 
	 * seed value. 
	 * The computation includes all characters in {@code src}.
	 * @param src character sequence for which the hash code is computed
	 * @param seed seed value used to initialize engine state
	 * @return computed hash code
	 */
	public static long hash(CharSequence src, long seed) {
		return hash(src, 0, src.length(), seed);
	}
	
	/**
	 * Computes the hash code for a character sequence, using the seed
	 * value associated with this instance.
	 * 
	 * @param src contains characters for which the hash code is computed
	 * @param start the index of the first element in {@code src} to
	 * include in the computation; must be non-negative and no larger than 
	 * {@code src.length()}
	 * @param length the number of characters to include in the computation; 
	 * must be non-negative and no larger than {@code src.length() - start}
	 * @return computed hash code
	 * @throws ArrayIndexOutOfBoundsException if the preconditions for <code>length</code> or <code>offset</code> do not obtain
	 * @throws IllegalArgumentException if <code>length</code> is negative
	 */
	public long hash(CharSequence src, int start, int length) {
		return hash(src, start, length, this.seed);
	}
	
	/**
	 * Computes the hash code for a character sequence, using the seed
	 * value associated with this instance. 	 
	 * The computation includes all characters in {@code src}.
	 * 
	 * @param src character sequence for which the hash code is computed
	 * @return computed hash code
	 */
	public long hash(CharSequence src) {
		return hash(src, 0, src.length());
	}
	
	/**
	 * Computes the hash code for an array of long, using the specified seed value. 
	 * The computation includes all elements in {@code src}.
	 * @param src array of long for which the hash code is computed
	 * @param seed seed value used to initialize engine state
	 * @return computed hash code
	 */
	public static long hash(long[] src, long seed) {
		return hash(src, 0, src.length, seed);
	}

	/**
	 * Computes the hash code for an array of long, using the seed
	 * value associated with this instance.
	 * 
	 * @param src contains long values for which the hash code is computed
	 * @param start the index of the first element in {@code src} to
	 * include in the computation; must be non-negative and no larger than 
	 * {@code src.length}
	 * @param length the number of long values to include in the computation; 
	 * must be non-negative and no larger than {@code src.length - start}
	 * @return computed hash code
	 * @throws ArrayIndexOutOfBoundsException if the preconditions for <code>length</code> or <code>offset</code> do not obtain
	 * @throws IllegalArgumentException if <code>length</code> is negative
	 */
	public long hash(long[] src, int start, int length) {
		return hash(src, start, length, this.seed);
	}
	
	/**
	 * Computes the hash code for an array of long, using the seed
	 * value associated with this instance. 	 
	 * The computation includes all elements in {@code src}.
	 * 
	 * @param src contains long values for which the hash code is computed
	 * @return computed hash code
	 */
	public long hash(long[] src) {
		return hash(src, 0, src.length);
	}

}