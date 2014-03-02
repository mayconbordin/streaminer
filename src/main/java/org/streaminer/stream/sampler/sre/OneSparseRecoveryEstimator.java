package org.streaminer.stream.sampler.sre;

/**
 * A straightforward implementation of a one-sparse recovery estimator.
 * Includes sparsity checks at varying levels of complexity.
 * Python Source Code: https://github.com/venantius/droplet
 * 
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public class OneSparseRecoveryEstimator {
    private int iota;
    private int phi;
    private int tau;
    
    /**
     * Update the recovery estimators.
     * @param index
     * @param weight 
     */
    public void update(int index, int weight) {
        phi += weight;
        iota += weight * index;
        tau += weight * Math.pow(index, 2);
    }
    
    /**
     * The simplest possible check to verify if P1 is indeed one-sparse.
     * @param index
     * @return 
     */
    public boolean isOneSparseSimple(int index) {
        return (iota / phi) == index;
    }
    
    /**
     * A slightly more advanced check to see if P1 is one-sparse. In cases where 
     * updates are assumed to be non-negative, this is sufficient.
     * @return 
     */
    public boolean isOneSparseGanguly() {
        return (phi * tau) == Math.pow(iota, 2);
    }

    public int getIota() {
        return iota;
    }

    public int getPhi() {
        return phi;
    }

    public int getTau() {
        return tau;
    }
    

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.iota;
        hash = 59 * hash + this.phi;
        hash = 59 * hash + this.tau;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OneSparseRecoveryEstimator other = (OneSparseRecoveryEstimator) obj;
        if (this.iota != other.iota) {
            return false;
        }
        if (this.phi != other.phi) {
            return false;
        }
        if (this.tau != other.tau) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "OneSparseRecoveryEstimator{" + "iota=" + iota + ", phi=" + phi 
                + ", tau=" + tau + ", isOneSparseGanguly=" + isOneSparseGanguly() + '}';
    }
}
