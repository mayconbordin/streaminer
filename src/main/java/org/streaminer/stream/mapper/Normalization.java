/**
 * 
 */
package org.streaminer.stream.mapper;

import org.streaminer.stream.data.Data;
import org.streaminer.stream.learner.LearnerUtils;

/**
 * @author chris
 * 
 */
public class Normalization implements IMapper<Data, Data> {

	String attribute = ".*";

	Double minimum = -10.0d;
	Double maximum = 10.0d;

	double range = Math.abs(maximum - minimum);
	double offset = 0.5d * (maximum - minimum);

	/**
	 * @see stream.data.DataProcessor#process(stream.data.Data)
	 */
	@Override
	public Data map(Data data) {
		for (String key : data.keySet()) {
			if (key.matches(attribute) && LearnerUtils.isNumerical(key, data)) {
				Double value = LearnerUtils.getDouble(key, data);
				data.put(key, normalize(value));
			}
		}

		return data;
	}

	protected void update() {
		range = Math.abs(maximum - minimum);
		offset = 0.5 * range;
	}

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
		update();
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
		update();
	}

	protected Double normalize(Double d) {
		Double v = d;
		if (d < minimum)
			v = minimum;

		if (d > maximum)
			v = maximum;

		return (v + offset) / range;
	}
}