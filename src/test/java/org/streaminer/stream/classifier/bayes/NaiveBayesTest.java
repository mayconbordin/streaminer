/**
 * 
 */
package org.streaminer.stream.classifier.bayes;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streaminer.stream.data.Data;
import org.streaminer.stream.data.DataImpl;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

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

	@Test
	public void testLearn() throws Exception {
            List<Data> dataset = new ArrayList<Data>();
            
            ICsvListReader listReader = new CsvListReader(
                    new FileReader("src/test/resources/golf.csv"), 
                    CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
            
            listReader.getHeader(true);
            
            List<String> list;
            while( (list = listReader.read()) != null ) {
                Data data = new DataImpl();
                data.put("outlook", list.get(0));
                data.put("temperature", Integer.parseInt(list.get(1)));
                data.put("humidity", Integer.parseInt(list.get(2)));
                data.put("wind", Boolean.parseBoolean(list.get(3)));
                data.put("play", list.get(4));
                
                dataset.add(data);
            }
		
            NaiveBayes nb = new NaiveBayes();
            nb.setLabelAttribute( "play" );
            
            
            for (Data data : dataset) {
                nb.learn( data );
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
	}
}