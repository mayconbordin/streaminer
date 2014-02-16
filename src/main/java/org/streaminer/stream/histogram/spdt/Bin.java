/**
 * Copyright 2013 BigML
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.streaminer.stream.histogram.spdt;

import org.streaminer.util.math.NumberUtil;
import java.text.DecimalFormat;
import org.json.simple.JSONArray;

public class Bin<T extends Target> implements Comparable<Bin> {

  public Bin(double mean, double count, T target) {
    /* Hack to avoid Java's negative zero */
    if (mean == 0d) {
      _mean = 0d;
    } else {
      _mean = mean;
    }
    _count = count;
    _target = target;
  }

  public Bin(Bin<T> bin) {
    this(bin.getMean(), bin.getCount(), (T) bin.getTarget().clone());
  }

  public JSONArray toJSON(DecimalFormat format) {
    JSONArray binJSON = new JSONArray();
    binJSON.add(NumberUtil.roundNumber(_mean, format));
    binJSON.add(NumberUtil.roundNumber(_count, format));
    _target.addJSON(binJSON, format);
    return binJSON;
  }

  public double getCount() {
    return _count;
  }

  public double getMean() {
    return _mean;
  }

  public double getWeight() {
    return _mean * (double) _count;
  }

  public T getTarget() {
    return _target;
  }

  public void sumUpdate(Bin bin) {
    _count += bin.getCount();
    _target.sum(bin.getTarget());
  }

  public void update(Bin bin) throws BinUpdateException {
    if (_mean != bin.getMean()) {
      throw new BinUpdateException("Bins must have matching means to update");
    }

    _count = bin.getCount();
    _target = (T) bin.getTarget();
  }

  @Override
  public String toString() {
    return toJSON(new DecimalFormat(Histogram.DEFAULT_FORMAT_STRING)).toJSONString();
  }

  public Bin combine(Bin<T> bin) {
    double count = getCount() + bin.getCount();
    double mean = (getWeight() + bin.getWeight()) / (double) count;
    T newTarget = (T) _target.init();
    newTarget.sum(_target);
    newTarget.sum(bin.getTarget());
    return new Bin<T>(mean, count, newTarget);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Bin<T> other = (Bin<T>) obj;
    return Double.doubleToLongBits(_mean) == Double.doubleToLongBits(other._mean);
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + (int) (Double.doubleToLongBits(_mean) ^ (Double.doubleToLongBits(_mean) >>> 32));
    return hash;
  }

  private T _target;
  private final double _mean;
  private double _count;

  public int compareTo(Bin o) {
    return Double.compare(getMean(), o.getMean());
  }
}
