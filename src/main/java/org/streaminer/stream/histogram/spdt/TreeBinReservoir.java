/**
 * Copyright 2013 BigML
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.streaminer.stream.histogram.spdt;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class implements bin operations (insertions, merges, etc) for a histogram.
 * This implementation is best for histograms with a large (>256) number of bins.
 * It uses tree data structures to give O(logN) insert performance with regard to
 * the number of bins in the histogram. For histograms with fewer bins, the
 * ArrayBinReservoir class offers faster insert performance.
 */
public class TreeBinReservoir<T extends Target> extends BinReservoir<T> {

  public TreeBinReservoir(int maxBins, boolean weightGaps, Long freezeThreshold) {
    super(maxBins, weightGaps, freezeThreshold);
    _bins = new TreeMap<Double, Bin<T>>();
    _gaps = new TreeSet<Gap<T>>();
    _binsToGaps = new HashMap<Double, Gap<T>>();
  }
  
  @Override
  public void insert(Bin<T> bin) {
    addTotalCount(bin);
    if (isFrozen() && getBins().size() == getMaxBins()) {
      Double floorDiff = Double.MAX_VALUE;
      Bin<T> floorBin = floor(bin.getMean());
      if (floorBin != null) {
        floorDiff = Math.abs(floorBin.getMean() - bin.getMean());
      }
      Double ceilDiff = Double.MAX_VALUE;
      Bin<T> ceilBin = ceiling(bin.getMean());
      if (ceilBin != null) {
        ceilDiff = Math.abs(ceilBin.getMean() - bin.getMean());
      }
      if (floorDiff <= ceilDiff) {
        floorBin.sumUpdate(bin);
      } else {
        ceilBin.sumUpdate(bin);
      }
    } else {
      Bin<T> existingBin = get(bin.getMean());
      if (existingBin != null) {
        existingBin.sumUpdate(bin);
        if (isWeightGaps()) {
          updateGaps(existingBin);
        }
      } else {
         updateGaps(bin);
        _bins.put(bin.getMean(), bin);
      }
    }
  }

  @Override
  public Bin<T> first() {
    return binFromEntry(_bins.firstEntry());
  }

  @Override
  public Bin<T> last() {
    return binFromEntry(_bins.lastEntry());
  }

  @Override
  public Bin<T> get(double p) {
    return _bins.get(p);
  }

  @Override
  public Bin<T> floor(double p) {
    return binFromEntry(_bins.floorEntry(p));
  }

  @Override
  public Bin<T> ceiling(double p) {
    return binFromEntry(_bins.ceilingEntry(p));
  }

  @Override
  public Bin<T> higher(double p) {
    return binFromEntry(_bins.higherEntry(p));
  }

  @Override
  public Bin<T> lower(double p) {
    return binFromEntry(_bins.lowerEntry(p));
  }

  @Override
  public Collection<Bin<T>> getBins() {
    return _bins.values();
  }

  @Override
  public void merge() {
    while (_bins.size() > getMaxBins()) {
      Gap<T> smallestGap = _gaps.pollFirst();
      Bin<T> newBin = smallestGap.getStartBin().combine(smallestGap.getEndBin());

      Gap<T> followingGap = _binsToGaps.get(smallestGap.getEndBin().getMean());
      if (followingGap != null) {
        _gaps.remove(followingGap);
      }

      _bins.remove(smallestGap.getStartBin().getMean());
      _bins.remove(smallestGap.getEndBin().getMean());
      _binsToGaps.remove(smallestGap.getStartBin().getMean());
      _binsToGaps.remove(smallestGap.getEndBin().getMean());
      
      updateGaps(newBin);
      _bins.put(newBin.getMean(), newBin);
    }
  }

  private void updateGaps(Bin<T> newBin) {
    Bin<T> prev = lower(newBin.getMean());
    if (prev != null) {
      updateGaps(prev, newBin);
    }

    Bin<T> next = higher(newBin.getMean());
    if (next != null) {
      updateGaps(newBin, next);
    }
  }

  private void updateGaps(Bin<T> prev, Bin<T> next) {
    Gap<T> newGap = new Gap<T>(prev, next, gapWeight(prev, next));

    Gap<T> prevGap = _binsToGaps.get(prev.getMean());
    if (prevGap != null) {
      _gaps.remove(prevGap);
    }

    _binsToGaps.put(prev.getMean(), newGap);
    _gaps.add(newGap);
  }

  private Bin<T> binFromEntry(Entry<Double, Bin<T>> entry) {
    if (entry == null) {
      return null;
    } else {
      return entry.getValue();
    }
  }
  
  private final TreeMap<Double, Bin<T>> _bins;
  private final TreeSet<Gap<T>> _gaps;
  private final HashMap<Double, Gap<T>> _binsToGaps;
}
