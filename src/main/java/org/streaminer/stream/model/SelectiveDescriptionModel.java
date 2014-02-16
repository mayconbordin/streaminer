package org.streaminer.stream.model;

/**
 * <p>
 * An output extension to the model interface which returns
 * a a description of the data. E.g. statistics, frequent items, etcpp.
 * </p>
 * 
 * <p>
 * The difference between selective and non-selective description
 * models is, that you can specify a parameter at request time for
 * selective description.
 * </p>
 * 
 * <p>
 * For a non-selective descriptive model see {@link DescriptionModel}.
 * </p>
 * 
 * @author Marcin Skirzynski
 *
 */
public interface SelectiveDescriptionModel<T, R> extends Model {

	/**
	 * <p>
	 * This method returns a description for the given input.
	 * </p>
	 * 
	 * @param parameter		a selective parameter for the description
	 * @return	a description of the data
	 */
	R describe( T parameter );
	
}
