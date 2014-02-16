/**
 * Copyright 2013 BigML
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.streaminer.stream.histogram.spdt;

import org.streaminer.util.math.NumberUtil;
import org.streaminer.stream.histogram.spdt.Histogram.TargetType;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ArrayCategoricalTarget extends Target<ArrayCategoricalTarget> implements CategoricalTarget {

  public ArrayCategoricalTarget(Map<Object, Integer> indexMap, double missingCount) {
    _indexMap = indexMap;
    _target = new double[indexMap.size()];
    Arrays.fill(_target, 0);
    _missingCount = missingCount;
  }

  public ArrayCategoricalTarget(Map<Object, Integer> indexMap, Object category) throws MixedInsertException {
    this(indexMap, category == null ? 1 : 0);

    if (category != null) {
      Integer index = indexMap.get(category);
      if (index == null) {
        throw new MixedInsertException();
      } else {
        _target[index]++;
      }
    }
  }

  public void setIndexMap(Map<Object, Integer> indexMap) {
    _indexMap = indexMap;
  }
  
  public HashMap<Object, Double> getCounts() {
    HashMap<Object, Double> countMap = new HashMap<Object, Double>();
    for (Entry<Object, Integer> entry : _indexMap.entrySet()) {
      Object category = entry.getKey();
      Integer index = entry.getValue();
      countMap.put(category, _target[index]);
    }
    return countMap;
  }

  @Override
  public double getMissingCount() {
    return _missingCount;
  }

  @Override
  public TargetType getTargetType() {
    return Histogram.TargetType.categorical;
  }
  
  @Override
  protected void addJSON(JSONArray binJSON, DecimalFormat format) {
    JSONObject counts = new JSONObject();
    for (Entry<Object,Integer> categoryIndex : _indexMap.entrySet()) {
      Object category = categoryIndex.getKey();
      int index = categoryIndex.getValue();
      double count = _target[index];
      counts.put(category, NumberUtil.roundNumber(count, format));
    }
    binJSON.add(counts);
  }

  @Override
  protected ArrayCategoricalTarget sum(ArrayCategoricalTarget target) {
    for (int i = 0; i < _target.length; i++) {
      _target[i] += target._target[i];
    }
    _missingCount += target.getMissingCount();
    return this;
  }

  @Override
  protected ArrayCategoricalTarget mult(double multiplier) {
    for (int i = 0; i < _target.length; i++) {
      _target[i] *= multiplier;
    }
   _missingCount *= multiplier;
   return this;
  }

  @Override
  protected ArrayCategoricalTarget clone() {
    ArrayCategoricalTarget rct = new ArrayCategoricalTarget(_indexMap, _missingCount);
    rct._target = (double[]) _target.clone();
    return rct;
  }

  @Override
  protected ArrayCategoricalTarget init() {
    return new ArrayCategoricalTarget(_indexMap, 0);
  }
  
  private Map<Object, Integer> _indexMap;
  private double[] _target;
  private double _missingCount;
}
