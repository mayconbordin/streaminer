/**
 * 
 */
package org.streaminer.stream.classifier.tree;

/**
 * <p>
 * This is the interface for the visitor pattern for trees.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 *
 */
public interface Visitor<B extends BinaryTreeNode<?,?>> {

	public void visit( B node );
}