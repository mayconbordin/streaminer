/*
 * The MIT License
 *
 * Copyright 2014 mayconbordin.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.streaminer.stream.classifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streaminer.stream.data.Data;
import org.streaminer.stream.learner.LearnerUtils;

/**
 *
 * @author mayconbordin
 */
public abstract class SimpleClassifier<T> extends AbstractClassifier<Data, T> {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleClassifier.class);
    public static final String DEFAULT_FEATURES_ATTRIBUTE = "features";
    
    private String labelAttribute = null;
    private String featuresAttribute = DEFAULT_FEATURES_ATTRIBUTE;
    
    @Override
    public T predict(Data item) {
        double[] features = (double[]) item.get(featuresAttribute);
        return predict(features);
    }

    @Override
    public void learn(Data item) {
        if (labelAttribute == null)
            labelAttribute = LearnerUtils.detectLabelAttribute(item);

        if (labelAttribute == null) {
            LOG.info("No label defined!");
            return;
        }

        T label = null;
        if (item.get(labelAttribute) == null) {
            LOG.error("No label found for example!");
            return;
        } else {
            label = (T) item.get(labelAttribute);
        }

        double[] features = (double[]) item.get(featuresAttribute);
        
        learn(label, features);
    }

    public String getFeaturesAttribute() {
        return featuresAttribute;
    }

    public void setFeaturesAttribute(String featuresAttribute) {
        this.featuresAttribute = featuresAttribute;
    }

    public String getLabelAttribute() {
        return labelAttribute;
    }

    public void setLabelAttribute(String labelAttribute) {
        this.labelAttribute = labelAttribute;
    }
    
    public abstract T predict(double[] features);
    public abstract void learn(T label, double[] features);
}
