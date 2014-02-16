/**
 * 
 */
package org.streaminer.stream.mapper;

import org.streaminer.stream.data.Data;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class KeyMapper implements IMapper<Data, Data> {

	static Logger log = LoggerFactory.getLogger(KeyMapper.class);
	String oldKey;
	String newKey;

	public KeyMapper(String oldKey, String newKey) {
		this.oldKey = oldKey;
		this.newKey = newKey;
	}

	public KeyMapper() {
		this.oldKey = "";
		this.newKey = "";
	}

	/**
	 * @return the oldKey
	 */
	public String getOld() {
		return oldKey;
	}

	/**
	 * @param oldKey
	 *            the oldKey to set
	 */
	public void setOld(String oldKey) {
		this.oldKey = oldKey;
	}

	/**
	 * @return the newKey
	 */
	public String getNew() {
		return newKey;
	}

	/**
	 * @param newKey
	 *            the newKey to set
	 */
	public void setNew(String newKey) {
		this.newKey = newKey;
	}

	/**
	 * @see stream.data.mapper.Mapper#map(java.lang.Object)
	 */
	@Override
	public Data map(Data input) throws Exception {
		if (input.containsKey(oldKey)) {
			if (input.containsKey(newKey))
				log.warn("Overwriting existing key '{}'!", newKey);

			Serializable o = input.remove(oldKey);
			input.put(newKey, o);
		}
		return input;
	}
}