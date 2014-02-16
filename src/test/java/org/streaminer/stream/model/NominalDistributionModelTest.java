/**
 * 
 */
package org.streaminer.stream.model;

import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * <p>
 * Test case of the nominal distribution model.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 *
 */
public class NominalDistributionModelTest extends TestCase {

	/**
	 * Test method for {@link stream.model.NominalDistributionModel#describe(java.io.Serializable)}.
	 */
	public void testDescribe() {
		NominalDistributionModel<String> m = new NominalDistributionModel<String>();
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		
		m.update( "B" );
		m.update( "B" );
		m.update( "B" );
		
		Double countA = m.describe( "A" );
		Assert.assertEquals( 5.0d / 8.0d, countA );
	}

	/**
	 * Test method for {@link stream.model.NominalDistributionModel#update(java.io.Serializable)}.
	 */
	public void testUpdate() {
		NominalDistributionModel<String> m = new NominalDistributionModel<String>();
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		
		m.update( "B" );
		m.update( "B" );
		m.update( "B" );
		
		m.update( "A" );
		Double countA2 = m.describe( "A" );

		Assert.assertEquals( 6.0d / 9.0d, countA2 );
	}

	/**
	 * Test method for {@link stream.model.NominalDistributionModel#getHistogram()}.
	 */
	public void testGetHistogram() {
		NominalDistributionModel<String> m = new NominalDistributionModel<String>();
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		
		m.update( "B" );
		m.update( "B" );
		m.update( "B" );
		
		Map<String,Double> hist = m.getHistogram();
		Assert.assertNotNull( hist.get( "A" ) );
		Assert.assertEquals( 5, hist.get( "A" ).intValue() );
		
		Assert.assertNotNull( hist.get( "B" ) );
		Assert.assertEquals( 3, hist.get( "B" ).intValue() );
	}

	/**
	 * Test method for {@link stream.model.NominalDistributionModel#truncate(int)}.
	 */
	public void testTruncate() {
		NominalDistributionModel<String> m = new NominalDistributionModel<String>();
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		
		m.update( "B" );
		m.update( "B" );
		m.update( "B" );

		m.truncate( 1 );
		
		Assert.assertEquals( 1.0d, m.describe( "A" ) );
		
		Map<String,Double> hist = m.getHistogram();
		Assert.assertEquals( 1, hist.size() );
	}
	
	
	public void testGetCount(){
		NominalDistributionModel<String> m = new NominalDistributionModel<String>();
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		m.update( "A" );
		
		m.update( "B" );
		m.update( "B" );
		m.update( "B" );

		Assert.assertEquals( 8, m.getCount().intValue() );
	}
}