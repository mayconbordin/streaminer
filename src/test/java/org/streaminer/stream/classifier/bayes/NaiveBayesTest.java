/**
 * 
 */
package org.streaminer.stream.classifier.bayes;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 *
 */
public class NaiveBayesTest {

	static Logger log = LoggerFactory.getLogger( NaiveBayesTest.class );
	
	/**
	 * Test method for {@link stream.learner.NaiveBayes#predict(stream.data.Data)}.
	 */
	@Test
	public void testPredict() {
		//fail("Not yet implemented");
	}

	/**
	 * Test method for {@link stream.learner.NaiveBayes#learn(stream.data.Data)}.
	 
	@Test
	public void testLearn() throws Exception {
		
		DataSource ds = new DataSource();
		ds.setName( "test-data" );
		ds.setUrl( "classpath:/golf.csv.gz" );
		ds.setClassName( "stream.io.CsvStream" );
		DataStream stream = ds.createDataStream(); //new CsvStream( NaiveBayesTest.class.getResource( "/golf.csv" ) );
		
		NaiveBayes nb = new NaiveBayes();
		nb.setLabelAttribute( "play" );

		Data data = stream.readNext();
		while( data != null ){
			nb.learn( data );
			data = stream.readNext();
		}

		Data test = new DataImpl();
		test.put( "outlook", "sunny" );
		test.put( "temperature", "cool" );
		test.put( "humidity", "high" );
		test.put( "windy", "TRUE" );
		
		String prediction = nb.predict( test );
		log.info( "item is: {}", test );
		log.info( "Prediction is: {}", prediction );
		
		test.put( "outlook", "overcast" );
		test.put( "temperature", "mild" );
		test.put( "humidity", "normal" );
		test.put( "windy", "FALSE" );
		
		prediction = nb.predict( test );
		log.info( "item is: {}", test );
		log.info( "Prediction is: {}", prediction );
		
		//  Ein Trainingsbeispiel:  "overcast";"hot";"normal";"FALSE";"yes"
		test.put( "outlook", "overcast" );
		test.put( "temperature", "hot" );
		test.put( "humidity", "normal" );
		test.put( "windy", "FALSE" );
		
		prediction = nb.predict( test );
		log.info( "item is: {}", test );
		log.info( "Prediction is: {}", prediction );
		Assert.assertEquals( "yes", prediction );
	}*/
}