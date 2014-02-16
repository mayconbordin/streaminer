/**
 * 
 */
package org.streaminer.stream.classifier;

import org.streaminer.stream.learner.LearnerUtils;
import org.streaminer.stream.data.Data;
import org.streaminer.stream.model.Distribution;
import org.streaminer.stream.model.NominalDistributionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 *
 */
public class MajorityClass extends AbstractClassifier<Data, String> {

	/** The unique class ID */
	private static final long serialVersionUID = 2843128554725059166L;
	
	static Logger log = LoggerFactory.getLogger( MajorityClass.class );

	Distribution<String> dist = new NominalDistributionModel<String>();
	
	String labelAttribute = null;
	
	/**
	 * @see stream.learner.AbstractClassifier#learn(java.lang.Object)
	 */
	@Override
	public void learn(Data item) {
		
		if( labelAttribute == null ){
			labelAttribute = LearnerUtils.detectLabelAttribute( item );
				
		}
		
		if( labelAttribute == null ){
			log.warn( "Ignoring unlabeled example (no label defined for MajorityClass learner)!" );
			return;
		}
		
		dist.update( item.get( labelAttribute ).toString() );
	}

	
	/**
	 * @see stream.learner.AbstractClassifier#predict(java.lang.Object)
	 */
	@Override
	public String predict(Data item) {
		
		if( labelAttribute == null )
			log.error( "No label-attribute defined!" );
		
		if( dist.getCount() == 0 )
			return "?";
		
		
		String major = null;
		Integer max = null;
		
		for( String key : dist.getElements() ){
			if( major == null || dist.getCount( key ) > max ){
				major = key;
				max = dist.getCount( key );
			}
		}
		
		return major;
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
}