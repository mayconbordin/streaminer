/**
 * 
 */
package org.streaminer.stream.classifier.tree;

import java.io.Serializable;


/**
 * <p>
 * This class is a simple implementation of a binary tree node. The class
 * provides a node-info attribute which can be used to associated user-defined
 * node information with each node.
 * </p>
 * <p>
 * In addition to that, the nodes can be labeled with a string identifier.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 *
 */
public class BinaryTreeNode<I extends NodeInfo,V>
implements Serializable 
{
	/** The unique class ID */
	private static final long serialVersionUID = -1888232699524432103L;

	/* A (optional) name/label for this node */
	String name;

	/* The parent node of this instance */
	BinaryTreeNode<I,V> parent = null;

	/* The left child of this node */
	private BinaryTreeNode<I,V> left;

	/* The right child of this node */
	private BinaryTreeNode<I,V> right;

	I nodeInfo = null;

	V value = null;


	public BinaryTreeNode( String name, BinaryTreeNode<I,V> parent ){
		this.name = name;
		this.parent = parent;
		this.left = null;
		this.right = null;
	}


	public BinaryTreeNode( String name, BinaryTreeNode<I,V> leftChild, BinaryTreeNode<I,V> rightChild ){
		this( name, null );
		setLeft( leftChild );
		setRight( rightChild );
	}


	/**
	 * @return the value
	 */
	public V getValue() {
		return value;
	}


	/**
	 * @param value the value to set
	 */
	public void setValue(V value) {
		this.value = value;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the left
	 */
	public BinaryTreeNode<I,V> getLeft() {
		return left;
	}


	/**
	 * @return the parent
	 */
	public BinaryTreeNode<I,V> getParent() {
		return parent;
	}


	/**
	 * @param parent the parent to set
	 */
	public void setParent(BinaryTreeNode<I,V> parent) {
		this.parent = parent;
	}


	/**
	 * @param left the left to set
	 */
	public void setLeft(BinaryTreeNode<I,V> left) {
		this.left = left;
		this.left.parent = this;
	}


	/**
	 * @return the right
	 */
	public BinaryTreeNode<I,V> getRight() {
		return right;
	}


	/**
	 * @param right the right to set
	 */
	public void setRight(BinaryTreeNode<I,V> right) {
		this.right = right;
		this.right.parent = this;
	}


	/**
	 * Returns the node information associated with this node.
	 * 
	 * @return
	 */
	public I getNodeInfo(){
		return nodeInfo;
	}


	/**
	 * Sets the node information to be associated with this node.
	 * 
	 * @param nodeInfo
	 */
	public void setNodeInfo( I nodeInfo ){
		this.nodeInfo = nodeInfo;
	}


	public boolean isLeaf(){
		return right == null && left == null;
	}


	public boolean isRightChild(){
		return parent != null && parent.getRight() == this;
	}

	public boolean isLeftChild(){
		return parent != null && parent.getLeft() == this;
	}


	/**
	 * Implementation of an in-order traversal following the visitor
	 * pattern.
	 * 
	 * @param visitor
	 */
	public void inOrder( Visitor<BinaryTreeNode<I,V>> visitor ){

		if( getLeft() != null )
			visitor.visit( getLeft() );
		
		visitor.visit( this );

		if( getRight() != null )
			visitor.visit( getRight() );
	}


	/**
	 * @param blanks number of blanks to insert
	 * @return string representation of this BTreeNode
	 */
	public String toString(int blanks){
		StringBuffer out = new StringBuffer();
		int length = 12 + name.length() + blanks;
		if( getRight() != null){
			out.append( getRight().toString(length));
		}
		out.append(RegressionTreeNode.LINE_SEPATATOR);
		for(int i = 0; i < blanks; i++){
			out.append(" ");
		}
		if(parent != null){
			out.append("-- ");
		}
		out.append(" ( " + getNodeInfo() +" ) ");
		out.append(RegressionTreeNode.LINE_SEPATATOR);
		if( getLeft() != null){
			out.append( getLeft().toString(length) );
		}
		return out.toString();
	}
}