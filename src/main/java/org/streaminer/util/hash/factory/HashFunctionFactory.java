package org.streaminer.util.hash.factory;

import org.streaminer.util.hash.function.HashFunction;
import java.io.Serializable;

/**
 * <p></p>
 *
 * @author Marcin Skirzynski
 * @param <T>
 */
public interface HashFunctionFactory<T> extends Serializable {

	public HashFunction<T> build( long domain );

}
