/**
 * Copyright 2011 Cloudera Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.streaminer.stream.quantile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author maycon
 */
public class MPQuantilesTest extends TestCase {
    private static List<Double> testQuantiles = new ArrayList<Double>(Arrays.asList(0.0, 38.0, 78.0, 118.0, 158.0, 198.0));
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    
    
    private MPQuantiles create(int numQuantiles) {
        return new MPQuantiles(numQuantiles);
    }
  
    @Test
    public void testBasic() {
        MPQuantiles qe = create(6);
        for (int i = 0; i < 200; i += 2) {
          qe.offer((double)i);
        }
        
        List<Double> quantiles = qe.getQuantiles();
        assertTrue(testQuantiles.containsAll(quantiles));
    }
}