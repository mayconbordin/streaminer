/**
 * 
 */
package org.streaminer.stream.classifier;

import org.streaminer.stream.model.PredictionModel;

/**
 * <p>
 * This class implements an abstract classifier, i.e. an instance that is capable of learning
 * from observations of a specific, generic type <code>D</code> and predicting class values of
 * type <code>C</code>. 
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 * @param <D> The data type of the input examples used for learning and prediction.
 * @param <C> The label type, i.e. the Java class of the predicted outcome.
 * 
 */
public abstract class AbstractClassifier<D,C> implements Classifier<D,C> {
    /** The unique class ID */
    private static final long serialVersionUID = -8809157061575037435L;


    /**
     * @see stream.learner.Learner#getModel()
     */
    @Override
    public final PredictionModel<D, C> getModel() {
        return this;
    }


    /**
     * @see stream.learner.Learner#init()
     */
    public void init(){
    }


    /**
     * @see stream.model.PredictionModel#predict(java.lang.Object)
     */
    @Override
    public abstract C predict(D item);


    /**
     * @see stream.learner.Learner#learn(java.lang.Object)
     */
    @Override
    public abstract void learn(D item);
}
