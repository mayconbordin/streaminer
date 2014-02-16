/**
 * 
 */
package org.streaminer.stream.mapper;

import org.streaminer.stream.data.Data;
import org.streaminer.stream.data.DataUtils;

/**
 * @author chris
 * 
 */
public class NumericalBinning implements IMapper<Data, Data> {

	Double minimum = 0.0d;

	Double maximum = 10.0d;

	Integer bins = 10;

	String include = ".*";

	double[] buckets = null;

	/**
	 * @return the minimum
	 */
	public Double getMinimum() {
		return minimum;
	}

	/**
	 * @param minimum
	 *            the minimum to set
	 */
	public void setMinimum(Double minimum) {
		this.minimum = minimum;
	}

	/**
	 * @return the maximum
	 */
	public Double getMaximum() {
		return maximum;
	}

	/**
	 * @param maximum
	 *            the maximum to set
	 */
	public void setMaximum(Double maximum) {
		this.maximum = maximum;
	}

	/**
	 * @return the bins
	 */
	public Integer getBins() {
		return bins;
	}

	/**
	 * @param bins
	 *            the bins to set
	 */
	public void setBins(Integer bins) {
		this.bins = bins;
		buckets = null;
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

	private void init() {
		buckets = new double[Math.max(1, bins)];
		double step = (maximum - minimum) / bins.doubleValue();
		buckets[0] = 0.0d;
		for (int i = 1; i < buckets.length; i++) {
			buckets[i] = buckets[i - 1] + step;
		}
	}

	/**
	 * @see stream.data.DataProcessor#process(stream.data.Data)
	 */
	@Override
	public Data map(Data data) {

		if (buckets == null)
			init();

		for (String key : DataUtils.getKeys(data)) {
			if ((include == null || key.matches(include))
					&& data.get(key).getClass() == Double.class)
				data.put(key, map((Double) data.get(key)));
		}

		return data;
	}

	protected String map(Double d) {
		if (d < buckets[0])
			return "bucket[first]";

		for (int i = 0; i < buckets.length; i++)
			if (i + 1 < buckets.length && buckets[i + 1] > d)
				return "bucket[" + i + "]";

		return "bucket[last]";
	}
}