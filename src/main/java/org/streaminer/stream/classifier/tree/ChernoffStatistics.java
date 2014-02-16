/**
 * 
 */
package org.streaminer.stream.classifier.tree;

/**
 * <p>
 * This interface defines the statistical measures required to determine the
 * chernoff bound on a node.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public interface ChernoffStatistics {

	/**
	 * The number <code>n</code> of example, observed.
	 * 
	 * @return
	 */
	public Double getNumberOfExamples();
	
	
	/**
	 * The measured mean as obtained from the observed examples.
	 * 
	 * @return
	 */
	public Double getMean();
	
	
	/**
	 * 
	 * @return
	 */
	public Double getEpsilon();
}
