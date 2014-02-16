package org.streaminer.stream.classifier.tree;

import org.streaminer.stream.data.Data;
import org.streaminer.stream.learner.Regressor;
import java.io.Serializable;


public class InnerNode extends RegressionTreeNode implements Serializable {

	private static final long serialVersionUID = -7739559317623140189L;

	/**
	 * feature associated with this node
	 */
	private final String feature;

	/**
	 * value of associated feature
	 */
	private final Serializable value;

	/**
	 * left child of the node
	 */
	private RegressionTreeNode leftChild;

	/**
	 * right child of the node
	 */
	private RegressionTreeNode rightChild;


	public InnerNode(String feature, Serializable value, Regressor<Data> linearRegression, int n){
		this.feature = feature;
		this.value = value;
		this.leftChild = new LeafNode(this, false, linearRegression, n);
		this.rightChild = new LeafNode(this, true, linearRegression, n);
	}

	/**
	 * traverses node to find matching leaf node
	 * @param value a valid value for the associated feature
	 * @return next node following the path of the given value, null if node does not exists
	 */
	public RegressionTreeNode traverseNode(Serializable value){
		if(value instanceof Number && this.value instanceof Number){
			Double number = ((Number) value).doubleValue();
			return this.traverseNumericalNode(number);
		}else{
			return this.traverseNominalNode(value);
		}
	}

	/**
	 * traverses numerical node to find matching leaf node
	 * @param value a valid value for the associated feature
	 * @return next node following the path of the given value, null if node does not exists
	 */
	private RegressionTreeNode traverseNumericalNode(Double v) {
		double val = (Double) this.value;
		if(v.compareTo(val) <= 0){
			return this.leftChild;
		}else{
			return this.rightChild;
		}
	}

	/**
	 * traverses nominal node to find matching leaf node
	 * @param value a valid value for the associated feature
	 * @return next node following the path of the given value, null if node does not exists
	 */
	private RegressionTreeNode traverseNominalNode(Serializable value) {
		if(value.equals(this.value)){
			return this.leftChild;
		}else{
			return this.rightChild;
		}
	}


	/**
	 * @return name of feature associated with this node
	 */
	public String getFeature() {
		return this.feature;
	}

	/**
	 * @return left child of this node
	 */
	public RegressionTreeNode getLeftChild() {
		return this.leftChild;
	}

	/**
	 * @return right child of this node
	 */
	public RegressionTreeNode getRightChild() {
		return this.rightChild;
	}

	/**
	 * sets new left child of this node
	 * @param leftChild RegressionTreeNode to be left child of this node
	 */
	public void setLeftChild(RegressionTreeNode leftChild) {
		this.leftChild = leftChild;
	}

	/**
	 * sets new right child of this node
	 * @param rightChild RegressionTreeNode to be right child of this node
	 */
	public void setRightChild(RegressionTreeNode rightChild) {
		this.rightChild = rightChild;
	}

	@Override
	public String toString(int blanks){
		StringBuffer out = new StringBuffer();
		int length = 8 + this.feature.length() + this.value.toString().length() + blanks;
		out.append(this.rightChild.toString(length + 4));
		out.append(LINE_SEPATATOR);
		for(int i = 0; i < blanks; i++){
			out.append(" ");
		}
		if(blanks > 0){
			out.append("-- ");
		}
		String compare;
		if(this.value instanceof Number){
			compare = "<=";
		}else{
			compare = "=";
		}
		out.append("("+this.feature+ compare +this.value.toString()+")");
		out.append(LINE_SEPATATOR);
		out.append(this.leftChild.toString(length + 4));
		return out.toString();
	}
}