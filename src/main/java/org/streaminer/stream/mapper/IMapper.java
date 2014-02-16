/**
 * 
 */
package org.streaminer.stream.mapper;

/**
 * <p>
 * This class defines a mapper as an instance that receives a datum from
 * a data-stream and maps it to a generic new element.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public interface IMapper<I, O> {

	/**
	 * Map the given input element to an object of the specified generic class.
	 * 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public O map( I input ) throws Exception;
}