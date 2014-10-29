/*
 * hoidla: various algorithms for Big Data solutions
 * Author: Pranab Ghosh
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.streaminer.stream.wavelet;

/**
 * Calculates Haar wavelet coefficients
 * @author pranab
 *
 */
public class HaarWaveletTransform implements IWavelet {
    private int[] coeff;
    private int average;
    private int numLargest;
    private int dataSize;

    /**
     * @param numLargest
     */
    public HaarWaveletTransform(int numLargest) {
        this.numLargest = numLargest;
    }

    /**
     * @param compactCoeff
     * @param dataSetSize
     */
    public HaarWaveletTransform(CompactHaarCoefficient compactCoeff, int dataSetSize) {
        average = compactCoeff.getAverage();
        coeff = compactCoeff.reconstructCoeff(dataSetSize);
        numLargest = compactCoeff.getCompactCoeff().length;
        dataSize = coeff.length + 1;
    }

    public void setNumLargest(int numLargest) {
        this.numLargest = numLargest;
    }

    /**
     * @param data
     */
    public void transform(int[] data) {
        if (isMultipleOfTwo(data.length)) {
            coeff = new int[data.length - 1];
            findCoeff(data, coeff, 0);
            dataSize = data.length;
        }
    }

    /**
     * @return
     */
    public int[] untransform() {
        int[] data = new int[1];
        data[0] = average;
        int coeffOffset = coeff.length - 1;
        data = getData(data, coeffOffset);
        return data;
    }

    /**
     * @return
     */
    public int[] getCoefficients() {
        return coeff;
    }

    public int getAverage() {
        return average;
    }

    /**
     * @return
     */
    public CompactHaarCoefficient getCompactTransform() {
        return new CompactHaarCoefficient(coeff, average, numLargest);
    }

    /**
     * @param value
     * @return
     */
    public boolean isMultipleOfTwo(int value) {
        int count = 0;
        for (int i = 0; i < 32; ++i) {
            if ((value & 1) == 1) {
                ++count;
            }
            value >>= 1;
        }
        return count == 1;
    }
    
    
    /**
     * @param data
     * @param coeffOffset
     * @return
     */
    private int[] getData(int[] data, int coeffOffset) {
        int[] newData = new int[data.length * 2];
        for (int i = 0, j = 0, k = coeffOffset; i < data.length; ++i, ++k) {
            newData[j++] = data[i] + coeff[k]; 
            newData[j++] = data[i] - coeff[k]; 
        }
        
        if (newData.length < dataSize) {
            getData(newData, coeffOffset + data.length);
        }
        
        return newData;
    }
    
    /**
     * @param data
     * @param coeff
     * @param current
     */
    private void findCoeff(int[] data, int[] coeff, int current) {
        int[] newData = new int[data.length/2];
        for (int i = 0, j = 0; i < data.length; i += 2, ++j) {
            newData[j] = (data[i] + data[i+1]) >> 1;
            coeff[current++] = (data[i] - data[i+1]) >> 1;
        }
        
        if (newData.length > 1) {
            findCoeff(newData, coeff, current); 
        } else {
            average = newData[0];
        }
    }
    

    /**
     * Compact representation with only large coefficients
     * @author pranab
     */
    public static class CompactHaarCoefficient {
        private int average;
        private int[] compactCoeff;
        private long coeffBitMap;

        /**
         * @param coeff
         * @param average
         * @param numLargest
         */
        public CompactHaarCoefficient(int[] coeff, int average, int numLargest) {
            this.average = average;
            int[] coeffCopy = new int[coeff.length];
            for (int i = 0; i < coeff.length; ++i) {
                coeffCopy[i] = Math.abs(coeff[i]);
            }
            int smallest = coeffCopy[coeff.length - numLargest - 1];

            compactCoeff = new int[numLargest];
            for (int i = 0, j = 0; i < coeff.length; ++i) {
                if (Math.abs(coeff[i]) > smallest) {
                    compactCoeff[j++] = coeff[i];
                    coeffBitMap |= (1 << i);
                }
            }
        }

        /**
         * deserializes from string
         * @param data
         */
        public CompactHaarCoefficient(String data) {
            String[] parts = data.split(",");
            average = Integer.parseInt(parts[0]);
            coeffBitMap = Long.parseLong(parts[1]);
            compactCoeff = new int[parts.length -2];
            for (int i = 2; i < parts.length; ++i) {
                compactCoeff[i - 2] = Integer.parseInt(parts[i]);
            }
        }

        @Override
        public String toString() {
            StringBuilder stBld = new StringBuilder();
            stBld.append(average).append(",").append(coeffBitMap);
            for (int i = 0; i < compactCoeff.length; ++i) {
                stBld.append(",").append(compactCoeff[i]);
            }

            return stBld.toString();
        }

        public int getAverage() {
            return average;
        }

        public int[] getCompactCoeff() {
            return compactCoeff;
        }

        public long getCoeffBitMap() {
            return coeffBitMap;
        }

        public int[] reconstructCoeff(int dataSetSize) {
            int[] coeff = new int[dataSetSize - 1];
            for (int i = 0, j = 0; i < dataSetSize - 1; ++i) {
                if ((coeffBitMap & (1 << i)) == 1) {
                    coeff[i] = compactCoeff[j++]; 
                } else {
                    coeff[i] = 0;
                }
            }
            return coeff;
        }
    }
}
