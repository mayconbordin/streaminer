package org.streaminer.stream.collection;

public class Quantity {
    private Double quantity;
    private Long timestamp; 
    private DecayFormula formula;
        
    public Quantity(Number quantity, Long timestamp, DecayFormula formula) {
        this.quantity = quantity.doubleValue();
        this.timestamp = timestamp;
        this.formula = formula;      
    }
    
    public Quantity(Number quantity, DecayFormula formula) {
        this(quantity, System.currentTimeMillis(), formula);
    }
    
    public Quantity(Number quantity, Long timestamp) {
        this(quantity, timestamp, null);
    }
    
    public Quantity(Number quantity) {
        this(quantity, System.currentTimeMillis());
    }
    
    public void attachFormula(DecayFormula formula) {
        this.formula = formula; 
    }

    final public void add(Quantity a) {
        if (timestamp < a.timestamp) {
            quantity = projectValue(a.timestamp) + a.quantity;
            timestamp = a.timestamp;
        } else {
            quantity += a.projectValue(timestamp);
        }
    }

    final public Double valueNow() {
        return projectValue(System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return String.valueOf(valueNow() + ":" + timestamp);
    }
    
    public Long getTimestamp() {
        return timestamp;
    }

    public Double projectValue(Long futureTimestamp) {
        if (futureTimestamp < timestamp) {
            throw new IllegalArgumentException(
                "Cannot project decaying quantity into the past."
            );
        }
        Double t = Double.valueOf(futureTimestamp -  timestamp);
        return formula.evaluate(quantity,t);
    }

}
