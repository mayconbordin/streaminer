package org.streaminer.stream.classifier.tree;

import org.streaminer.stream.data.Data;
import org.streaminer.stream.learner.AbstractRegressor;
import org.streaminer.stream.learner.LearnerUtils;
import org.streaminer.stream.learner.Regressor;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class implements a regression tree. A regression trees is a model tree, i.e.
 * an induced decision tree with prediction models at its leafs. In case of the regression
 * tree these prediction models are simply regression models.
 * </p>
 * 
 * @author Mattias Balke, Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 *
 */
public class RegressionTree 
	extends AbstractRegressor<Data> {

	/** The unique class ID */
	private static final long serialVersionUID = -2683782830606679008L;

	static final transient Logger log = LoggerFactory.getLogger(RegressionTree.class);


	/** The resulting RegressionTree which is incrementally updated  */
	final RegressionTreeModel tree;


	/** possible split point    */
	BTreeNode possibleSplitpoint;

	/**  true if possible split point fulfils chernoff bound  */
	boolean splitPossible;

	/** epsilon, computed by chernoff bound   */
	double epsilon;

	/** value for delta error in chernoff bound */
	final double delta;


	/**
	 *
	 * @param targetValue
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public RegressionTree(Double delta, Regressor<Data> regression) throws Exception{
		//	  FIXME warning: removed by balke: because it's never used
		//	    this.linearRegressionClass = linearRegressionClass;
		//		this.parameters = parameters;
		this.tree = new RegressionTreeModel(regression);
		this.possibleSplitpoint = null;
		this.delta = delta;
	}

	@Override
	public RegressionTreeModel getModel() {
		return this.tree;
	}

	@Override
	public void learn(Data item) {
		LeafNode leaf = tree.getLeaf(item);
		leaf.getRegressionModel().learn( item );
		leaf.updateBTrees(item);
		this.computeChernoffBound(leaf, item);
		if(this.checkSplitpoints(leaf, item) != null){
			try{
				this.doSplit(leaf);
			}catch(Exception e){} // can not occur, same exception would have been thrown in constructor
		}
	}

	protected BTreeNode checkSplitpoints(LeafNode leaf, Data item ){
		BTreeNode possibleSplitpoint = null;
		
		for(String feature : LearnerUtils.getAttributes( item ) ){
			BTreeNode root = leaf.getBTrees().get(feature);
			double preSd = root.getNodeInfo().getStandardDeviation(); // computeSD(root.getLeqElements() + root.getGreaterElements(), root.getLeqDeltaSum() + root.getGreaterDeltaSum(), root.getLeqSquaredDeltaSum() + root.getGreaterSquaredDeltaSum());

			if( preSd > 0 && possibleSplitpoint != null ){
				
				Double sdr = root.getStandardDeviationReduction();
				if( possibleSplitpoint != null && sdr > possibleSplitpoint.getStandardDeviationReduction() ){
					
					if( sdr - possibleSplitpoint.getStandardDeviationReduction() - epsilon >=  0 ){
					}
				}
				
				computeSDRs(root, preSd);
				
			}
		}
		
		if( possibleSplitpoint != null ){
			return possibleSplitpoint;
		}

		return null;
	}

	protected void computeSDRs(BTreeNode node, double preSd ){ //, double leqParentElements, double leqParentSum, double leqParentSquaredSum, double greaterParentElements, double greaterParentSum, double greaterParentSquaredSum, double preSd){
		if(preSd > 0){
			double sdr = node.getStandardDeviationReduction();

			if (this.possibleSplitpoint != null) {
				if (sdr > this.possibleSplitpoint.getStandardDeviationReduction() ) {
					if ((sdr - this.possibleSplitpoint.getStandardDeviationReduction() - this.epsilon) >= 0) {
						this.splitPossible = true;
					}
					this.possibleSplitpoint = node;
				} else {
					if ((this.possibleSplitpoint.getStandardDeviationReduction() - sdr - this.epsilon) < 0) {
						this.splitPossible = false;
					}
				}
			}else{
				this.possibleSplitpoint = node;
			}
		}
	}

	/*
	private double computeSD(double n, double deltaSum, double squaredDeltaSum){
		double tmp = 1/n *(squaredDeltaSum - 1/n * Math.pow(deltaSum, 2));
		if(tmp < 0){
			return 0.0;
		}
		return Math.sqrt(tmp);
	}

	private double computeSDR(double preSd, double leqElements, double leqSd, double greaterElements, double greaterSd){
		double n = leqElements + greaterElements;
		double leq = leqElements / n * leqSd;
		double greater = greaterElements / n * greaterSd;
		return preSd - (leq + greater);
	}
	 */

	protected void doSplit(LeafNode leaf) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		System.out.println("perform split");
		InnerNode splitPoint = new InnerNode(this.possibleSplitpoint.getName(), this.possibleSplitpoint.getValue(), leaf.getRegressionModel(), leaf.getN());
		InnerNode parent = (InnerNode)leaf.getParent();
		if(parent != null){
			if(leaf.isRightChild()){
				parent.setRightChild(splitPoint);
			}else{
				parent.setLeftChild(splitPoint);
			}
		}else{
			this.tree.setRoot(splitPoint);
		}
	}

	protected void computeChernoffBound(LeafNode leaf, Data item){
		log.debug( "call predict: {}", item );
		double prediction = (Double) leaf.getRegressionModel().predict( item );
		this.epsilon = Math.sqrt(Math.abs(3*prediction/leaf.getN()*Math.log(2/this.delta)));
	}


	/**
	 * @see stream.learner.AbstractRegressor#predict(java.lang.Object)
	 */
	@Override
	public Double predict(Data item) {
		return tree.predict( item );
	}
}