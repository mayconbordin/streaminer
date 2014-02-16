package org.streaminer.util.math;

/**
 * this class should provide math helper and wrapper functions
 * 
 * @author Carsten Przyluczky
 *
 */
public class MathHelper {

        /**
         * private constructor as it makes no sense to be able to create an instance of
         * a utility class / a class with static methods only
         */
        private MathHelper() {
        }

	/**
	 * This method simply calculates the log with base 2 
	 * @param number
	 * @return
	 */
	public static double log2(double number){
		return Math.log10(number)/Math.log10(2d);
	}
}
