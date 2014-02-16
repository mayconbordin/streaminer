/**
 * 
 */
package org.streaminer.stream.mapper;

import org.streaminer.stream.data.Data;

/**
 * @author chris
 *
 */
public class Identity implements IMapper<Data, Data> {

	/**
	 * @see stream.data.mapper.Mapper#map(java.lang.Object)
	 */
	@Override
	public Data map(Data input) throws Exception {
		return input;
	}
}