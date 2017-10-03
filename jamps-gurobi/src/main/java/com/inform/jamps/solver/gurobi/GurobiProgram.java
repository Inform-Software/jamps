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
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inform.jamps.exception.DuplicateEntryException;
import com.inform.jamps.exception.ProgramException;
import com.inform.jamps.modeling.Constraint;
import com.inform.jamps.modeling.Objective;
import com.inform.jamps.modeling.ObjectiveSense;
import com.inform.jamps.modeling.Operator;
import com.inform.jamps.modeling.Program;
import com.inform.jamps.modeling.Variable;
import com.inform.jamps.modeling.VariableType;

import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.StringAttr;
import gurobi.GRBConstr;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class GurobiProgram implements Program {

  private static final Logger          LOG               = LoggerFactory.getLogger (GurobiProgram.class);

  private final static AtomicLong      AUTO_NAME_COUNTER = new AtomicLong (0);

  private final List<GurobiVariable>   variables         = new ArrayList<GurobiVariable> ();

  private final List<GurobiObjective>  objectives        = new ArrayList<GurobiObjective> ();

  private final List<GurobiConstraint> constraints       = new ArrayList<GurobiConstraint> ();

  private final GurobiSolverParameters parameters;

  private final GRBModel               nativeModel;

  protected GurobiProgram (final GurobiSolverParameters parameters) {
    this (parameters, "program" + AUTO_NAME_COUNTER.incrementAndGet ());
  }

  protected GurobiProgram (final GurobiSolverParameters parameters,
                           final String name) {
    this.parameters = parameters;
    try {
      this.nativeModel = new GRBModel (parameters.getNativeEnvironment ());
    } catch (GRBException e) {
      LOG.error ("Unable to create native gurobi model", e);
      throw new ProgramException ("Unable to create native gurobi model", e);
    }
    setName (name);
  }

  @Override
  public String getName () {
    try {
      return nativeModel.get (StringAttr.ModelName);
    } catch (GRBException e) {
      LOG.error ("Unable to get name from native gurobi model", e);
      throw new ProgramException ("Unable to get name from native gurobi model", e);
    }
  }

  @Override
  public void setName (final String name) {
    if (name == null) {
      throw new IllegalArgumentException ("Parameter name is mandatory and may not be null");
    }

    try {
      nativeModel.set (StringAttr.ModelName, name);
    } catch (GRBException e) {
      LOG.error ("Unable to set name to native gurobi model", e);
      throw new ProgramException ("Unable to set name to native gurobi model", e);
    }
  }

  @Override
  public int getVariablesCount () {
    return variables.size ();
  }

  @Override
  public int getObjectivesCount () {
    return objectives.size ();
  }

  @Override
  public int getConstraintsCount () {
    return constraints.size ();
  }

  @Override
  public Variable addVariable (final String name,
                               final VariableType variableType,
                               final double lowerBound,
                               final double upperBound) {
    final GurobiVariable variable = new GurobiVariable (this, name, variableType);
    variable.setLowerBound (lowerBound);
    variable.setUpperBound (upperBound);
    return addVariable (variable);
  }

  @Override
  public Variable addVariable (final VariableType variableType,
                               final double lowerBound,
                               final double upperBound) {
    final GurobiVariable variable = new GurobiVariable (this, variableType);
    variable.setLowerBound (lowerBound);
    variable.setUpperBound (upperBound);
    return addVariable (variable);
  }

  @Override
  public Variable addVariable (final String name,
                               final VariableType variableType) {
    return addVariable (new GurobiVariable (this, name, variableType));
  }

  @Override
  public Variable addVariable (final VariableType variableType) {
    return addVariable (new GurobiVariable (this, variableType));
  }

  @Override
  public Variable addVariable () {
    return addVariable (new GurobiVariable (this));
  }

  protected GurobiVariable addVariable (final GurobiVariable var) {
    if (variables.add (var)) {
      return var;
    } else {
      throw new DuplicateEntryException ("Variable already exists in this programm");
    }
  }

  @Override
  public Objective addObjective (final String name,
                                 final ObjectiveSense sense) {
    return addObjective (new GurobiObjective (this, name, sense));
  }

  @Override
  public Objective addObjective (final ObjectiveSense sense) {
    return addObjective (new GurobiObjective (this, sense));
  }

  @Override
  public Objective addObjective () {
    return addObjective (new GurobiObjective (this));
  }

  protected GurobiObjective addObjective (final GurobiObjective obj) {
    objectives.add (obj);
    return obj;
  }

  @Override
  public Constraint addConstraint (final String name,
                                   final Operator operator) {
    return addConstraint (new GurobiConstraint (this, name, operator));
  }

  @Override
  public Constraint addConstraint (final Operator operator) {
    return addConstraint (new GurobiConstraint (this, operator));
  }

  @Override
  public Constraint addConstraint () {
    return addConstraint (new GurobiConstraint (this));
  }

  protected GurobiConstraint addConstraint (final GurobiConstraint constr) {
    constraints.add (constr);
    return constr;
  }

  protected List<GurobiVariable> getVariables () {
    return Collections.unmodifiableList (new ArrayList<GurobiVariable> (variables));
  }

  protected List<GurobiObjective> getObjectives () {
    return Collections.unmodifiableList (objectives);
  }

  protected List<GurobiConstraint> getConstraints () {
    return Collections.unmodifiableList (constraints);
  }

  protected GRBModel getNativeModel () {
    return nativeModel;
  }

  protected GRBModel initialCreateNativeModel () {
    if (nativeEnvironment == null) {
      throw new IllegalStateException ("Native environment must be set before creating a native model");
    }

    final ObjectiveSense programSense = determineProgramObjectiveSense ();
    final GRBModel model = createNativeModel (programSense);
    addVarsAndObjectivesToModel (model, programSense);
    addConstraintsToModel (model);
    return model;
  }

  protected GRBModel createNativeModel (final ObjectiveSense programSense) {
    try {
      final GRBModel model = new GRBModel (nativeEnvironment);
      model.set (StringAttr.ModelName, name);

      if (programSense == ObjectiveSense.MINIMIZE) {
        model.set (GRB.IntAttr.ModelSense, 1);
      } else {
        model.set (GRB.IntAttr.ModelSense, -1);
      }

      return model;
    } catch (GRBException e) {
      throw new IllegalStateException ("Unable to create native model", e);
    }
  }

  protected ObjectiveSense determineProgramObjectiveSense () {
    if (objectives.isEmpty ()) {
      throw new IllegalStateException ("Program must contain at least one objective");
    }
    return objectives.get (0).getObjectiveSense ();
  }

  protected void addVarsAndObjectivesToModel (final GRBModel model,
                                              final ObjectiveSense programSense) {
    final String[] varNames = new String[variables.size ()];
    final char[] nativeTypes = new char[variables.size ()];
    final double[] lowerBounds = new double[variables.size ()];
    final double[] upperBounds = new double[variables.size ()];
    final double[] objectiveCoefficients = new double[variables.size ()];

    int count = 0;
    for (final GurobiVariable var: variables) {
      double objective = 0.0;
      for (final GurobiObjective obj: objectives) {
        double factor = 1.0;
        if (obj.getObjectiveSense () != programSense) {
          factor = -1.0;
        }

        objective += factor * obj.getExpression ().getCoefficient (var);
      }

      char nativeType = '\0';
      switch (var.getType ()) {
        case CONTINUOUS:
          nativeType = GRB.CONTINUOUS;
          break;
        case BINARY:
          nativeType = GRB.BINARY;
          break;
        case INTEGER:
          nativeType = GRB.INTEGER;
          break;
        case SEMI_CONTINUOUS:
          nativeType = GRB.SEMICONT;
          break;
        case SEMI_INTEGER:
          nativeType = GRB.SEMIINT;
          break;
        default:
          throw new IllegalStateException ("Variables of type " + var.getType ().name () + " are not supported");
      }

      varNames[count] = var.getName ();
      nativeTypes[count] = nativeType;
      lowerBounds[count] = var.getLowerBound ();
      upperBounds[count] = var.getUpperBound ();
      objectiveCoefficients[count] = objective;
      count++;
    }

    try {
      final GRBVar[] vars = model.addVars (lowerBounds, upperBounds, objectiveCoefficients, nativeTypes, varNames);

      count = 0;
      for (final GurobiVariable var: variables) {
        if (var.hasInitialValue ()) {
          vars[count].set (DoubleAttr.Start, var.getInitialValue ());
        }

        var.setNativeVariable (vars[count]);
        count++;
      }

      model.update ();
    } catch (GRBException e) {
      throw new IllegalStateException ("Unable to add variables and objectives to native model", e);
    }
  }

  protected void addConstraintsToModel (final GRBModel model) {
    final GRBLinExpr[] expressions = new GRBLinExpr[constraints.size ()];
    final String[] constrNames = new String[constraints.size ()];
    final char[] sense = new char[constraints.size ()];
    final double[] rhs = new double[constraints.size ()];

    try {
      int count = 0;
      for (final GurobiConstraint constraint: constraints) {
        final GRBLinExpr nativeLhsExpr = ((GurobiLinearExpression) constraint.getLhs ()).getNativeExpression ();
        final GRBLinExpr nativeRhsExpr = ((GurobiLinearExpression) constraint.getRhs ()).getNativeExpression ();
        nativeLhsExpr.multAdd (-1, nativeRhsExpr);

        expressions[count] = nativeLhsExpr;
        constrNames[count] = constraint.getName ();
        rhs[count] = 0.0;

        switch (constraint.getOperator ()) {
          case EQUALS:
            sense[count] = GRB.EQUAL;
            break;
          case GREATER_EQUALS:
            sense[count] = GRB.GREATER_EQUAL;
            break;
          case LESS_EQUALS:
            sense[count] = GRB.LESS_EQUAL;
            break;
          default:
            throw new IllegalStateException ("Constraints with operator " + constraint.getOperator ().name () +
                                             " are not supported");
        }

        count++;
      }

      final GRBConstr[] constrs = model.addConstrs (expressions, sense, rhs, constrNames);

      count = 0;
      for (final GurobiConstraint constr: constraints) {
        constr.setNativeConstraint (constrs[count]);
        count++;
      }

      model.update ();
    } catch (GRBException e) {
      throw new IllegalStateException ("Unable to add constraints to native model", e);
    }
  }

  @Override
  public void updateChanges () {
    try {
      nativeModel.update ();
    } catch (GRBException e) {
      LOG.error ("Unable to update native gurobi model", e);
      throw new ProgramException ("Unable to update native gurobi model", e);
    }
  }

  @Override
  public final int hashCode () {
    return nativeModel.hashCode ();
  }

  @Override
  public final boolean equals (final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof GurobiProgram)) {
      return false;
    }
    final GurobiProgram other = (GurobiProgram) obj;
    return nativeModel.equals (other.nativeModel);
  }

  @Override
  public String toString () {
    return getName ();
  }

}
