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

import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inform.jamps.exception.ProgramException;
import com.inform.jamps.modeling.Variable;
import com.inform.jamps.modeling.VariableType;

import gurobi.GRB;
import gurobi.GRB.CharAttr;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.StringAttr;
import gurobi.GRBException;
import gurobi.GRBVar;

public class GurobiVariable implements Variable {

  private static final Logger     LOG               = LoggerFactory.getLogger (GurobiVariable.class);

  private final static AtomicLong AUTO_NAME_COUNTER = new AtomicLong (0);

  private final GRBVar            nativeVar;

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

    final char nativeType = GurobiUtils.convertToNativeType (type);

    try {
      if (type == VariableType.BINARY) {
        this.nativeVar = program.getNativeModel ().addVar (0.0, 1.0, 0.0, nativeType, name);
      } else {
        this.nativeVar = program.getNativeModel ().addVar (Double.NEGATIVE_INFINITY,
                                                           Double.POSITIVE_INFINITY,
                                                           0.0,
                                                           nativeType,
                                                           name);
      }
    } catch (GRBException e) {
      LOG.error ("Unable to create native gurobi variable", e);
      throw new ProgramException ("Unable to create native gurobi variable", e);
    }
  }

  @Override
  public String getName () {
    try {
      return nativeVar.get (StringAttr.VarName);
    } catch (GRBException e) {
      LOG.error ("Unable to get name from native gurobi variable", e);
      throw new ProgramException ("Unable to get name from native gurobi variable", e);
    }
  }

  @Override
  public VariableType getType () {
    try {
      return GurobiUtils.convertFromNativeType (nativeVar.get (CharAttr.VType));
    } catch (Exception e) {
      LOG.error ("Unable to get type from native gurobi variable", e);
      throw new ProgramException ("Unable to get type from native gurobi variable", e);
    }
  }

  @Override
  public double getLowerBound () {
    try {
      return GurobiUtils.convertInfinity (nativeVar.get (DoubleAttr.LB));
    } catch (GRBException e) {
      LOG.error ("Unable to get lower bound from native gurobi variable", e);
      throw new ProgramException ("Unable to get lower bound from native gurobi variable", e);
    }
  }

  @Override
  public void setLowerBound (final double lowerBound) {
    try {
      nativeVar.set (DoubleAttr.LB, lowerBound);
    } catch (GRBException e) {
      LOG.error ("Unable to set lower bound to native gurobi variable", e);
      throw new ProgramException ("Unable to set lower bound to native gurobi variable", e);
    }
  }

  @Override
  public double getUpperBound () {
    try {
      return GurobiUtils.convertInfinity (nativeVar.get (DoubleAttr.UB));
    } catch (GRBException e) {
      LOG.error ("Unable to get upper bound from native gurobi variable", e);
      throw new ProgramException ("Unable to get upper bound from native gurobi variable", e);
    }
  }

  @Override
  public void setUpperBound (final double upperBound) {
    try {
      nativeVar.set (DoubleAttr.UB, upperBound);
    } catch (GRBException e) {
      LOG.error ("Unable to set upper bound to native gurobi variable", e);
      throw new ProgramException ("Unable to set upper bound to native gurobi variable", e);
    }
  }

  @Override
  public boolean hasInitialValue () {
    try {
      final double startValue = nativeVar.get (DoubleAttr.Start);
      return !Precision.equals (startValue, GRB.UNDEFINED);
    } catch (GRBException e) {
      LOG.error ("Unable to get initial value from native gurobi variable", e);
      throw new ProgramException ("Unable to get initial value from native gurobi variable", e);
    }
  }

  @Override
  public double getInitialValue () {
    try {
      final double startValue = nativeVar.get (DoubleAttr.Start);
      if (Precision.equals (startValue, GRB.UNDEFINED)) {
        throw new IllegalStateException ("Variable has no initial value set");
      } else {
        return GurobiUtils.convertInfinity (startValue);
      }
    } catch (GRBException e) {
      LOG.error ("Unable to get initial value from native gurobi variable", e);
      throw new ProgramException ("Unable to get initial value from native gurobi variable", e);
    }
  }

  @Override
  public void setInitialValue (final double initialValue) {
    try {
      nativeVar.set (DoubleAttr.Start, initialValue);
    } catch (GRBException e) {
      LOG.error ("Unable to set initial value to native gurobi variable", e);
      throw new ProgramException ("Unable to set initial value to native gurobi variable", e);
    }
  }

  protected GRBVar getNativeVariable () {
    return nativeVar;
  }

  @Override
  public final int hashCode () {
    return nativeVar.hashCode ();
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
    return nativeVar.sameAs (other.nativeVar);
  }

  @Override
  public String toString () {
    final StringBuilder sb = new StringBuilder (200);
    switch (getType ()) {
      case BINARY:
        sb.append ("Binary ");
        break;
      case CONTINUOUS:
        sb.append ("Continuous ");
        break;
      case INTEGER:
        sb.append ("Integer ");
        break;
      case SEMI_CONTINUOUS:
        sb.append ("Semi-Continuous ");
        break;
      case SEMI_INTEGER:
        sb.append ("Semi-Integer ");
        break;
      default:
        break;
    }
    sb.append (getName ());
    final double lowerBound = getLowerBound ();
    final double upperBound = getUpperBound ();
    if (lowerBound > Double.NEGATIVE_INFINITY || upperBound < Double.POSITIVE_INFINITY) {
      if (Precision.equals (lowerBound, Double.NEGATIVE_INFINITY)) {
        sb.append (" (,");
      } else {
        sb.append (" [");
        sb.append (lowerBound);
        sb.append (',');
      }
      if (Precision.equals (upperBound, Double.POSITIVE_INFINITY)) {
        sb.append (')');
      } else {
        sb.append (' ');
        sb.append (upperBound);
        sb.append (']');
      }
    } else {
      sb.append (" (unbounded)");
    }
    return sb.toString ();
  }
}
