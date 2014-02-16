/**
 * 
 */
package org.streaminer.stream.classifier.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


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
public class TreeNode<I extends Serializable>
	implements Serializable 
{
	/** The unique class ID */
	private static final long serialVersionUID = -1888232699524432103L;

	/* A (optional) name/label for this node */
	String name;
	
	/* The value of the attribute determined by this tree node's name */
	Object value;
	
	/* The parent node of this instance */
	TreeNode<I> parent = null;
	
	List<TreeNode<I>> children = new ArrayList<TreeNode<I>>();
	
	I nodeInfo = null;
	
	
	public TreeNode( String name, Object value, TreeNode<I> parent ){
		this.name = name;
		this.value = value;
		this.parent = parent;
		this.children = new ArrayList<TreeNode<I>>();
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
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}


	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}


	/**
	 * @return the parent
	 */
	public TreeNode<I> getParent() {
		return parent;
	}


	/**
	 * @param parent the parent to set
	 */
	public void setParent(TreeNode<I> parent) {
		this.parent = parent;
	}

	
	/**
	 * @return the children
	 */
	public List<TreeNode<I>> getChildren() {
		return children;
	}



	/**
	 * @param children the children to set
	 */
	public void setChildren(List<TreeNode<I>> children) {
		this.children = children;
		for( TreeNode<I> n : this.children )
			n.setParent( this );
	}

	
	public void add( TreeNode<I> child ){
		child.setParent( this );
		children.add( child );
	}

	
	public void insert( int idx, TreeNode<I> child ){
		child.setParent( this );
		children.add( idx, child );
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
		return children == null || children.isEmpty();
	}
}