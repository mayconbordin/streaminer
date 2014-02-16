/**
 * 
 */
package org.streaminer.stream.classifier.tree;

import org.streaminer.stream.data.Data;
import org.streaminer.stream.learner.LearnerUtils;
import org.streaminer.stream.learner.Regressor;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 *
 */
public class RTree extends ModelTree<RegressionTreeStatistics,Double> implements Regressor<Data> {
	
	/** The unique class ID */
	private static final long serialVersionUID = 4926545397273482368L;

	/* The logger for this class */
	static Logger log = LoggerFactory.getLogger( RTree.class );
	
	/* The split criterion for this model tree */
	SplitCriterion<RegressionTreeStatistics> splitCriterion;
	
	/* One binary tree for each numerical attribute */
	Map<String,BTreeNode> btrees = new HashMap<String,BTreeNode>();
	
	BestSplitValueFinder splitValueFinder = new BestSplitValueFinder();
	
	/**
	 * @param name
	 * @param parent
	 */
	public RTree(String name, TreeNode<RegressionTreeStatistics> parent) {
		super(name, parent, new ChernoffSplitCriterion<RegressionTreeStatistics>() );
	}
	

	/**
	 * @see stream.learner.AbstractClassifier#learn(java.lang.Object)
	 */
	@Override
	public void learn(Data item) {
		
		// find the leaf node for the given data item and update the
		// model within that leaf
		//
		ModelTree<RegressionTreeStatistics,Double> leaf = getLeaf( item );
		leaf.getModel().learn( item );

		//
		// update the node statistics, which determine whether to split this
		// node or not
		//
		if( LearnerUtils.isNumerical( getName(), item ) ){
			Double value = LearnerUtils.getDouble( getName(), item );
			leaf.getNodeInfo().update( value );

			BTreeNode btree = btrees.get( leaf.getName() );
			if( btree == null ){
				btree = new BTreeNode( leaf.getName(), value );
				btrees.put( leaf.getName(), btree );
			} else 
				btree.insert( value );
			
			
		} else
			throw new RuntimeException( "Nominal values are not supported!" );
			//leaf.getNodeInfo().update( item.get( getName() ).toString() );
		
		//
		// compute the chernoff bound based on the node-statistics
		//
		
		
		boolean requiresSplit = splitCriterion.requiresSplit( leaf.getNodeInfo() );
		Double splitValue = getBestSplitValue( btrees.get( leaf.getName() ) );
		
		if( requiresSplit ){
			//
			// split this leaf and create siblings
			//
			ModelTree<RegressionTreeStatistics,Double> parent = (ModelTree<RegressionTreeStatistics,Double>) leaf.getParent();
			
			ModelTree<RegressionTreeStatistics,Double> replacement = new ModelTree<RegressionTreeStatistics,Double>( leaf.getName(), parent, splitCriterion );
			replacement.add( new ModelTree<RegressionTreeStatistics,Double>( leaf.getName(), null, splitCriterion ) );
			
		} else {
			//
			// update this leaf's statistics and train the associated model 
			//
			//btrees.get( leaf.getName() ).getNodeInfo().update( value );
		}
	}

	
	public Double getBestSplitValue( BTreeNode btree ){
		splitValueFinder.reset();
		btree.inOrder( (Visitor<BinaryTreeNode<RegressionTreeStatistics,Double>>) splitValueFinder );
		log.info( "Best split value is: {}  (SDR: {})", splitValueFinder.getValue(), splitValueFinder.getMaximum() );
		return splitValueFinder.getValue();
	}
	
	
	/**
	 * @see stream.learner.AbstractClassifier#predict(java.lang.Object)
	 */
	@Override
	public Double predict(Data item) {
		ModelTree<RegressionTreeStatistics,Double> leaf = getLeaf( item );
		return leaf.predict( item );
	}

	
	/**
	 * @see stream.learner.Learner#init()
	 */
	@Override
	public void init() {
	}

	
	
	/**
	 * This class is a visitor for tree nodes and maintains a maximum standard
	 * deviation reduction during its visits.
	 * 
	 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
	 *
	 */
	class BestSplitValueFinder implements Visitor<BinaryTreeNode<RegressionTreeStatistics,Double>> {
		
		Double maxValue = null;
		Double maxSdr = Double.NEGATIVE_INFINITY;
		
		
		public void reset(){
			maxValue = null;
			maxSdr = Double.NEGATIVE_INFINITY;
		}
		
		public Double getValue(){
			return maxValue;
		}
		
		public Double getMaximum(){
			return maxSdr;
		}
		
		/**
		 * @see stream.learner.tree.Visitor#visit(stream.learner.tree.BinaryTreeNode)
		 */
		@Override
		public void visit( BinaryTreeNode<RegressionTreeStatistics,Double> node) {
			
			if( maxValue == null ){
				maxValue = node.getValue();
				log.info( "Found initial split value: {}  (sdr: {})", maxValue, maxSdr );
				return;
			}
			
			Double sdr = getStandardDeviationReduction( node );
			if( sdr > maxSdr ){
				maxValue = node.getValue();
				maxSdr = sdr;
				log.info( "Found new best split value: {}  (sdr: {})", maxValue, maxSdr );
			}
		}
		

		public Double getStandardDeviationReduction( BinaryTreeNode<RegressionTreeStatistics,Double> node ){
			Double sdr = 0.0d;
			Double sdT = node.getNodeInfo().getStandardDeviation();
			Double t = node.getNodeInfo().getNumberOfExamples();
			
			Double t1 = 0.0d;
			Double sdT1 = 0.0d;
			if( node.getLeft() != null ){
				t1 = node.getLeft().getNodeInfo().getNumberOfExamples();
				sdT1 = node.getLeft().getNodeInfo().getStandardDeviation();
			}
			
			Double t2 = 0.0d;
			Double sdT2 = 0.0d;
			if( node.getRight() != null ){
				t2 = node.getRight().getNodeInfo().getNumberOfExamples();
				sdT2 = node.getRight().getNodeInfo().getStandardDeviation();
			}
			
			sdr = sdT -  ( t1 / t ) * sdT1 -  (t2 / t) * sdT2;
			return sdr;
		}
	}
}