package org.streaminer.stream.frequency;

import java.util.concurrent.ConcurrentHashMap;
import org.streaminer.stream.frequency.decay.DecayFormula;
import org.streaminer.stream.frequency.decay.Quantity;

/**
 * Time-decaying HashMap.
 * Source code: https://github.com/michal-harish/streaming-sketches
 * @author Michal Harish
 * @param <K> Type to be stored
 */
public class TimeDecayRealCounting<K> extends ConcurrentHashMap<K, Quantity> implements ITimeDecayFrequency<K> {
    private static final long serialVersionUID = 1L;

    private DecayFormula formula;

    public TimeDecayRealCounting(DecayFormula formula) {
        this.formula = formula;
    }
    
    public void add(K item, long qtd, long timestamp) {
        Quantity q = new Quantity(qtd, timestamp, formula);
        
        if (super.containsKey(item)) {
            Quantity existingQuantity = get(item);
            synchronized(existingQuantity) {
                existingQuantity.add(q);
            }
        } else {
            put(item, q);
        }
    }

    public double estimateCount(K item, long timestamp) {
        if (super.containsKey(item)) {
            Quantity existingQuantity = get(item);
            synchronized(existingQuantity) {
                return existingQuantity.projectValue(timestamp);
            }
        }
        
        return 0d;
    }
    
    
}
