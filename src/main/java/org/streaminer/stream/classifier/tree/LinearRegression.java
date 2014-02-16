package org.streaminer.stream.classifier.tree;

import org.streaminer.stream.learner.Regressor;
import java.util.Map;

public interface LinearRegression<D> 
	extends Regressor<D>
{
	public void setParameters(Map<String, Object> parameters);

}
