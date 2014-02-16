/**
 * Copyright 2013 BigML
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.streaminer.stream.histogram.spdt;

import java.util.Collection;

public abstract class BinReservoir<T extends Target> {
  public BinReservoir(int maxBins, boolean weightGaps, Long freezeThreshold) {
    _maxBins = maxBins;
    _weightGaps = weightGaps;
    _freezeThreshold = freezeThreshold;
    _totalCount = 0;
  }
  
  public int getMaxBins() {
    return _maxBins;
  }
  
  public boolean isWeightGaps() {
    return _weightGaps;
  }
  
  public Long getFreezeThreshold() {
    return _freezeThreshold;
  }
    
  public boolean isFrozen() {
    return _freezeThreshold != null && _totalCount > _freezeThreshold;
  }
  
  public long getTotalCount() {
    return _totalCount;
  }
  
  public void addTotalCount(Bin<T> bin) {
    _totalCount += bin.getCount();
  }
  
  public abstract void insert(Bin<T> bin);
  public abstract Bin<T> first();
  public abstract Bin<T> last();
  public abstract Bin<T> get(double p);
  public abstract Bin<T> floor(double p);
  public abstract Bin<T> ceiling(double p);
  public abstract Bin<T> higher(double p);
  public abstract Bin<T> lower(double p);
  public abstract Collection<Bin<T>> getBins();
  public abstract void merge();
  
  protected double gapWeight(Bin<T> prev, Bin<T> next) {
    double diff = next.getMean() - prev.getMean();
    if (isWeightGaps()) {
      diff *= Math.log(Math.E + Math.min(prev.getCount(), next.getCount()));
    }
    return diff;
  }
  
  private final int _maxBins;
  private final boolean _weightGaps;
  private final Long _freezeThreshold;
  private long _totalCount;
}
