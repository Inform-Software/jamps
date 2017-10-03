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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math3.util.Precision;

import com.inform.jamps.modeling.Expression;
import com.inform.jamps.modeling.LinearExpression;
import com.inform.jamps.modeling.LinearTerm;
import com.inform.jamps.modeling.QuadraticTerm;
import com.inform.jamps.modeling.Variable;

import gurobi.GRBLinExpr;

public class GurobiLinearExpression implements LinearExpression {

  private static final double ZERO_COEFFICIENT = 0.0;

  private final GRBLinExpr    expression;

  protected GurobiLinearExpression (final GurobiProgram program) {
    this.objective = null;
    this.constraint = constraint;
  }

  @Override
  public List<LinearTerm> getLinearTerms () {
    return new ArrayList<LinearTerm> (linearTerms.values ());
  }

  @Override
  public List<QuadraticTerm> getQuadraticTerms () {
    return Collections.emptyList ();
  }

  @Override
  public double getConstant () {
    return constant;
  }

  @Override
  public double getCoefficient (final Variable variable) {
    final GurobiLinearTerm term = linearTerms.get (variable);

    if (term == null) {
      return ZERO_COEFFICIENT;
    } else {
      return term.getCoefficient ();
    }
  }

  @Override
  public double getCoefficient (final Variable var1,
                                final Variable var2) {
    return ZERO_COEFFICIENT;
  }

  @Override
  public Expression addTerm (final double coefficient,
                             final Variable variable) {
    if (Precision.equals (coefficient, ZERO_COEFFICIENT)) {
      return this;
    }

    if (!(variable instanceof GurobiVariable)) {
      throw new IllegalArgumentException ("Adding variable " + variable.getName () +
                                          " of type not equal GurobiVariable is not supported");
    }

    final GurobiProgram varProgram = ((GurobiVariable) variable).getProgram ();
    final GurobiProgram ownProgram = (objective == null) ? constraint.getProgram () : objective.getProgram ();
    if (varProgram == null || !varProgram.equals (ownProgram)) {
      throw new IllegalArgumentException ("Adding variable " + variable.getName () +
                                          " from a different program is not supported");
    }

    final GurobiLinearTerm term = linearTerms.get (variable);

    if (term == null) {
      final GurobiLinearTerm newTerm = new GurobiLinearTerm (coefficient, (GurobiVariable) variable);
      linearTerms.put ((GurobiVariable) variable, newTerm);
    } else {
      term.addCoefficient (coefficient);
    }

    return this;
  }

  @Override
  public Expression addTerm (final double constant) {
    if (Precision.equals (constant, ZERO_COEFFICIENT)) {
      return this;
    }

    this.constant += constant;
    return this;
  }

  @Override
  public Expression addTerm (final double coefficient,
                             final Variable var1,
                             final Variable var2) {
    throw new UnsupportedOperationException ("Quadratic terms are not supported by Gurobi solver");
  }

  @Override
  public Expression addTerms (final Expression expr) {
    final List<LinearTerm> linearTerms = expr.getLinearTerms ();
    for (final LinearTerm term: linearTerms) {
      addTerm (term.getCoefficient (), term.getVariable ());
    }

    addTerm (expr.getConstant ());

    return this;
  }

  @Override
  public Expression removeLinearTerm (final Variable variable) {
    linearTerms.remove (variable);
    return this;
  }

  @Override
  public Expression removeQuadraticTerm (final Variable var1,
                                         final Variable var2) {
    throw new UnsupportedOperationException ("Quadratic terms are not supported by Gurobi solver");
  }

  @Override
  public Expression removeConstant () {
    this.constant = ZERO_COEFFICIENT;
    return this;
  }

  protected GRBLinExpr getNativeExpression () {
    return expression;
  }

  @Override
  public int compareTo (final Expression expr) {
    if (!(expr instanceof GurobiLinearExpression)) {
      return -1;
    }
    if (equals (expr)) {
      return 0;
    }

    final GurobiLinearExpression grbExpr = ((GurobiLinearExpression) expr);
    final int exprLength1 = linearTerms.size () + (Precision.equals (constant, ZERO_COEFFICIENT) ? 0 : 1);
    final int exprLength2 = grbExpr.linearTerms.size () +
                            (Precision.equals (grbExpr.constant, ZERO_COEFFICIENT) ? 0 : 1);

    final int result = Integer.valueOf (exprLength1).compareTo (exprLength2);
    if (result != 0) {
      return result;
    }

    return Double.compare (constant, grbExpr.constant);
  }

  @Override
  public final int hashCode () {
    final int prime = 31;
    int result = 1;
    result = prime * result + linearTerms.hashCode ();
    long temp;
    temp = Double.doubleToLongBits (constant);
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
    if (!(obj instanceof GurobiLinearExpression)) {
      return false;
    }
    final GurobiLinearExpression other = (GurobiLinearExpression) obj;
    if (Double.doubleToLongBits (constant) != Double.doubleToLongBits (other.constant)) {
      return false;
    }
    return linearTerms.equals (other.linearTerms);
  }

  @Override
  public String toString () {
    if (linearTerms.isEmpty ()) {
      return String.valueOf (constant);
    }

    final StringBuilder sb = new StringBuilder (2000);
    for (final Entry<GurobiVariable, GurobiLinearTerm> entry: linearTerms.entrySet ()) {
      final GurobiLinearTerm term = entry.getValue ();
      final double coefficient = term.getCoefficient ();
      final double abs = Math.abs (coefficient);

      if (sb.length () > 0 || coefficient < 0.0) {
        sb.append (' ');
        sb.append (coefficient < ZERO_COEFFICIENT ? '-' : '+');
        sb.append (' ');
      }

      if (!Precision.equals (abs, 1.0)) {
        sb.append (abs);
        sb.append (' ');
      }
      sb.append (term.getVariable ().getName ());
    }

    if (constant < ZERO_COEFFICIENT) {
      sb.append (" - ");
      sb.append (Math.abs (constant));
    } else if (constant > ZERO_COEFFICIENT) {
      sb.append (" + ");
      sb.append (constant);
    }

    return sb.toString ();
  }
}
