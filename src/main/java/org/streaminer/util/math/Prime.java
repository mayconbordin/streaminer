package org.streaminer.util.math;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <p>
 * Class for all prime-related methods. This class will not compute primes. All
 * primes in the long-range are computed and stored in a file. This class uses
 * this file for all methods.
 * </p>
 * 
 * @author Marcin Skirzynski
 *
 */
public class Prime {
	
	/**
	 * <p>
	 * The file which contains the primes
	 */
	private static final File PRIME_FILE = new File(Prime.class.getResource("/prim.txt").getFile());
	
	/**
	 * <p>
	 * A random generator without seed.
	 * </p>
	 */
	private static final Random RANDOM = new Random();

        /**
         * private constructor as it makes no sense to be able to create an instance of
         * a utility class / a class with static methods only
         */
        private Prime() {
        }

	/**
	 * <p>
	 * Returns a randomly chosen prime between a left bound
	 * and a right bound.
	 * </p>
	 * 
	 * @param min		the left bound of the randomly chosen prime (incl. min)
	 * @param max		the right bound of the randomly chosen prime (incl. max)
	 * @return	a randomly chosen prime between leftBound and rightBound
	 */
	public static long getRandom( long min, long max ) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(PRIME_FILE));
			
			List<Long> primes = new ArrayList<Long>();
			long prime = Long.parseLong(in.readLine());
			
			while( prime<=max ) {
				if( prime>=min )
					primes.add(prime);
				prime = Long.parseLong(in.readLine());
			}
			
			in.close();
			return primes.get(RANDOM.nextInt(primes.size()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
