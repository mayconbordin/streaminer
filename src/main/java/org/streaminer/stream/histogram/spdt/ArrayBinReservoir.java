/**
 * Copyright 2013 BigML
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.streaminer.stream.histogram.spdt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * This class implements bin operations (insertions, merges, etc) for a histogram.
 * This implementation is best for histograms with a small (<=256) number of bins.
 * It uses an ArrayList to give O(N) insert performance with regard to the number
 * of bins in the histogram. For histograms with more bins, the TreeBinReservoir
 * class offers faster insert performance.
 */
public class ArrayBinReservoir <T extends Target> extends BinReservoir<T> {

  public ArrayBinReservoir(int maxBins, boolean weightGaps, Long freezeThreshold) {
    super(maxBins, weightGaps, freezeThreshold);
    _bins = new ArrayList<Bin<T>>();
  }

  @Override
  public void insert(Bin<T> bin) {
    addTotalCount(bin);
    int index = Collections.binarySearch(_bins, bin);
    if (index >= 0) {
      _bins.get(index).sumUpdate(bin);
    } else {
      if (isFrozen()) {
        int prevIndex = Math.abs(index) - 2;
        int nextIndex = prevIndex + 1;
        double prevDist = (prevIndex >= 0) ? 
                bin.getMean() - _bins.get(prevIndex).getMean() : Double.MAX_VALUE;
        double nextDist = (nextIndex < _bins.size()) ?
                _bins.get(nextIndex).getMean() - bin.getMean() : Double.MAX_VALUE;
        if (prevDist < nextDist) {
          _bins.get(prevIndex).sumUpdate(bin);
        } else {
          _bins.get(nextIndex).sumUpdate(bin);
        }
      } else {
        _bins.add(Math.abs(index) - 1, bin);
      }
    }
  }

  @Override
  public Bin<T> first() {
    return _bins.get(0);
  }

  @Override
  public Bin<T> last() {
    return _bins.get(_bins.size() - 1);
  }

  @Override
  public Bin<T> get(double p) {
    int index = Collections.binarySearch(_bins, new Bin(p, 0, null));
    return (index >= 0) ? _bins.get(index) : null;
  }

  @Override
  public Bin<T> floor(double p) {
    int index = Collections.binarySearch(_bins, new Bin(p, 0, null));
    if (index >= 0) {
      return _bins.get(index);
    } else {
      index = Math.abs(index) - 2;
      return (index >= 0) ? _bins.get(index) : null;
    }
  }

  @Override
  public Bin<T> ceiling(double p) {
    int index = Collections.binarySearch(_bins, new Bin(p, 0, null));
    if (index >= 0) {
      return _bins.get(index);
    } else {
      index = Math.abs(index) - 1;
      return (index < _bins.size()) ? _bins.get(index) : null;
    }
  }

  @Override
  public Bin<T> lower(double p) {
    int index = Collections.binarySearch(_bins, new Bin(p, 0, null));
    if (index >= 0) {
      index--;
    } else {
      index = Math.abs(index) - 2;
    }
    return (index >= 0) ? _bins.get(index) : null;
  }

  @Override
  public Bin<T> higher(double p) {
    int index = Collections.binarySearch(_bins, new Bin(p, 0, null));
    if (index >= 0) {
      index++;
    } else {
      index = Math.abs(index) - 1;
    }
    return (index < _bins.size()) ? _bins.get(index) : null;
  }

  @Override
  public Collection<Bin<T>> getBins() {
    return _bins;
  }

  @Override
  public void merge() {
    while (_bins.size() > getMaxBins()) {
      int minGapIndex = -1;
      double minGap = Double.MAX_VALUE;
      for (int i = 0; i < _bins.size() - 1; i++) {
        double gap = gapWeight(_bins.get(i), _bins.get(i + 1));
        if (minGap > gap) {
          minGap = gap;
          minGapIndex = i;
        }
      }
      Bin<T> prev = _bins.get(minGapIndex);
      Bin<T> next = _bins.remove(minGapIndex + 1);
      _bins.set(minGapIndex, prev.combine(next));
    }
  }
  
  private ArrayList<Bin<T>> _bins;
}
