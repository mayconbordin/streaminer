/**
 * Copyright 2013 BigML
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.streaminer.stream.histogram.spdt;

import org.streaminer.util.math.NumberUtil;
import org.streaminer.stream.histogram.spdt.Histogram.TargetType;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map.Entry;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MapCategoricalTarget extends Target<MapCategoricalTarget> implements CategoricalTarget {

  public MapCategoricalTarget(Object category) {
    _counts = new HashMap<Object, Double>(1,1);
    _counts.put(category, 1d);
  }
  
  public MapCategoricalTarget(HashMap<Object, Double> targetCounts, double missingCount) {
    _counts = targetCounts;
    _counts.put(null, missingCount);
  }

  public MapCategoricalTarget(HashMap<Object, Double> targetCounts) {
    _counts = targetCounts;
  }
  
  public HashMap<Object, Double> getCounts() {
    return _counts;
  }

  @Override
  public double getMissingCount() {
    Double missingCount = _counts.get(null);
    return missingCount == null ? 0 : missingCount;
  }
  
  @Override
  public TargetType getTargetType() {
    return TargetType.categorical;
  }
  
  @Override
  protected void addJSON(JSONArray binJSON, DecimalFormat format) {
    JSONObject counts = new JSONObject();
    for (Entry<Object,Double> categoryCount : _counts.entrySet()) {
      Object category = categoryCount.getKey();
      double count = categoryCount.getValue();
      counts.put(category, NumberUtil.roundNumber(count, format));
    }
    binJSON.add(counts);
  }

  @Override
  protected MapCategoricalTarget sum(MapCategoricalTarget target) {
    for (Entry<Object, Double> categoryCount : target.getCounts().entrySet()) {
      Object category = categoryCount.getKey();
      
      Double oldCount = _counts.get(category);
      oldCount = (oldCount == null) ? 0 : oldCount;

      double newCount = oldCount + categoryCount.getValue();
      _counts.put(category, newCount);
    }
    
    return this;
  }

  @Override
  protected MapCategoricalTarget mult(double multiplier) {
   for (Entry<Object, Double> categoryCount : getCounts().entrySet()) {
     categoryCount.setValue(categoryCount.getValue() * multiplier);
   }

   return this;
  }

  @Override
  protected MapCategoricalTarget clone() {
    return new MapCategoricalTarget(new HashMap<Object, Double>(_counts));
  }

  @Override
  protected MapCategoricalTarget init() {
    return new MapCategoricalTarget(new HashMap<Object, Double>());
  }
  
  private HashMap<Object, Double> _counts;
}
