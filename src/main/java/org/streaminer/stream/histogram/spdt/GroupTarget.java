/**
 * Copyright 2013 BigML
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.streaminer.stream.histogram.spdt;

import org.streaminer.stream.histogram.spdt.Histogram.TargetType;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.json.simple.JSONArray;

public class GroupTarget extends Target<GroupTarget> {
  
  public GroupTarget(ArrayList<Target> group) {
    _target = group;
  }
  
  public GroupTarget(Collection<Object> values, Collection<TargetType> types) {
    ArrayList<Target> group = new ArrayList<Target>();
    
    if (types == null) {
      for (Object value : values) {
        Target target;
        if (value instanceof Number) {
          Double tVal = (value == null ? null : ((Number) value).doubleValue());
          target = new NumericTarget(tVal);
        } else {
          target = new MapCategoricalTarget(value);
        }
        group.add(target);
      }
    } else {
      Target target;
      Iterator<Object> valueIter = values.iterator();
      Iterator<TargetType> typeIter = types.iterator();
      while (valueIter.hasNext()) {
        Object value = valueIter.next();
        TargetType type = typeIter.next();
        if (type == TargetType.numeric) {
          Double tVal = (value == null ? null : ((Number) value).doubleValue());
          target = new NumericTarget(tVal);
        } else {
          target = new MapCategoricalTarget(value);
        }
        group.add(target);
      }
    }
    _target = group;
  }
  
  public ArrayList<Target> getGroupTarget() {
    return _target;
  }

  /* Missing values not allowed for GroupTarget */
  @Override
  public double getMissingCount() {
    return 0;
  }

  @Override
  public TargetType getTargetType() {
    return Histogram.TargetType.group;
  }

  @Override
  protected void addJSON(JSONArray binJSON, DecimalFormat format) {
    JSONArray targetsJSON = new JSONArray();
    for (Target target : _target) {
      target.addJSON(targetsJSON, format);
    }
    binJSON.add(targetsJSON);
  }
  
  @Override
  protected GroupTarget sum(GroupTarget group) {
    for (int i = 0; i < _target.size(); i++) {
      _target.get(i).sum(group.getGroupTarget().get(i));
    }
    return this;
  }

  @Override
  protected GroupTarget mult(double multiplier) {
    for (Target target : _target) {
      target.mult(multiplier);
    }    
    return this;
  }

  @Override
  protected GroupTarget clone() {
    ArrayList<Target> newGroup = new ArrayList<Target>();
    for (Target target : _target) {
      newGroup.add(target.clone());
    }
    return new GroupTarget(new ArrayList<Target>(newGroup));
  }

  @Override
  protected GroupTarget init() {
    ArrayList<Target> newGroup = new ArrayList<Target>();
    for (Target target : _target) {
      newGroup.add(target.init());
    }
    return new GroupTarget(new ArrayList<Target>(newGroup));
  }
  
  private ArrayList<Target> _target;
}
