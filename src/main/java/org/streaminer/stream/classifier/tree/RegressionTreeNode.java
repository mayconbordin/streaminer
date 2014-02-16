package org.streaminer.stream.classifier.tree;

import java.io.Serializable;

public abstract class RegressionTreeNode implements Serializable {

	/** The unique class ID */
	private static final long serialVersionUID = 7340528085839800436L;

	
	/**
	 * @param blanks number of blanks to insert
	 * @return a string representation of this node
	 */
	public abstract String toString(int blanks);
	
	public static final String LINE_SEPATATOR = System.getProperty("line.separator");
	
}