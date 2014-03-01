package org.streaminer.stream.collection;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DecayingQuantityTest {
    
    private ExpDecayFormula expDecay1Min;

    @Before
    public void createFormulas() {
        expDecay1Min = new ExpDecayFormula(60);
    }
    
    private Double approximately(Double value ) {
        return Double.valueOf(Math.round(value  * 1000000) / 1000000);
    }

    @Test
    public void statinoaryQuantityDoesnt() {
        Long now = System.currentTimeMillis();
        Quantity x = new Quantity(3454.3245345, now, expDecay1Min);
        Assert.assertEquals(3454.3245345, x.projectValue(now));
    }

    @Test
    public void quantityDecaysCorrectly() {
        Long now = System.currentTimeMillis();
        Quantity x = new Quantity(1000.00, now, expDecay1Min);        
        Assert.assertEquals(500.00, x.projectValue(now + 1 * 60*1000));
        Assert.assertEquals(250.00, approximately(x.projectValue(now + 2 * 60*1000)));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void cannotProjectQuantityIntoThePast() {
        Long now = System.currentTimeMillis();
        Quantity x = new Quantity(1000.00, now, expDecay1Min);
        x.projectValue(now - 1000);
    }

    @Test
    public void quantityAddsUpCorrectly() {
        Long now  = System.currentTimeMillis();
        Quantity x = new Quantity(1000.00, now, expDecay1Min);                
        x.add(new Quantity(1000.00, now - 60*1000, expDecay1Min));
        Assert.assertEquals(1500.00, x.projectValue(now));  
        
        now += 60*1000;
        x.add(new Quantity(1000.00, now, expDecay1Min));
        Assert.assertEquals(1000.00 + 1500 / 2, approximately(x.projectValue(now)));
        
        now += 60*1000;
        Assert.assertEquals((1000.00 + 1500 / 2) / 2, approximately(x.projectValue(now)));
    }
}
