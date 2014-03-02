package org.streaminer.stream.sampler.sre;

import java.util.ArrayList;
import java.util.List;
import org.streaminer.util.hash.Hash;

/**
 * An s-sparse recovery estimator comprised of an array of 1-sparse estimators.
 * Python Source Code: https://github.com/venantius/droplet
 * 
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class SSparseRecoveryEstimator {
    private int ncols;
    private int nrows;
    private Hash hasher;
    private OneSparseRecoveryEstimator[][] array;

    public SSparseRecoveryEstimator(int ncols, int nrows, Hash hasher) {
        this.ncols = ncols;
        this.nrows = nrows;
        this.hasher = hasher;
        
        array = new OneSparseRecoveryEstimator[nrows][ncols];
        for (int i=0; i<nrows; i++)
            for (int j=0; j<ncols; j++)
                array[i][j] = new OneSparseRecoveryEstimator();
    }
    
    /**
     * Check to see if this level is s-sparse.
     * @return 
     */
    public boolean isSSparse() {
        int sparsity = 0;
        int size = ncols * nrows;
        
        for (int i=0; i<nrows; i++)
            for (int j=0; j<ncols; j++)
                if (array[i][j].getPhi() == 0)
                    sparsity++;
        
        return sparsity >= (ncols / 2) && (sparsity != size);
    }
    
    /**
     * Update the s-sparse recovery estimator at each hash function.
     * @param index
     * @param value 
     */
    public void update(int index, int value) {
        for (int row=0; row<nrows; row++) {
            int col = hash(index, row) % ncols;
            array[row][col].update(index, value);
        }
    }
    
    /**
     * Attempt to recover a nonzero vector from this level.
     * @return 
     */
    public List<OneSparseRecoveryEstimator> recover() {
        List<OneSparseRecoveryEstimator> aPrime = new ArrayList<OneSparseRecoveryEstimator>();
        
        for (int i=0; i<nrows; i++)
            for (int j=0; j<ncols; j++)
                if (array[i][j].getPhi() != 0 && array[i][j].isOneSparseGanguly())
                    aPrime.add(array[i][j]);
        
        return aPrime;
    }
    
    protected int hash(int index, int seed) {
        return hasher.hash(String.valueOf(index).getBytes(), seed);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SSparseRecoveryEstimator{array=[");
        
        for (int i=0; i<nrows; i++)
            for (int j=0; j<ncols; j++)
                sb.append(String.format("(%d,%d)=%s, ", i, j, array[i][j].toString()));
        sb.append("]}");
        
        return sb.toString();
    }
    
    
}
