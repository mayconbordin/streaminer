package org.streaminer.stream.classifier.tree;

import java.io.Serializable;


/**
 * <p>
 * This class implements a simple binary tree node.
 * </p>
 * 
 * @author Matthias Balke, Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 *
 */
public class BTreeNode 
	extends BinaryTreeNode<RegressionTreeStatistics,Double>
	implements Serializable 
{

	private static final long serialVersionUID = -3488366594443164439L;

	/* The statistics of the samples with  sample.y <= value  */
	RegressionTreeStatistics lower = new RegressionTreeStatistics();

	/* The statistics of the samples with  sample.y  > value  */
	RegressionTreeStatistics upper = new RegressionTreeStatistics();
	


	/**
	 * constructs new BTreeNode object as root
	 * @param feature example feature associated with this BTree
	 * @param value value of associated feature
	 */
	public BTreeNode(String feature, Double value){
		super( feature, null );
		this.value = value;
		RegressionTreeStatistics statistics = new RegressionTreeStatistics();
		setNodeInfo( statistics );
	}

	/**
	 * constructs new BTreeNode object as leaf node
	 * @param feature example feature associated with this BTree
	 * @param value value of associated feature
	 * @param parent parent node of this new node
	 * @param isRightChild true if new node is the right child of its parent / if value is greater then the parents value
	 * @param gamma value of target feature
	 */
	public BTreeNode(String feature, Double value, BTreeNode parent ){
		super( feature, parent );
		RegressionTreeStatistics stats = new RegressionTreeStatistics();
		setNodeInfo( stats );
	}


	/**
	 * This method simply ensures that each node delivers a non-null
	 * node information object.
	 */
        @Override
	public RegressionTreeStatistics getNodeInfo(){
		RegressionTreeStatistics st = super.getNodeInfo();
		if( st == null ){
			st = new RegressionTreeStatistics();
			setNodeInfo( st );
		}
		return st;
	}

	
	public void insert( Double value ){
		getNodeInfo().update( value );
	
		int cmp = value.compareTo( this.value );
		if( cmp == 0 )
			return;
		
		if( cmp < 0 ){
			
			BTreeNode left = (BTreeNode) getLeft();
			if( left != null ){
				left.getNodeInfo().update( value );
			} else {
				setLeft( new BTreeNode( getName(), value, this ) );
			}
			
		} else {

			BTreeNode right = (BTreeNode) getRight();
			if( right != null ){
				right.getNodeInfo().update( value );
			} else {
				setRight( new BTreeNode( getName(), value, this ) );
			}
		}
	}
	
	
	/**
	 * @return the feature
	 */
	public String getFeature() {
		return getName();
	}
	
	
	public RegressionTreeStatistics getLowerStatistics(){
		return this.lower;
	}

	
	public RegressionTreeStatistics getUpperStatistics(){
		return this.upper;
	}
	
	public Double getStandardDeviationReduction(){
		Double sdr = 0.0d;
		Double sdT = getNodeInfo().getStandardDeviation();
		Double t = getNodeInfo().getNumberOfExamples();
		
		Double t1 = getLowerStatistics().getNumberOfExamples();
		Double t2 = getLowerStatistics().getNumberOfExamples();
		
		sdr = sdT -  ( t1 / t ) * getLowerStatistics().getStandardDeviation() -  (t2 / t) * getUpperStatistics().getStandardDeviation();
		return sdr;
	}
}