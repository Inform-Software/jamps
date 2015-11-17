/*
 * Copyright (C) 2015 The Jamps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.inform.jamps.solver.gurobi;

import java.util.concurrent.atomic.AtomicLong;

import com.inform.jamps.modeling.Variable;
import com.inform.jamps.modeling.VariableType;

import gurobi.GRBVar;

public class GurobiVariable implements Variable {

  protected final static VariableType DEFAULT_VARIABLE_TYPE = VariableType.CONTINUOUS;

  private final static AtomicLong     AUTO_NAME_COUNTER     = new AtomicLong (0);

  private final GurobiProgram         program;

  private final String                name;

  private final VariableType          type;

  private double                      lowerBound;

  private double                      upperBound;

  private Double                      initialValue;

  private transient GRBVar            nativeVar;

  protected GurobiVariable (final GurobiProgram program) {
    this (program, DEFAULT_VARIABLE_TYPE);
  }

  protected GurobiVariable (final GurobiProgram program,
                            final VariableType type) {
    this (program, "x" + AUTO_NAME_COUNTER.incrementAndGet (), type);
  }

  protected GurobiVariable (final GurobiProgram program,
                            final String name,
                            final VariableType type) {
    if (program == null) {
      throw new IllegalArgumentException ("Parameter program is mandatory and may not be null");
    }
    if (name == null) {
      throw new IllegalArgumentException ("Parameter name is mandatory and may not be null");
    }
    if (type == null) {
      throw new IllegalArgumentException ("Parameter type is mandatory and may not be null");
    }

    this.program = program;
    this.name = name;
    this.type = type;

    if (type == VariableType.BINARY) {
      lowerBound = 0.0;
      upperBound = 1.0;
    } else {
      lowerBound = Double.NEGATIVE_INFINITY;
      upperBound = Double.POSITIVE_INFINITY;
    }
  }

  @Override
  public String getName () {
    return name;
  }

  @Override
  public VariableType getType () {
    return type;
  }

  @Override
  public double getLowerBound () {
    return lowerBound;
  }

  @Override
  public void setLowerBound (final double lowerBound) {
    this.lowerBound = lowerBound;
  }

  @Override
  public double getUpperBound () {
    return upperBound;
  }

  @Override
  public void setUpperBound (final double upperBound) {
    this.upperBound = upperBound;
  }

  @Override
  public boolean hasInitialValue () {
    return initialValue != null;
  }

  @Override
  public double getInitialValue () {
    if (!hasInitialValue ()) {
      throw new IllegalStateException ("Variable has no initial value set");
    }
    return initialValue;
  }

  @Override
  public void setInitialValue (final double initialValue) {
    this.initialValue = initialValue;
  }

  protected GurobiProgram getProgram () {
    return program;
  }

  protected void setNativeVariable (final GRBVar grbVar) {
    if (grbVar == null) {
      throw new IllegalArgumentException ("GRBVar parameter is mandatory and may not be null");
    }

    this.nativeVar = grbVar;
  }

  protected GRBVar getNativeVariable () {
    return nativeVar;
  }

  @Override
  public int compareTo (final Variable var) {
    if (!(var instanceof GurobiVariable)) {
      return -1;
    }

    int result = name.compareTo (var.getName ());
    if (result != 0) {
      return result;
    }

    result = type.compareTo (var.getType ());
    if (result != 0) {
      return result;
    }

    result = Double.valueOf (lowerBound).compareTo (var.getLowerBound ());
    if (result != 0) {
      return result;
    }

    return Double.valueOf (upperBound).compareTo (var.getUpperBound ());
  }

  @Override
  public final int hashCode () {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode ();
    result = prime * result + type.hashCode ();
    long temp;
    temp = Double.doubleToLongBits (lowerBound);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits (upperBound);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public final boolean equals (final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof GurobiVariable)) {
      return false;
    }
    final GurobiVariable other = (GurobiVariable) obj;
    if (!name.equals (other.name)) {
      return false;
    }
    if (type != other.type) {
      return false;
    }
    if (Double.doubleToLongBits (lowerBound) != Double.doubleToLongBits (other.lowerBound)) {
      return false;
    }
    if (Double.doubleToLongBits (upperBound) != Double.doubleToLongBits (other.upperBound)) {
      return false;
    }
    return true;
  }
}
