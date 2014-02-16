package org.streaminer.stream.quantile;

import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a simple implementation to determine exact quantiles. It's just for
 * testing/validating purpose so it won't be the most efficient algorithm neither 
 * in respect to space consumption nor in respect to time consumption. And of 
 * course it is not an online algorithm!
 * 
 * @author Markus Kokott
 *
 */
public class ExactQuantiles implements IQuantiles<Double> {
    private Map<Double,Long> elements;
    private long overallCount;
    private TreeSet<Double> data = new TreeSet<Double>();
    
    /**
     * Creates a new instance of {@link ExactQuantiles}
     */
    public ExactQuantiles(){
        this.elements = new ConcurrentHashMap<Double, Long>();
        this.overallCount = 0L;
    }

    
    
    @Override
    public void offer(Double value) {
        Long count = elements.get(value);
        if( count == null ) {
            elements.put(value, new Long(1L));
            data.add( value );
        } else
            elements.put(value, count + 1);

        this.overallCount++;
    }

    @Override
    public Double getQuantile(double q) throws QuantilesException {
        final long overallCountCopy = new Long(overallCount);
        final long rank = (long)Math.floor(q * overallCountCopy);

        long countSum = 0L;
        for (Double val : data ) {
            Long count = elements.get( val );
            countSum += count;
            if (rank <= countSum) {
                return val;
            }
        }
        return Double.NaN;
    }
    
    @Override
    public String toString(){
        StringBuffer s = new StringBuffer();
        s.append( getClass().getCanonicalName() );
        s.append( " {" );
        s.append( " }" );
        return s.toString();
    }
}
