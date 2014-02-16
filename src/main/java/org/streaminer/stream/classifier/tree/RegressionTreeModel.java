package org.streaminer.stream.classifier.tree;

import org.streaminer.stream.data.Data;
import org.streaminer.stream.learner.Regressor;
import org.streaminer.stream.model.PredictionModel;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class implements a regression tree model. A regression tree model is a
 * decision tree with regression models at its leaf nodes.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 * 
 */
public class RegressionTreeModel implements PredictionModel<Data, Double> {
	/** The unique class ID */
	private static final long serialVersionUID = 5489550422815750513L;

	static final transient Logger log = LoggerFactory
			.getLogger(RegressionTreeModel.class);

	/**
	 * root of regression tree
	 */
	private RegressionTreeNode root;

	/**
	 * constructs new regression model
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("unchecked")
	public RegressionTreeModel(Regressor<Data> regression) throws Exception {
		root = new LeafNode(null, false, regression, 0);
	}

	/**
	 * Returns the leaf at which the path through the tree ends for the
	 * specified {@link Example}.
	 * 
	 * @param item
	 * @return leaf if found, null instead
	 */
	public LeafNode getLeaf(Data item) {
		RegressionTreeNode currentNode = this.root;
		while (currentNode instanceof InnerNode) {
			InnerNode innerNode = (InnerNode) currentNode;
			currentNode = innerNode.traverseNode(item.get(innerNode
					.getFeature()));
		}
		if (currentNode instanceof LeafNode) {
			return (LeafNode) currentNode;
		}
		return null;
	}

	@Override
	public Double predict(Data item) {
		LeafNode leaf = this.getLeaf(item);
		if (leaf != null) {
			Double prediction = leaf.getRegressionModel().predict(item);
			return prediction;
		}
		return Double.NaN;
	}

	/**
	 * @return root element of regression tree
	 */
	public RegressionTreeNode getRoot() {
		return this.root;
	}

	/**
	 * sets root element of regression tree
	 * 
	 * @param root
	 *            RegressionTreeNode to be new root of regression tree
	 */
	public void setRoot(RegressionTreeNode root) {
		this.root = root;
	}

	/**
	 * @return String representation of this RegressionTreeModel
	 */
	@Override
	public String toString() {
		return this.root.toString(0);
	}
}