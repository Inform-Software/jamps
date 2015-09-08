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

import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.inform.jamps.modeling.Expression;
import com.inform.jamps.modeling.LinearTerm;
import com.inform.jamps.modeling.QuadraticTerm;
import com.inform.jamps.modeling.Variable;

public class GurobiExpression implements Expression {

  private final GurobiObjective                       objective;

  private final GurobiConstraint                      constraint;

  private final Map<GurobiVariable, GurobiLinearTerm> linearTerms = new TreeMap<GurobiVariable, GurobiLinearTerm> ();

  private double                                      constant    = 0.0;

  protected GurobiExpression (final GurobiObjective objective) {
    if (objective == null) {
      throw new IllegalArgumentException ("Parameter objective is mandatory and may not be null");
    }

    this.objective = objective;
    this.constraint = null;
  }

  protected GurobiExpression (final GurobiConstraint constraint) {
    if (constraint == null) {
      throw new IllegalArgumentException ("Parameter constraint is mandatory and may not be null");
    }

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
      return 0.0;
    } else {
      return term.getCoefficient ();
    }
  }

  @Override
  public double getCoefficient (final Variable var1,
                                final Variable var2) {
    return 0.0;
  }

  @Override
  public Expression addTerm (final double coefficient,
                             final Variable variable) {
    if (coefficient == 0.0) {
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
    if (constant == 0.0) {
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
    for (LinearTerm term: linearTerms) {
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
    this.constant = 0.0;
    return this;
  }

  protected GRBLinExpr getNativeExpression () {
    final GRBLinExpr expr = new GRBLinExpr ();

    GRBVar[] vars = new GRBVar[linearTerms.size ()];
    final double[] coeffs = new double[linearTerms.size ()];

    int index = 0;
    for (Entry<GurobiVariable, GurobiLinearTerm> entry: linearTerms.entrySet ()) {
      vars[index] = entry.getKey ().getNativeVariable ();
      coeffs[index] = entry.getValue ().getCoefficient ();
      index++;
    }

    expr.addConstant (constant);

    try {
      expr.addTerms (coeffs, vars);
    } catch (GRBException e) {
      throw new IllegalStateException ("Unable to create native linear expression", e);
    }

    return expr;
  }

  @Override
  public int compareTo (final Expression expr) {
    if (!(expr instanceof GurobiExpression)) {
      return -1;
    }
    if (equals (expr)) {
      return 0;
    }

    final GurobiExpression grbExpr = ((GurobiExpression) expr);
    final int exprLength1 = linearTerms.size () + ((constant == 0.0) ? 0 : 1);
    final int exprLength2 = grbExpr.linearTerms.size () + ((grbExpr.constant == 0.0) ? 0 : 1);

    final int result = Integer.valueOf (exprLength1).compareTo (exprLength2);
    if (result != 0) {
      return result;
    }

    return Double.valueOf (constant).compareTo (grbExpr.constant);
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
    if (!(obj instanceof GurobiExpression)) {
      return false;
    }
    final GurobiExpression other = (GurobiExpression) obj;
    if (Double.doubleToLongBits (constant) != Double.doubleToLongBits (other.constant)) {
      return false;
    }
    if (!linearTerms.equals (other.linearTerms)) {
      return false;
    }
    return true;
  }

}
