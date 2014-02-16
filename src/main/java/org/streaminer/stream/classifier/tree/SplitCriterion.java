/**
 * 
 */
package org.streaminer.stream.classifier.tree;

/**
 * <p>
 * This interface defines the split criterion for a decision tree. The criterion
 * is based on sufficient statistics, which may differ depending on the concrete
 * implementation of this interface.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 *
 */
public interface SplitCriterion<S> {

	/**
	 * Uses the given statistics to computes whether a split is required/supported
	 * or not. 
	 * 
	 * @param statistics The statistics gathered to determine the split decision.
	 * @return
	 */
	public boolean requiresSplit( S statistics );
}
