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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.inform.jamps.modeling.LinearTerm;
import com.inform.jamps.modeling.Objective;
import com.inform.jamps.modeling.ObjectiveSense;
import com.inform.jamps.modeling.Variable;
import com.inform.jamps.solver.Solution;

public class GurobiSolution implements Solution {

  private final GurobiProgram               program;

  private final boolean                     optimal;

  private final Map<GurobiVariable, Double> variableValues       = new HashMap<GurobiVariable, Double> ();

  private final Map<Objective, Double>      objectiveValuesCache = new ConcurrentHashMap<Objective, Double> ();

  private Double                            objectiveValue;

  private Double                            bound;

  private double                            gap                  = Double.POSITIVE_INFINITY;

  protected GurobiSolution (final GurobiProgram program) {
    this (program, false);
  }

  protected GurobiSolution (final GurobiProgram program,
                            final boolean optimal) {
    if (program == null) {
      throw new IllegalArgumentException ("Parameter program is mandatory and may not be null");
    }
    this.optimal = optimal;
    this.program = program;
  }

  @Override
  public boolean isOptimal () {
    return optimal;
  }

  @Override
  public double getVariableValue (final Variable variable) {
    if (variable == null) {
      throw new IllegalArgumentException ("Parameter variable is mandantory and may not be null");
    }

    final Double value = variableValues.get (variable);
    if (value == null) {
      return 0.0;
    }
    return value;
  }

  @Override
  public boolean getBinaryValue (final Variable variable) {
    final double value = getVariableValue (variable);
    return value > 0.0;
  }

  @Override
  public long getIntegerValue (final Variable variable) {
    final double value = getVariableValue (variable);
    return Math.round (value);
  }

  @Override
  public double getObjectiveValue () {
    if (objectiveValue == null) {
      final ObjectiveSense programSense = program.determineProgramObjectiveSense ();
      double sum = 0.0;

      final List<GurobiObjective> objectives = program.getObjectives ();
      for (final GurobiObjective objective: objectives) {
        if (objective.getObjectiveSense () == programSense) {
          sum += getObjectiveValue (objective);
        } else {
          sum -= getObjectiveValue (objective);
        }
      }

      objectiveValue = sum;
      calculateGap ();
    }

    return objectiveValue;
  }

  @Override
  public double getObjectiveValue (final Objective objective) {
    if (objective == null) {
      throw new IllegalArgumentException ("Parameter objective is mandantory and may not be null");
    }

    final Double cachedValue = objectiveValuesCache.get (objective);
    if (cachedValue != null) {
      return cachedValue;
    }

    final List<LinearTerm> linearTerms = objective.getExpression ().getLinearTerms ();
    double objectiveValue = objective.getExpression ().getConstant ();
    for (final LinearTerm term: linearTerms) {
      objectiveValue += term.getCoefficient () * getVariableValue (term.getVariable ());
    }

    objectiveValuesCache.put (objective, objectiveValue);
    return objectiveValue;
  }

  @Override
  public double getRelativeOptimalityGap () {
    return gap;
  }

  protected void setVariableValue (final GurobiVariable variable,
                                   final double value) {
    if (variable == null) {
      throw new IllegalArgumentException ("Parameter variable is mandantory and may not be null");
    }

    if (value == -0.0) {
      variableValues.put (variable, 0.0);
    } else {
      variableValues.put (variable, value);
    }
  }

  protected GurobiProgram getProgram () {
    return program;
  }

  protected void setBestObjectiveBound (final double bound) {
    this.bound = bound;
    calculateGap ();
  }

  private void calculateGap () {
    if (bound == null || objectiveValue == null) {
      return;
    }

    gap = Math.abs (bound - objectiveValue) / Math.abs (objectiveValue);
  }

  @Override
  public int compareTo (final Solution o) {
    if (o == null) {
      return -1;
    }

    return Double.compare (getObjectiveValue (), o.getObjectiveValue ());
  }

  @Override
  public final int hashCode () {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bound == null) ? 0 : bound.hashCode ());
    result = prime * result + (optimal ? 1231 : 1237);
    result = prime * result + variableValues.hashCode ();
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
    if (!(obj instanceof GurobiSolution)) {
      return false;
    }
    final GurobiSolution other = (GurobiSolution) obj;
    if (optimal != other.optimal) {
      return false;
    }
    if (bound == null) {
      if (other.bound != null) {
        return false;
      }
    } else if (!bound.equals (other.bound)) {
      return false;
    }
    return variableValues.equals (other.variableValues);
  }

}
