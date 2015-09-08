/*
 * Copyright (C) 2015 The Jamps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inform.jamps.solver.gurobi;

import com.inform.jamps.modeling.LinearTerm;
import com.inform.jamps.modeling.Variable;

public class GurobiLinearTerm implements LinearTerm {

  private double               coefficient;

  private final GurobiVariable variable;

  protected GurobiLinearTerm (final double coefficient,
                              final GurobiVariable variable) {
    if (variable == null) {
      throw new IllegalArgumentException ("Parameter variable is mandatory and may not be null");
    }

    this.coefficient = coefficient;
    this.variable = variable;
  }

  @Override
  public double getCoefficient () {
    return coefficient;
  }

  @Override
  public Variable getVariable () {
    return variable;
  }

  protected void addCoefficient (final double coefficient) {
    this.coefficient = this.coefficient + coefficient;
  }

  @Override
  public int compareTo (final LinearTerm o) {
    if (o == null) {
      return -1;
    }

    final int result = variable.compareTo (o.getVariable ());
    if (result != 0) {
      return result;
    }

    return Double.valueOf (coefficient).compareTo (o.getCoefficient ());
  }

  @Override
  public final int hashCode () {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits (coefficient);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + variable.hashCode ();
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
    if (!(obj instanceof GurobiLinearTerm)) {
      return false;
    }
    final GurobiLinearTerm other = (GurobiLinearTerm) obj;
    if (Double.doubleToLongBits (coefficient) != Double.doubleToLongBits (other.coefficient)) {
      return false;
    }
    if (!variable.equals (other.variable)) {
      return false;
    }
    return true;
  }
}
