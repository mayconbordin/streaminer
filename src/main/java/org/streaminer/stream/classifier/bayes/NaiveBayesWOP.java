/**
 * 
 */
package org.streaminer.stream.classifier.bayes;

import org.streaminer.stream.classifier.AbstractClassifier;
import org.streaminer.stream.data.Data;
import org.streaminer.stream.model.Distribution;
import org.streaminer.stream.model.NominalDistributionModel;
import org.streaminer.stream.model.NumericalDistributionModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class implements a NaiveBayes classifier. It combines the learning algorithm and
 * the model implementation in one. The implementation provides support for numerical
 * (Double) and nominal (String) attributes.
 * </p>
 * <p>
 * The implementation is a strictly incremental one and supports memory limitation. The
 * memory limitation is carried out by truncating the distribution models built for each
 * of the observed attributes.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public class NaiveBayesWOP 
extends AbstractClassifier<Data,String>
{
	/** The unique class ID */
	private static final long serialVersionUID = 1095437834368310484L;

	/* The global logger for this class */
	static Logger log = LoggerFactory.getLogger( NaiveBayesWOP.class );

	/* The attribute used as label */
	String labelAttribute = null;

	/* The La-Place correction term */
	Double laplaceCorrection = 0.0001;

	/* This is the distribution of the different classes observed */
	Distribution<String> classDistribution = null; // createNominalDistribution();

	/* A map providing the distributions of the attributes (nominal,numerical) */
	Map<String,Distribution<?>> distributions = new HashMap<String,Distribution<?>>();


	/**
	 * Create a new NaiveBayes instance. The label attribute is automatically determined
	 * by the learner, if not explicitly set with the <code>setLabelAttribute</code>
	 * method.
	 */
	public NaiveBayesWOP(){
		classDistribution = createNominalDistribution();
	}


	/**
	 * Create a new NaiveBayes instance which uses the specified attribute as label.
	 * 
	 * @param labelAttribute
	 */
	public NaiveBayesWOP( String labelAttribute ){
		this();
		setLabelAttribute( labelAttribute );
	}


	/**
	 * @return the labelAttribute
	 */
	public String getLabelAttribute() {
		return labelAttribute;
	}


	/**
	 * @param labelAttribute the labelAttribute to set
	 */
	public void setLabelAttribute(String labelAttribute) {
		this.labelAttribute = labelAttribute;
	}


	/**
	 * @return the laplaceCorrection
	 */
	public Double getLaplaceCorrection() {
		return laplaceCorrection;
	}


	/**
	 * @param laplaceCorrection the laplaceCorrection to set
	 */
	public void setLaplaceCorrection(Double laplaceCorrection) {
		this.laplaceCorrection = laplaceCorrection;
	}


	/**
	 * @see stream.model.PredictionModel#predict(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String predict(Data item) {

		Map<String,Double> classLikeli = new LinkedHashMap<String,Double>();
		log.debug( "Predicting one of these classes: {}", classDistribution.getElements() );

		for( String label : getClassDistribution().getElements() ){
			// 9/14  class likelihoods
			//
			Double cl = getClassDistribution().getCount( label ).doubleValue(); //.getHistogram().get( label );
			log.debug( "class likelihood for class '" + label + "' is {} / {}", cl, getClassDistribution().getCount() );
			Double p_label = getClassDistribution().getHistogram().get( label ) / this.getClassDistribution().getCount();
			classLikeli.put( label, p_label );
			classLikeli.put( label, 1.0d );
		}


		//
		// compute the class likelihood for each class:
		//
		Double max = 0.0d;
		String maxClass = null;
		Double totalLikelihood = 0.0d;

		for( String clazz : classLikeli.keySet() ){

			Double likelihood = classLikeli.get( clazz );

			for( String attribute : item.keySet() ){

				if( !this.labelAttribute.equals( attribute ) ){

					Object value = item.get( attribute );
					if( value.getClass().equals( Double.class ) ){
						//
						// multiplying probability for double value
						//
						Distribution<Double> dist = (Distribution<Double>) distributions.get(clazz);
						likelihood *= dist.prob( (Double) value );

					} else {
						//
						// determine likelihood for nominal value
						//
						String feature = this.getNominalCondition( attribute, item);

						Double d = ((Distribution<String>) distributions.get( clazz )).getCount( feature ).doubleValue();
						Double total = this.getClassDistribution().getCount( clazz ).doubleValue();

						if( d == null || d == 0.0d ){
							d = laplaceCorrection;
							total += laplaceCorrection;
						}

						log.debug( "  likelihood for {}  is  {}  |" + clazz + " ", feature, d / total );
						likelihood *= ( d / total );
					}
				}
			}

			classLikeli.put( clazz, likelihood );
			totalLikelihood += likelihood;
		}


		// determine most likely class
		//
		for( String clazz : classLikeli.keySet() ){
			Double likelihood = classLikeli.get( clazz ) / totalLikelihood;
			log.debug( "probability for {} is {}", clazz, likelihood );
			if( maxClass == null || likelihood > max ){
				maxClass = clazz;
				max = likelihood;
			}
		}

		return maxClass;
	}


	public String getNominalCondition( String attribute, Data item ){
		return attribute + "='" + item.get( attribute ) + "'";
	}


	/**
	 * @see stream.learner.Learner#learn(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void learn(Data item) {

		//
		// determine the label attribute, if not already set
		//
		if( labelAttribute == null ){
			for( String name : item.keySet() )
				if( name.startsWith( "_class" ) ){
					labelAttribute = name;
					break;
				}
		}


		if( item.get( labelAttribute ) == null ){
			log.warn( "Not processing unlabeled data item {}", item );
			return;
		}

		String clazz = item.get( labelAttribute ).toString();
		log.debug( "Learning from example with label={}", clazz );
		if( this.classDistribution == null )
			this.classDistribution = new NominalDistributionModel<String>(); //this.createNominalDistribution();


		if( log.isDebugEnabled() ){
			log.debug( "Classes: {}", classDistribution.getElements() );
			for( String t : classDistribution.getElements() )
				log.debug( "    {}:  {}", t, classDistribution.getCount( t ) );
		}
		//
		// For learning we update the distributions of each attribute
		//
		for( String attribute : item.keySet() ){

			if( attribute.equalsIgnoreCase( labelAttribute ) ){
				//
				// adjust the class label distribution
				//
				classDistribution.update( clazz );

			} else {

				Object obj = item.get( attribute );

				if( obj.getClass().equals( Double.class ) ){
					Double value = (Double) obj;
					log.debug( "Handling numerical case ({}) with value  {}", obj, value );
					//
					// manage the case of an numerical attribute
					//
					Distribution<Double> numDist = (Distribution<Double>) distributions.get( attribute );
					if( numDist == null ){
						numDist = this.createNumericalDistribution();
						log.debug( "Creating new numerical distribution model for attribute {}", attribute );
						distributions.put( attribute, numDist );
					}
					numDist.update( value );

				} else {

					String value = this.getNominalCondition( attribute, item );
					log.debug( "Handling nominal case for [ {} | {} ]", value, "class=" + clazz );

					//
					// adapt the nominal distribution for this attribute
					//
					Distribution<String> nomDist = (Distribution<String>) distributions.get( clazz );
					if( nomDist == null ){
						nomDist = this.createNominalDistribution();
						log.debug( "Creating new nominal distribution model for attribute {}, {}", attribute, "class=" + clazz );
						distributions.put( clazz, nomDist );
					}
					nomDist.update( value );
				}
			}
		}
	}


	/**
	 * Returns the class distribution of the current state of the algorithm.
	 * 
	 * @return
	 */
	public Distribution<String> getClassDistribution(){
		if( classDistribution == null )
			classDistribution = createNominalDistribution();

		return this.classDistribution;
	}



	/**
	 * <p>
	 * Returns the set of numerical distributions of this model.
	 * </p>
	 * 
	 * @return The set of numerical distributions, currently known to this classifier.
	 */
	@SuppressWarnings("unchecked")
	public List<Distribution<Double>> getNumericalDistributions(){
		List<Distribution<Double>> numDists = new ArrayList<Distribution<Double>>();
		for( Distribution<?> d : distributions.values() ){
			if( d instanceof NumericalDistributionModel )
				numDists.add( (Distribution<Double>) d );
		}

		return numDists;
	}


	/**
	 * <p>
	 * This method creates a new distribution model for nominal values. It can be overwritten
	 * by subclasses to make use of a more sophisticated/space-limited assessment of nominal
	 * distributions.
	 * </p>
	 * 
	 * @return A new, empty distribution model.
	 */
	public Distribution<String> createNominalDistribution(){
		return new NominalDistributionModel<String>();
	}


	/**
	 * <p>
	 * This method creates a new distribution model for numerical data.
	 * </p>
	 * 
	 * @return A new, empty distribution model.
	 */
	public Distribution<Double> createNumericalDistribution(){
		return new NumericalDistributionModel( 1000, 1.0 );
	}
}