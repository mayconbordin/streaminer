package org.streaminer.stream.membership;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class DLeftCountingBloomFilterTest {
    
    public DLeftCountingBloomFilterTest() {
    }
    
    @Test
    public void test() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        
        byte[] b = md.digest("testf".getBytes());
        
        String hex = byteArrayToHexString(b);
        
        System.out.println("Byte size: " + b.length);
        System.out.println("Hex: " + hex);
        
        DLeftCountingBloomFilter filter = new DLeftCountingBloomFilter(8, 32);
        
        long bits = filter.getBits(b, 5, 0);
        System.out.println("Bits: " + bits);
    }
    
    
    public static String byteArrayToHexString(byte[] b) {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }
}
