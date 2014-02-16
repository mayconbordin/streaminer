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
package org.streaminer.stream.data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import org.streaminer.util.SizeOf;

/**
 * <p>
 * This class is the default implementation of the Data item. The complete
 * implementation is based upon Java's core LinkedHashMap implementation.
 * </p>
 * <p>
 * Objects of this class should not be created directory, but rather by using a
 * {@link stream.data.DataFactory}.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 * 
 */
public class DataImpl extends LinkedHashMap<String, Serializable> implements Data {
    /** The unique class ID */
    private static final long serialVersionUID = -7751681008628413236L;

    public DataImpl() {
    }

    /**
     * @param data
     */
    public DataImpl(Map<String, Serializable> data) {
        super(data);
    }

    /**
     * @return 
     * @see stream.Measurable#getByteSize()
     */
    public double getByteSize() {
        double size = 0.0d;

        for (String key : keySet()) {
            size += key.length() + 1; // provide the rough size of one byte for
                                      // each character + a single terminating
                                      // 0-byte

            // add the size of each value of this map
            Serializable value = get(key);
            size += SizeOf.sizeOf(value);
        }

        return size;
    }
}
