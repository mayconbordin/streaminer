package org.streaminer.stream.collection;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Time-decaying HashMap.
 * Source code: https://github.com/michal-harish/streaming-sketches
 * @author Michal Harish
 * @param <K> Type to be stored
 */
public class DecayHashMap<K> extends ConcurrentHashMap<K, Quantity> {
    private static final long serialVersionUID = 1L;

    private DecayFormula formula;

    public DecayHashMap(DecayFormula formula) {
        this.formula = formula;
    }

    @Override
    public Quantity put(K key, Quantity quantity) {
        quantity.attachFormula(formula);
        if (super.containsKey(key)) {
            Quantity existingQuantity = super.get(key); 
            synchronized(existingQuantity) {
                existingQuantity.add(quantity);
                return super.get(key);
            }
        } else {
            return super.put(key, quantity);
        }        
    }
}
