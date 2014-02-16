/**
 * 
 */
package org.streaminer.stream.classifier.tree;

import org.streaminer.stream.classifier.Classifier;
import org.streaminer.stream.data.Data;
import org.streaminer.stream.learner.LearnerUtils;
import org.streaminer.stream.model.PredictionModel;


/**
 * <p>
 * This class implements a general model tree, which basically is a decision tree
 * with prediction models at its leaf nodes.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 *
 */
public class ModelTree<I extends NodeInfo,O> 
	extends TreeNode<I>
	implements PredictionModel<Data,O> 
{
	/** The unique class ID */
	private static final long serialVersionUID = 4855897886594236180L;

	
	Classifier<Data,O> model;
	
	SplitCriterion<I> splitCrierion;

	/**
	 * @param name
	 * @param parent
	 */
	public ModelTree(String name, TreeNode<I> parent, SplitCriterion<I> splitCrit ) {
		super(name, null, parent);
		splitCrierion = splitCrit;
	}
	

	/**
	 * @return the model
	 */
	public Classifier<Data, O> getModel() {
		return model;
	}


	/**
	 * @param model the model to set
	 */
	public void setModel(Classifier<Data, O> model) {
		this.model = model;
	}


	/**
	 * @see stream.model.PredictionModel#predict(java.lang.Object)
	 */
	@Override
	public O predict(Data item) {
		
		ModelTree<I,O> leaf = getLeaf( item );
		if( leaf != null )
			return leaf.model.predict(item);

		return null;
	}
	

	
	/**
	 * This method traverses the tree by testing the given data item at each
	 * inner node until reaching a leaf.
	 * 
	 * @param item
	 * @return
	 */
	public ModelTree<I,O> getLeaf( Data item ){
		
		if( this.isLeaf() )
			return this;
		
		if( this.value == null )
			throw new RuntimeException( "Weird error! This node is not a leaf, but also does not contain a threshold value!" );

		if( LearnerUtils.isNumerical( getName(), item ) ){
			//
			// This checks for the best matching successor based on numerical
			// intervals obtained from each sibling
			//
			Double val = LearnerUtils.getDouble( getName(), item );
			ModelTree<I,O> child = getChildFor( val );
			if( child != null )
				return child.getLeaf( item );
			
		} else {
			// check for matching child on nominal attribute value
			//
			ModelTree<I,O> child = getChildFor( item.get( getName() ).toString() );
			if( child != null )
				return child.getLeaf( item );
		}
		
		return null;
	}
	
	
	/**
	 * This method checks all siblings of the current node and returns the
	 * successor that matches the given value. This method handles the case
	 * of a real-valued condition.
	 * 
	 * @param val
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ModelTree<I,O> getChildFor( Double val ){
		//
		// the very first sibbling corresponds to  ] -infinity; value ]
		//
		Double lower = Double.NEGATIVE_INFINITY;
		
		for( int i = 0; i < children.size(); i++ ){
			ModelTree<I,O> child = (ModelTree<I,O>) children.get(i);
			Double upper = (Double) child.value;
			if( lower < val && val <= upper ){
				return child;
			} else {
				lower = upper;
			}
		}
		return null;
	}
	
	
	/**
	 * This method returns the child for the given nominal value.
	 * 
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ModelTree<I,O> getChildFor( String value ){
		//
		// check for matching child on nominal attribute value
		//
		for( int i = 0; i < children.size(); i++ ){
			ModelTree<I,O> child = (ModelTree<I,O>) children.get(i);
			if( child.value.equals( value ) )
				return child;
		}
		return null;
	}
}