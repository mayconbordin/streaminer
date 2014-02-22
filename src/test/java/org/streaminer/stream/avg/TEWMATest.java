package org.streaminer.stream.avg;

import java.security.NoSuchAlgorithmException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class TEWMATest {
    static final String message = "272";
    static final int[] correctIdx = new int[]{9754050, 11917035, 12238481, 14522717, 5947587, 1548765, 7212604, 12309647, 16066215, 1232510, 10694178, 14271239, 4784036, 15510045, 304956, 5224878};
    static final String correctHash = "C2D594EBD6B591BEBA5D99DDC3C05ADDA1173C0E";
    
    public TEWMATest() {
    }
    
    public void printBytes(byte[] in) {
        System.out.println("");
        for (int i=0; i < in.length; i++) {
            System.out.printf("%02X ", in[i]);
        }
        System.out.println("");
    }
    
    @Test
    public void testSHA1() throws NoSuchAlgorithmException {
        TEWMA tewma = new TEWMA(16, 24, 1, 1);
        byte[] digest = tewma.toSHA1(message.getBytes());
        String hex = tewma.bytesToHex(digest);
        
        //printBytes(digest);
        System.out.println(hex);
        
        assertEquals(correctHash, hex);
    }
    
    @Test
    public void testIndexes() throws NoSuchAlgorithmException {
        TEWMA tewma = new TEWMA(16, 24, 1, 1);
        int[] indexes = tewma.indexes(message);
        
        System.out.println("Indexes:");
        for (int i=0; i<indexes.length; i++) {
            assertEquals(correctIdx[i], indexes[i]);
            System.out.println(indexes[i]);
        }
    }
    
    @Test
    public void testAdd() {
        System.out.println("testAdd");
        TEWMA tewma = new TEWMA(16, 24, 0.3, 10);
        
        double value = tewma.add(272, 1, System.currentTimeMillis()/1000);
        System.out.println("Value: " + value);
        
        assertEquals(Math.ceil(1.203972), Math.ceil(value), 0);
    }
}
