/*
 *  streams library
 *
 *  Copyright (C) 2011-2012 by Christian Bockermann, Hendrik Blom
 * 
 *  streams is a library, API and runtime environment for processing high
 *  volume data streams. It is composed of three submodules "stream-api",
 *  "stream-core" and "stream-runtime".
 *
 *  The streams library (and its submodules) is free software: you can 
 *  redistribute it and/or modify it under the terms of the 
 *  GNU Affero General Public License as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any 
 *  later version.
 *
 *  The stream.ai library (and its submodules) is distributed in the hope
 *  that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.streaminer.util;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * This class implements a split-method which takes care of quoted strings, i.e. there will be no
 * split within a char sequence that is surrounded by quotes (single or double quotes). These sequences
 * are simply skipped.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 */
public class QuotedStringTokenizer {
    
    
    public static List<String> splitRespectQuotes( String input, char sep ){

        List<String> results = new ArrayList<String>();
        int last = 0;
        int i = 0;
        
        while( i <= input.length() - 1 ){
            char c = input.charAt( i );
            
            
            // we skip quoted substrings 
            //
            if( c == '"' || c == '\'' ){
                do {
                    i++;
                    //char d = input.charAt( i );
                } while( i < input.length() && (input.charAt( i ) != c || input.charAt( i - 1 ) == '\\' ) );
            }
            
            // if we hit a separating character, we found another token
            //
            if( input.indexOf( sep, i ) == i  || i+1 == input.length() ){
                if( i + 1 == input.length() )
                    results.add( input.substring( last, i + 1 ) );
                else
                    results.add( input.substring( last, i ) );
                last = i + 1;
            }
            
            i++;
        }
        
        return results;
    }
}