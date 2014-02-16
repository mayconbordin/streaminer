/**
 * Copyright 2013 BigML
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.streaminer.stream.histogram.spdt;

import org.streaminer.util.math.NumberUtil;
import org.streaminer.stream.histogram.spdt.Histogram.TargetType;
import java.text.DecimalFormat;
import org.json.simple.JSONArray;

public class NumericTarget extends Target<NumericTarget> {

  public NumericTarget(Double target, Double sumSquares, double missingCount) {
    _sum = target;
    _sumSquares = sumSquares;
    _missingCount = missingCount;
  }

  public NumericTarget(Double target, double missingCount) {
    _sum = target;
    if (target != null) {
      _sumSquares = target * target;
    }
    _missingCount = missingCount;
  }

  public NumericTarget(Double target) {
    this(target, target == null ? 1 : 0);
  }

  public Double getSum() {
    return _sum;
  }

  public Double getSumSquares() {
    return _sumSquares;
  }

  @Override
  public double getMissingCount() {
    return _missingCount;
  }

  @Override
  public TargetType getTargetType() {
    return Histogram.TargetType.numeric;
  }

  @Override
  public String toString() {
    return String.valueOf(_sum) + "," + String.valueOf(_sumSquares);
  }

  @Override
  protected void addJSON(JSONArray binJSON, DecimalFormat format) {
    if (_sum == null) {
      binJSON.add(null);
    } else {
      binJSON.add(NumberUtil.roundNumber(_sum, format));
      binJSON.add(NumberUtil.roundNumber(_sumSquares, format));
    }
  }

  @Override
  protected NumericTarget init() {
    return new NumericTarget(0d);
  }

  @Override
  protected NumericTarget clone() {
    return new NumericTarget(_sum, _sumSquares, _missingCount);
  }

  private Double _sum;
  private Double _sumSquares;
  private double _missingCount;

  @Override
  protected NumericTarget sum(NumericTarget target) {
    if (_sum == null && target.getSum() != null) {
      _sum = target.getSum();
      _sumSquares = target.getSumSquares();
    } else if (_sum != null && target.getSum() != null){
      _sum += target.getSum();
      _sumSquares += target.getSumSquares();
    }
    _missingCount += target.getMissingCount();
    return this;
  }

  @Override
  protected NumericTarget mult(double multiplier) {
    if (_sum != null) {
      _sum *= multiplier;
      _sumSquares *= multiplier;
    }
    _missingCount *= multiplier;
    return this;
  }
}
