package org.streaminer.stream.classifier.tree;

import org.streaminer.stream.data.Data;
import org.streaminer.stream.learner.Regressor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LeafNode extends RegressionTreeNode implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
	 * root nodes of all BTrees for every remaining attribute to compute the
	 * standard deviation reduction
	 */
	private Map<String, BTreeNode> bTrees;
	
	
	/**
	 * learner for linear regression model
	 */
	private Regressor<Data> regressionModelLearner;
	
	/**
	 * parent of this node
	 */
	private RegressionTreeNode parent;
	
	/**
	 * true if node is right child of its parent
	 */
	private boolean isRightChild;
	
	/**
	 * number of elements that have passed this node
	 */
	private int n;	
	
	
	/**
	 * Constructs empty leaf element with the given regressor as its regression model.
	 */
	public LeafNode(RegressionTreeNode parent, boolean isRightChild, Regressor<Data> linearRegression, int n) {
		regressionModelLearner = linearRegression;
		
		bTrees = new HashMap<String, BTreeNode>();
		this.parent = parent;
		this.isRightChild = isRightChild;
		this.n = n;
	}
	
	/**
	 * @return number of element that have passed this node
	 */
	public int getN() {
		return n;
	}

	/**
	 * updates all BTrees in this leaf
	 * @param item example to learn with
	 */
	public void updateBTrees(Data item) {
		n++;
		/*
		for (String feature : item.getFeatures()) {
			Serializable value = item.get(feature);
			BTreeNode bTree = bTrees.get(feature);
			if (bTree != null) {
				if (item.get(feature) instanceof Number) {
					bTree.traverseNode(value,((Number) item.getLabel()).doubleValue());
				} else {
					bTree.updateNominal(value, ((Number) item.getLabel()).doubleValue());
				}
			} else {
				bTree = new BTreeNode(feature, value, (Number)item.getLabel());
				bTrees.put(feature, bTree);
			}

		}
		 */
	}

	/**
	 * @return regression model learner used in this node to compute regression model
	 */
	public Regressor<Data> getRegressionModel() {
		return regressionModelLearner;
	}

	/**
	 * @return map with all root nodes of BTrees located in this leaf node
	 */
	public Map<String, BTreeNode> getBTrees() {
		return bTrees;
	}

	/**
	 * @return parent node of this node if node is not root node, null otherwise
 	 */
	public RegressionTreeNode getParent() {
		return parent;
	}

	/**
	 * @return true if node is right child of its parent
	 */
	public boolean isRightChild() {
		return isRightChild;
	}

	/**
	 * sets the parent node of this node
	 * @param parent new parent node of this node
	 */
	public void setParent(RegressionTreeNode parent) {
		this.parent = parent;
	}
	
	
    /**
     * {@inheritDoc}
     */
	@Override
	public String toString(int blanks){
		StringBuffer out = new StringBuffer();
		for(int i = 0; i < blanks; i++){
			out.append(" ");
		}
		if(blanks > 0){
			out.append("-- ");
		}
		out.append("leafNode" + LINE_SEPATATOR);
		
		for(String feature : bTrees.keySet()){
			out.append(bTrees.get(feature).toString(blanks + 12));
			
//			double count = bTrees.get(feature).getLeqElements() + bTrees.get(feature).getGreaterElements();
//			out.append("(" + feature + ":" + Double.toString(count) + ") ");
		}
		return out.toString();
	}
}