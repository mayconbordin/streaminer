package org.streaminer.stream.classifier.bayes;

import org.streaminer.stream.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streaminer.util.SizeOf;

/**
 * <p>
 * This implementation of the MultiBayes learner uses the Lossy-Counting Bayes
 * implementation, which counts at fixed memory space.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 * 
 */
public class MultiTopkBayes extends MultiBayes {
    /** The unique class ID */
    private static final long serialVersionUID = 1354945765610306076L;

    /* A global logger for this class */
    private static final Logger LOG = LoggerFactory.getLogger(MultiTopkBayes.class);

    private int k = 100;
    private int i = 0;
    private int nestedNBs = 0;

    /**
     * @return the k
     */
    public Integer getK() {
        return k;
    }

    /**
     * @param k the k to set
     */
    public void setK(Integer k) {
        this.k = k;
    }

    /**
     * @return 
     * @see stream.learner.MultiBayes#createBayesLearner(java.lang.String)
     */
    @Override
    protected NaiveBayes createBayesLearner(String attribute) {
        LOG.debug("Creating new TopK-bayes for attribute {}", attribute);
        TopKBayes lb = new TopKBayes();

        if (getK() == null) {
            setK(100);
            LOG.warn("No value set for parameter 'k', using default: {}", getK());
        }
        
        lb.setK(getK());
        lb.setLabelAttribute(attribute);
        this.nestedNBs++;
        return lb;
    }

    /**
     * @param item
     * @see stream.learner.MultiBayes#learn(stream.data.Data)
     */
    @Override
    public void learn(Data item) {
        super.learn(item);
        i++;
        if (i % 100 == 0) {
            LOG.debug("After {} envts:", i);
            LOG.debug("   using {} bytes", SizeOf.sizeOf(this));
            LOG.debug("   using {} naive bayes learner", nestedNBs);
        }
    }
}