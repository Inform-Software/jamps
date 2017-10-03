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

import com.inform.jamps.modeling.Constraint;
import com.inform.jamps.modeling.Expression;
import com.inform.jamps.modeling.Operator;

import gurobi.GRBConstr;

public class GurobiConstraint implements Constraint {

  protected final static Operator DEFAULT_OPERATOR  = Operator.EQUALS;

  private final static AtomicLong AUTO_NAME_COUNTER = new AtomicLong (0);

  private final GurobiLinearExpression  lhs;

  private final GurobiLinearExpression  rhs;

  private final GurobiProgram     program;

  private final String            name;

  private Operator                operator;

  private GRBConstr               nativeConstraint;

  protected GurobiConstraint (final GurobiProgram program) {
    this (program, DEFAULT_OPERATOR);
  }

  protected GurobiConstraint (final GurobiProgram program,
                              final Operator operator) {
    this (program, "constr" + AUTO_NAME_COUNTER.incrementAndGet (), operator);
  }

  protected GurobiConstraint (final GurobiProgram program,
                              final String name,
                              final Operator operator) {
    if (program == null) {
      throw new IllegalArgumentException ("Parameter program is mandatory and may not be null");
    }
    if (name == null) {
      throw new IllegalArgumentException ("Parameter name is mandatory and may not be null");
    }
    if (operator == null) {
      throw new IllegalArgumentException ("Parameter operator is mandatory and may not be null");
    }

    this.name = name;
    this.program = program;
    this.operator = operator;
    this.lhs = new GurobiLinearExpression (this);
    this.rhs = new GurobiLinearExpression (this);
  }

  @Override
  public String getName () {
    return name;
  }

  @Override
  public Expression getRhs () {
    return rhs;
  }

  @Override
  public Expression getLhs () {
    return lhs;
  }

  @Override
  public Operator getOperator () {
    return operator;
  }

  @Override
  public void setOperator (final Operator operator) {
    if (operator == null) {
      throw new IllegalArgumentException ("Parameter operator is mandatory and may not be null");
    }

    this.operator = operator;
  }

  protected GurobiProgram getProgram () {
    return program;
  }

  protected void setNativeConstraint (final GRBConstr grbConstr) {
    if (grbConstr == null) {
      throw new IllegalArgumentException ("GRBConstr parameter is mandatory and may not be null");
    }

    this.nativeConstraint = grbConstr;
  }

  protected GRBConstr getNativeConstraint () {
    return nativeConstraint;
  }

  @Override
  public int compareTo (final Constraint o) {
    if (!(o instanceof GurobiConstraint)) {
      return -1;
    }

    final GurobiConstraint grbConstr = (GurobiConstraint) o;
    int result = operator.compareTo (grbConstr.operator);
    if (result != 0) {
      return result;
    }

    result = lhs.compareTo (grbConstr.lhs);
    if (result != 0) {
      return result;
    }

    result = rhs.compareTo (grbConstr.rhs);
    if (result != 0) {
      return result;
    }

    return name.compareTo (grbConstr.name);
  }

  @Override
  public final int hashCode () {
    final int prime = 31;
    int result = 1;
    result = prime * result + operator.hashCode ();
    result = prime * result + name.hashCode ();
    result = prime * result + lhs.hashCode ();
    result = prime * result + rhs.hashCode ();
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
    if (!(obj instanceof GurobiConstraint)) {
      return false;
    }
    final GurobiConstraint other = (GurobiConstraint) obj;
    if (operator != other.operator) {
      return false;
    }
    if (!name.equals (other.name)) {
      return false;
    }
    if (!lhs.equals (other.lhs)) {
      return false;
    }
    return rhs.equals (other.rhs);
  }

  @Override
  public String toString () {
    final StringBuilder sb = new StringBuilder (1000);
    sb.append (name).append (": ").append (lhs);
    switch (operator) {
      case EQUALS:
        sb.append (" = ");
        break;
      case GREATER_EQUALS:
        sb.append (" >= ");
        break;
      case LESS_EQUALS:
        sb.append (" <= ");
        break;
      default:
        break;
    }
    sb.append (rhs);
    return sb.toString ();
  }
}
