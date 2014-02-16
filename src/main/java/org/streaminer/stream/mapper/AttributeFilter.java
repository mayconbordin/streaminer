/**
 * 
 */
package org.streaminer.stream.mapper;

import org.streaminer.stream.data.Data;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class AttributeFilter implements IMapper<Data, Data> {

	static Logger log = LoggerFactory.getLogger(AttributeFilter.class);

	String include;
	String exclude;

	public AttributeFilter() {
		include = ".*";
		exclude = null;
	}

	/**
	 * @return the include
	 */
	public String getInclude() {
		return include;
	}

	/**
	 * @param include
	 *            the include to set
	 */
	public void setInclude(String include) {
		this.include = include;
	}

	/**
	 * @return the exclude
	 */
	public String getExclude() {
		return exclude;
	}

	/**
	 * @param exclude
	 *            the exclude to set
	 */
	public void setExclude(String exclude) {
		this.exclude = exclude;
	}

	/**
	 * @see stream.data.DataProcessor#process(stream.data.Data)
	 */
	@Override
	public Data map(Data data) {

		ArrayList<String> keys = new ArrayList<String>();

		for (String key : data.keySet()) {
			if (include == null || key.matches(include)) {
				if (exclude != null && key.matches(exclude)) {
					log.debug("Excluding key '{}'");
					keys.add(key);
				}
			} else
				keys.add(key);
		}
		for (String key : keys)
			data.remove(key);
		return data;
	}
}