/**
 * 
 */
package org.streaminer.stream.learner;

import org.streaminer.stream.model.PredictionModel;


/**
 * @author chris
 *
 */
public abstract class AbstractRegressor<D> 
	implements Regressor<D> {

	/** The unique class ID */
	private static final long serialVersionUID = 951585509815153514L;

	/**
	 * @see stream.model.PredictionModel#predict(java.lang.Object)
	 */
	@Override
	public abstract Double predict(D item);

	
	/**
	 * @see stream.learner.Learner#getModel()
	 */
	@Override
	public PredictionModel<D, Double> getModel() {
		return this;
	}

	
	/**
	 * @see stream.learner.Learner#init()
	 */
	@Override
	public void init() {
	}

	
	/**
	 * @see stream.learner.Learner#learn(java.lang.Object)
	 */
	@Override
	public abstract void learn(D item);
}