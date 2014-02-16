package org.streaminer.stream.eval;

/**
 * <p>A data structure for computation of statistical values based on a 2x2 confusion matrix.</p>
 *
 * <p>Look at the 2x2-case at http://en.wikipedia.org/wiki/Confusion_matrix for detailed descriptions.</p>
 *
 * @author Benedikt Kulmann, Lukas Kalabis
 * @see ConfusionMatrix
 */
public final class TableOfConfusion {

    private long truePositive;
    private long trueNegative;
    private long falsePositive;
    private long falseNegative;

    /**
     * Short form of {@link #addTruePositive(1)}.
     */
    public void addTruePositive() {
        addTruePositive(1);
    }

    /**
     * Adds the amount specified by the argument <code>delta</code> to the internal data structure
     * for the counting of true positive instances.
     *
     * @param delta The amount to add to the true positive counter
     */
    public void addTruePositive(long delta) {
        truePositive += delta;
    }

    /**
     * Short form of {@link #addTrueNegative(1)}.
     */
    public void addTrueNegative() {
        addTrueNegative(1);
    }

    /**
     * Adds the amount specified by the argument <code>delta</code> to the internal data structure
     * for the counting of true negative instances.
     *
     * @param delta The amount to add to the true negative counter
     */
    public void addTrueNegative(long delta) {
        trueNegative += delta;
    }

    /**
     * Short form of {@link #addFalsePositive(1)}.
     */
    public void addFalsePositive() {
        addFalsePositive(1);
    }

    /**
     * Adds the amount specified by the argument <code>delta</code> to the internal data structure
     * for the counting of false positive instances.
     *
     * @param delta The amount to add to the false positive counter
     */
    public void addFalsePositive(long delta) {
        falsePositive += delta;
    }

    /**
     * Short form of {@link #addFalseNegative(1)}.
     */
    public void addFalseNegative() {
        addFalseNegative(1);
    }

    /**
     * Adds the amount specified by the argument <code>delta</code> to the internal data structure
     * for the counting of false negative instances.
     *
     * @param delta The amount to add to the false negative counter
     */
    public void addFalseNegative(long delta) {
        falseNegative += delta;
    }

    /**
     * Calculates and returns the precision value.
     *
     * @return The precision value.
     */
    public double calculatePrecision() {
        final long divisor = truePositive + falsePositive;
        if(divisor == 0) {
            return 0.0;
        } else {
            return truePositive / (double)divisor;
        }
    }

    /**
     * Calculates and returns the recall value.
     *
     * @return The recall value.
     */
    public double calculateRecall() {
        final long divisor = truePositive + falseNegative;
        if(divisor == 0) {
            return 0.0;
        } else {
            return truePositive / (double)divisor;
        }
    }

    /**
     * Calculates and returns the specificity value.
     *
     * @return The specificity value.
     */
    public double calculateSpecificity() {
        final long divisor = trueNegative + falsePositive;
        if(divisor == 0) {
            return 0.0;
        } else {
            return trueNegative / (double)divisor;
        }
    }

    /**
     * Calculates and returns the accuracy value.
     *
     * @return The accuracy value.
     */
    public double calculateAccuracy() {
        final long divisor = truePositive + trueNegative + falsePositive + falseNegative;
        if(divisor == 0) {
            return 0.0;
        } else {
            return (truePositive + trueNegative) / (double)divisor;
        }
    }

    /**
     * Calculates and returns the f-score value.
     *
     * @return The f-score value.
     */
    public double calculateFScore() {
        final double divisor = calculatePrecision() + calculateRecall();
        if(divisor == 0) {
            return 0.0;
        } else {
            return 2 * ((calculatePrecision() * calculateRecall()) / (calculatePrecision() + calculateRecall()));
        }
    }

    /**
     * Returns a String representation of this table of confusion. Specifically the returned String contains a representation of
     * <ul>
     * 	<li>Precision</li>
     *  <li>Recall</li>
     *  <li>Specifity</li>
     *  <li>Accuracy</li>
     *  <li>FScore</li>
     * </ul>
     *
     * @return A String representation of this object.
     */
    @Override
    public String toString() {
        final String lineSeparator = System.getProperty("line.separator");
        return new StringBuilder("TableOfConfusion").append(lineSeparator)
                .append("precision:   ").append(calculatePrecision()).append(lineSeparator)
                .append("recall:      ").append(calculateRecall()).append(lineSeparator)
                .append("specificity: ").append(calculateSpecificity()).append(lineSeparator)
                .append("accuracy:    ").append(calculateAccuracy()).append(lineSeparator)
                .append("fscore:      ").append(calculateFScore()).append(lineSeparator)
                .toString();
    }
}
