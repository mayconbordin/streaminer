package org.streaminer.stream.eval;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>This is a data structure for dealing with label-pair-relations. For statistical
 * information on a per-label-base there are methods which return instance(s) of
 * {@link TableOfConfusion}. That class has methods to calculate values like Precision, Recall, [...].</p>
 *
 * <p>Look at http://en.wikipedia.org/wiki/Confusion_matrix for detailed descriptions.</p>
 *
 * <p>Although there is a method for adding a label or a list of labels to the existing labels, it is highly recommended
 * to use it as rare as possible, as it requires that (internally) the array storing the label-combination-counts has to 
 * be copied to a new array which then replaces the old array. Instead, use the constructor with a list of labels as argument,
 * where that list should contain as much labels as possible so that the need for adding labels is at a minimum frequency.
 * However, if the labels are not known in advance, using {@link #addLabels(java.util.List)} is preferable to
 * {@link #addLabel(java.lang.Object)} as there are fewer array copy operations needed with the first mentioned method.</p>
 *
 * @param T the type of the labels
 * @author Benedikt Kulmann, Lukas Kalabis
 * @see TableOfConfusion
 */
public final class ConfusionMatrix<T> {

	/**
	 * The list of all labels. Indices refer to the {@link #confusionMatrix}.
	 */
	private List<T> labels;

	/**
	 * <p>Array which stores the counts of classification instances (pairs of true and predicted labels)</p>
	 *
	 * <p>The first dimension represents the true labels while the second dimension represents the predicted labels.</p>
	 */
	private long[][] confusionMatrix;

	/**
	 * Creates a new ConfusionMatrix instance with an empty list of labels. Don't use
	 * this constructor if it is possible to construct a list of possible labels in advance.
	 */
	public ConfusionMatrix() {
		this(new ArrayList<T>());
	}

	/**
	 * Creates a new ConfusionMatrix instance.
	 *
	 * @param labels Labels to maintain a label-pair-combination-counter for.
	 */
	public ConfusionMatrix(List<T> labels) {
		this.labels = labels;
		this.confusionMatrix = new long[labels.size()][labels.size()];
	}

	/**
	 * <p>Adds the provided label to the list of labels. Afterwards it is necessary to create
	 * a new array for the internal counters so the usage of this method is expensive. Try to
	 * add as much labels as possible at a time by using {@link #addLabels(java.util.List)} or,
	 * which is even better, at object creation time.</p>
	 *
	 * <p>Duplicates are ignored.</p>
	 *
	 * @param additionalLabel The label to add to the internal list of labels
	 */
	public void addLabel(T additionalLabel) {
		final List<T> additionalLabelAsList = new ArrayList<T>();
		additionalLabelAsList.add(additionalLabel);
		addLabels(additionalLabelAsList);
	}

	/**
	 * <p>Adds the provided list of labels to the internal list of labels. Afterwards it is necessary to create
	 * a new array for the internal counters so the usage of this method is expensive. Try to
	 * add as much labels as possible at object creation time.</p>
	 *
	 * <p>Duplicates are ignored.</p>
	 *
	 * @param additionalLabels The labels to add to the internal list of labels
	 */
	public void addLabels(List<T> additionalLabels) {
		// construct new list of labels
		final List<T> modAdditionalLabels = new ArrayList<T>(additionalLabels);
		modAdditionalLabels.removeAll(labels);
		labels.addAll(modAdditionalLabels);

		// construct new confusion matrix
		final long[][] newConfusionMatrix = new long[labels.size()][labels.size()];
		for(int i=0; i<confusionMatrix.length; i++) {
			System.arraycopy(confusionMatrix[i], 0, newConfusionMatrix[i], 0, confusionMatrix.length);
		}
		confusionMatrix = newConfusionMatrix;
	}

	/**
	 * Returns the list of labels this {@link ConfusionMatrix} maintains counters for.
	 * @return The list of labels this {@link ConfusionMatrix} maintains counters for.
	 */
	public List<T> getLabels() {
		return labels;
	}

	/**
	 * <p>Adds a classification instance (true and predicted label) to this {@link ConfusionMatrix}.</p>
	 *
	 * <p>Each label which didn't exist previously will be added within this method automatically (which is
	 * expensive and to be avoided).</p>
	 *
	 * @param truth The true label
	 * @param prediction The predicted label
	 */
	public void add(T truth, T prediction) {
		int indexOfTruth = labels.indexOf(truth);
		if(indexOfTruth == -1) {
			indexOfTruth = labels.size();
			addLabel(truth);
		}

		int indexOfPrediction = labels.indexOf(prediction);
		if(indexOfPrediction == -1) {
			indexOfPrediction = labels.size();
			addLabel(prediction);
		}

		confusionMatrix[indexOfTruth][indexOfPrediction]++;
	}

	/**
	 * Returns a map which contains a {@link TableOfConfusion} per label.
	 *
	 * @return a map of {@link TableOfConfusion} instances
	 * @see #getTableOfConfusion(java.lang.Object)
	 */
	public Map<T, TableOfConfusion> getTablesOfConfusion() {
		final Map<T, TableOfConfusion> tablesOfConfusion = new HashMap<T, TableOfConfusion>();
		for(T label : labels) {
			tablesOfConfusion.put(label, getTableOfConfusion(label));
		}
		return tablesOfConfusion;
	}

	/**
	 * Constructs and returns the {@link TableOfConfusion} for the provided label.
	 *
	 * @param label The label to construct a {@link TableOfConfusion} for
	 * @return A {@link TableOfConfusion} instance for the provided label
	 *
	 * @see TableOfConfusion
	 */
	public TableOfConfusion getTableOfConfusion(T label) {
		final TableOfConfusion tableOfConfusion = new TableOfConfusion();
		tableOfConfusion.addTruePositive(getTruePositiveCount(label));
		tableOfConfusion.addTrueNegative(getTrueNegativeCount(label));
		tableOfConfusion.addFalsePositive(getFalsePositiveCount(label));
		tableOfConfusion.addFalseNegative(getFalseNegativeCount(label));
		return tableOfConfusion;
	}

	/**
	 * Calculates and returns the overall accuracy for this confusion matrix in range [0,1].
	 *
	 * @return the overall accuracy for this confusion matrix in range [0,1]
	 */
	public double calculateAccuracy() {
		double correct = 0.0;
		for(int i=0; i<labels.size(); i++) {
			correct += confusionMatrix[i][i];
		}
		double divisor = 0.0;
		for(int i=0; i<labels.size(); i++) {
			for(int j=0; j<labels.size(); j++) {
				divisor += confusionMatrix[i][j];
			}
		}
		if(divisor == 0) {
			return Double.NaN;
		} else {
			return correct / divisor;
		}
	}

	/**
	 * Returns the class size as a weight for per-class-calculations.
	 * @param label The label to get the weight for
	 * @return the class size as a weight for per-class-calculations.
	 */
	public long getWeightForLabel(T label) {
		final int indexOfLabel = labels.indexOf(label);
		long weight = 0L;
		for(int i=0; i<labels.size(); i++) {
			if(i == indexOfLabel) {
				continue;
			}
			weight += confusionMatrix[indexOfLabel][i];
		}
		return weight;
	}

	/**
	 * Returns the number of "true positive" instances for the provided label.
	 *
	 * @param label The label to return the counted number of "true positive" instances for
	 * @return The number of "true positive" instances for the provided label
	 */
	private long getTruePositiveCount(T label) {
		final int indexOfLabel = labels.indexOf(label);
		return confusionMatrix[indexOfLabel][indexOfLabel];
	}

	/**
	 * Returns the number of "true negative" instances for the provided label.
	 *
	 * @param label The label to return the counted number of "true negative" instances for
	 * @return The number of "true negative" instances for the provided label
	 */
	private long getTrueNegativeCount(T label) {
		final int indexOfLabel = labels.indexOf(label);

		long trueNegativeCount = 0L;
		for(int i=0; i<labels.size(); i++) {
			if(i == indexOfLabel) {
				continue;
			}
			for(int j=0; j<labels.size(); j++) {
				if(j == indexOfLabel) {
					continue;
				}
				trueNegativeCount += confusionMatrix[i][j];
			}
		}
		return trueNegativeCount;
	}

	/**
	 * Returns the number of "false positive" instances for the provided label.
	 *
	 * @param label The label to return the counted number of "false positive" instances for
	 * @return The number of "false positive" instances for the provided label
	 */
	private long getFalsePositiveCount(T label) {
		final int indexOfLabel = labels.indexOf(label);

		long falsePositiveCount = 0L;
		for(int i=0; i<labels.size(); i++) {
			if(i == indexOfLabel) {
				continue;
			} else {
				falsePositiveCount += confusionMatrix[indexOfLabel][i];
			}
		}
		return falsePositiveCount;
	}

	/**
	 * Returns the number of "false negative" instances for the provided label.
	 *
	 * @param label The label to return the counted number of "false negative" instances for
	 * @return The number of "false negative" instances for the provided label
	 */
	private long getFalseNegativeCount(T label) {
		final int indexOfLabel = labels.indexOf(label);

		long falseNegativeCount = 0L;
		for(int i=0; i<labels.size(); i++) {
			if(i == indexOfLabel) {
				continue;
			} else {
				falseNegativeCount += confusionMatrix[i][indexOfLabel];
			}
		}
		return falseNegativeCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final String lineSeparator = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder("ConfusionMatrix (rows=truth,columns=prediction)").append(lineSeparator).append("values:").append(lineSeparator);
		for(int i=0; i<labels.size(); i++) {
			sb.append(labels.get(i));
			for(int j=0; j<labels.size(); j++) {
				sb.append(" ").append(confusionMatrix[i][j]);
			}
			sb.append(lineSeparator);
		}
		sb.append(lineSeparator).append("results:").append(lineSeparator);

		for(T label : labels) {
			sb.append(label).append(lineSeparator).append(getTableOfConfusion(label));
		}
		return sb.toString();
	}
	
	
	public String toHtml(){
		StringBuilder b = new StringBuilder( "<table class=\"confusionMatrix\">" );
		b.append( "<tr>" );
		b.append( "<td colspan=\"2\" rowspan=\"2\" style=\"border: none;\"></td><th colspan=\"" + labels.size() + "\">prediction</th>" );
		b.append( "</tr>");
		b.append( "<tr>" );
		for( T l : labels ){
			b.append( "<th>" + l.toString() + "</th>" );
		}
		
		b.append( "<th>Precision</th>" );
		b.append( "</tr>" );
		DecimalFormat fmt = new DecimalFormat( "0.00 %" );
		
		for(int i=0; i<labels.size(); i++) {
			T cur = labels.get( i );
			b.append( "<tr>" );
			if( i == 0 )
				b.append( "<th rowspan=\"" + labels.size() + "\">true</th>" );
			
			b.append( "<th>" + labels.get(i) + "</th>" );
			Double tp = 0.0d;
			Double fp = 0.0d;
			for(int j=0; j<labels.size(); j++){
				T against = labels.get( j );
				if( cur != against )
					fp += confusionMatrix[i][j];
				else
					tp += confusionMatrix[i][j];
				b.append(" <td>").append(confusionMatrix[i][j]).append( "</td>" );
			}
			b.append( "<td><nobr>" + fmt.format( tp / (tp+fp) ) + "</nobr></td>" );
			
			b.append( "</tr>\n" );
		}
		
		
		b.append( "</table>" );
		return b.toString();
	}
}