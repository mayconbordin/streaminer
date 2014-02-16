/**
 * Copyright 2013 BigML
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.streaminer.stream.histogram.spdt;

import org.streaminer.stream.histogram.spdt.Histogram.TargetType;
import java.text.DecimalFormat;
import org.json.simple.JSONArray;

public class SimpleTarget extends Target<SimpleTarget> {
  public static final SimpleTarget TARGET = new SimpleTarget();

  /* SimpleTargets cannot have missing values */
  @Override
  public double getMissingCount() {
    return 0;
  }

  @Override
  public TargetType getTargetType() {
    return Histogram.TargetType.none;
  }

  @Override
  protected void addJSON(JSONArray binJSON, DecimalFormat format) {
  }
  
  @Override
  protected SimpleTarget init() {
    return this;
  }
  
  @Override
  protected SimpleTarget clone() {
    return this;
  }
  
  @Override
  protected SimpleTarget sum(SimpleTarget bin) {
    return this;
  }

  @Override
  protected SimpleTarget mult(double multiplier) {
    return this;
  }
    
  private SimpleTarget() {}
}
