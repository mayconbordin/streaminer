/**
 * Copyright 2013 BigML
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.streaminer.stream.histogram.spdt;

import org.json.simple.JSONArray;

public class Gap<T extends Target> implements Comparable<Gap> {

  public Gap(Bin<T> startBin, Bin<T> endBin, double weight) {
    _startBin = startBin;
    _endBin = endBin;
    _weight = weight;
  }
          
  public Gap(Bin<T> startBin, Bin<T> endBin) {
    this(startBin, endBin, endBin.getMean() - startBin.getMean());
  }

  public Bin<T> getStartBin() {
    return _startBin;
  }
  
  public Bin<T> getEndBin() {
    return _endBin;
  }

  public double getSpace() {
    return _weight;
  }
  
  @Override
  public String toString() {
    JSONArray jsonArray = new JSONArray();
    jsonArray.add(_weight);
    jsonArray.add(_startBin);
    jsonArray.add(_endBin);
    return jsonArray.toJSONString();
  }
  
  @Override
  public int compareTo(Gap t) {
    int result = Double.compare(this.getSpace(), t.getSpace());
    if (result == 0) {
      result = getStartBin().compareTo(t.getStartBin());
    }
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Gap other = (Gap) obj;
    if (Double.doubleToLongBits(this._weight) != Double.doubleToLongBits(other._weight)) {
      return false;
    }
    if (this._startBin != other._startBin && (this._startBin == null || !this._startBin.equals(other._startBin))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 23 * hash + (int) (Double.doubleToLongBits(this._weight) ^ (Double.doubleToLongBits(this._weight) >>> 32));
    hash = 23 * hash + (this._startBin != null ? this._startBin.hashCode() : 0);
    return hash;
  }
  
  private final double _weight;
  private final Bin<T> _startBin;
  private final Bin<T> _endBin;
}
