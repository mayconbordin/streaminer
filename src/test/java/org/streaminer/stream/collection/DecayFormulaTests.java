package org.streaminer.stream.collection;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DecayFormulaTests {

    @Test
    public void linearDecayLimit() {
        DecayFormula formula;
        long lifeTime = 3600 * 24 * 365 * 68 ; //68 years
        formula = new LinDecayFormula( lifeTime ); 
        Assert.assertEquals(99.99999999995337, formula.evaluate(100.00, 1.0));
        Assert.assertEquals(50.00, formula.evaluate(100.00, Double.valueOf(lifeTime / 2 * 1000)));
    }

    @Test
    public void exponentialDecayLimit() {
        DecayFormula formula;
        long halfLife = 3600 * 24 * 365 * 68 ; //68 years
        formula = new ExpDecayFormula( halfLife ); //68 years
        Assert.assertEquals(99.99999999996768, formula.evaluate(100.00, 1.0));
        Assert.assertEquals(50.0, formula.evaluate(100.00, Double.valueOf(halfLife * 1000)));
    }

    @Test
    public void logarithmsDecayLimit() throws InterruptedException {
        Long lifeTime = (long) 3600 * 24 * 365 * 68; //68  years
        DecayFormula formula = new LogDecayFormula( lifeTime );
        Assert.assertEquals(99.999999999997848, formula.evaluate(100.00, 1.0));
    }
}
