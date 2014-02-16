/**
 * 
 */
package org.streaminer.stream.classifier.bayes;

import org.streaminer.stream.classifier.AbstractMultiClassifier;
import org.streaminer.stream.data.Data;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This is a special variant of the NaiveBayes classifier, which implements an ensemble
 * of NaiveBayes instances, each of which predicts one of a set of labels.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public class MultiBayes extends AbstractMultiClassifier<Data, String> {

	/** The unique class ID */
	private static final long serialVersionUID = 6744468614488176888L;

	static Logger log = LoggerFactory.getLogger( MultiBayes.class );

	/* A mapping of attributes to the appropriate NaiveBayes instance */
	Map<String,NaiveBayes> learner = new LinkedHashMap<String,NaiveBayes>();


	/**
	 * @see stream.learner.AbstractClassifier#learn(java.lang.Object)
	 */
	@Override
	public void learn(Data item) {
		for( String attribute : item.keySet() ){
			NaiveBayes nb = learner.get( attribute );
			if( nb == null ){
				if( attributes == null || attributes.contains( attribute ) ){
					log.info( "Creating new classifier for attribute '{}'", attribute );
					nb = createBayesLearner( attribute );
					learner.put( attribute, nb );
				}
			}

			if( nb != null ){
				log.debug( "Training classifier for attribute '{}'", attribute );
				nb.learn( item );
			}
		}
	}

	/**
	 * @see stream.learner.AbstractClassifier#predict(java.lang.Object)
	 */
	@Override
	public Map<String,String> predict(Data item) {
		Map<String,String> result = new LinkedHashMap<String,String>();

		for( String labelAttribute : learner.keySet() ){
			String prediction = learner.get( labelAttribute ).predict( item );
			//log.debug( "Prediction for '{}' is: {}", labelAttribute, prediction );
			result.put( labelAttribute, prediction );
		}

		return result;
	}

	
	/**
	 * This method is used to create a new NaiveBayes learner instance for the
	 * given attribute.
	 * 
	 * @param attribute
	 * @return
	 */
	protected NaiveBayes createBayesLearner( String attribute ){
		return new NaiveBayes( attribute );
	}
}