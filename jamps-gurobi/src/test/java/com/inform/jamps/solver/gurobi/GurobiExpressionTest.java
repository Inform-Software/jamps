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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inform.jamps.modeling.Expression;
import com.inform.jamps.modeling.ObjectiveSense;
import com.inform.jamps.modeling.Operator;
import com.inform.jamps.modeling.Variable;
import com.inform.jamps.modeling.VariableType;
import com.inform.jamps.solver.gurobi.GurobiConstraint;
import com.inform.jamps.solver.gurobi.GurobiExpression;
import com.inform.jamps.solver.gurobi.GurobiLinearTerm;
import com.inform.jamps.solver.gurobi.GurobiObjective;
import com.inform.jamps.solver.gurobi.GurobiProgram;
import com.inform.jamps.solver.gurobi.GurobiVariable;

@RunWith (PowerMockRunner.class)
public class GurobiExpressionTest {

  @Test
  public void testObjectCreation () {
    final GurobiProgram program = new GurobiProgram ();
    final GurobiConstraint constraint = new GurobiConstraint (program);
    final GurobiObjective objective = new GurobiObjective (program);
    final GurobiVariable variable = new GurobiVariable (program);

    final GurobiExpression linExpr1 = new GurobiExpression (constraint);
    final GurobiExpression linExpr2 = new GurobiExpression (objective);

    assertEquals ("Expected linear expression to contain no variables", 0, linExpr1.getLinearTerms ().size ());
    assertEquals ("Expected linear expression to contain no variables", 0, linExpr2.getLinearTerms ().size ());

    assertEquals ("Expected linear expression to have a constant term of 0.0", 0.0, linExpr1.getConstant (), 0.000001);
    assertEquals ("Expected linear expression to have a constant term of 0.0", 0.0, linExpr2.getConstant (), 0.000001);

    assertEquals ("Expected linear expression to have no coefficient vor variable",
                  0.0,
                  linExpr1.getCoefficient (variable),
                  0.000001);
    assertEquals ("Expected linear expression to have no coefficient vor variable",
                  0.0,
                  linExpr2.getCoefficient (variable),
                  0.000001);

  }

  @Test
  public void testObjectCreationWithErrors () {
    try {
      new GurobiExpression ((GurobiConstraint) null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      new GurobiExpression ((GurobiObjective) null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testAddingConstantTerms () {
    final double CONSTANT_TERM = 10.0;

    final GurobiProgram program = new GurobiProgram ();
    final GurobiObjective obj = new GurobiObjective (program, ObjectiveSense.MAXIMIZE);
    final GurobiConstraint constr = new GurobiConstraint (program, Operator.GREATER_EQUALS);

    final GurobiExpression expr1 = new GurobiExpression (constr);
    final GurobiExpression expr2 = new GurobiExpression (obj);

    expr1.addTerm (CONSTANT_TERM);
    expr2.addTerm (CONSTANT_TERM);

    assertEquals ("Expected different constant term", CONSTANT_TERM, expr1.getConstant (), 0.0001);
    assertEquals ("Expected different constant term", CONSTANT_TERM, expr2.getConstant (), 0.0001);

    expr1.addTerm (CONSTANT_TERM);
    expr2.addTerm (CONSTANT_TERM);

    assertEquals ("Expected different constant term", 2 * CONSTANT_TERM, expr1.getConstant (), 0.0001);
    assertEquals ("Expected different constant term", 2 * CONSTANT_TERM, expr2.getConstant (), 0.0001);

    expr1.addTerm (0.0).addTerm (CONSTANT_TERM);
    expr2.addTerm (0.0).addTerm (CONSTANT_TERM);

    assertEquals ("Expected different constant term", 3 * CONSTANT_TERM, expr1.getConstant (), 0.0001);
    assertEquals ("Expected different constant term", 3 * CONSTANT_TERM, expr2.getConstant (), 0.0001);
  }

  @Test
  public void testAddingVariableTerms () {
    final double COEFFICIENT = 10.0;

    final GurobiProgram program = new GurobiProgram ();
    final GurobiObjective obj = new GurobiObjective (program, ObjectiveSense.MAXIMIZE);
    final GurobiConstraint constr = new GurobiConstraint (program, Operator.GREATER_EQUALS);
    final GurobiVariable var1 = new GurobiVariable (program, VariableType.BINARY);
    final GurobiVariable var2 = new GurobiVariable (program, VariableType.BINARY);
    final GurobiVariable var3 = new GurobiVariable (program, VariableType.BINARY);

    final GurobiExpression expr1 = new GurobiExpression (constr);
    final GurobiExpression expr2 = new GurobiExpression (obj);

    expr1.addTerm (COEFFICIENT, var1);
    expr2.addTerm (COEFFICIENT, var1);

    assertTrue ("Expected different variables", containsTerm (expr1, COEFFICIENT, var1));
    assertTrue ("Expected different variables", containsTerm (expr2, COEFFICIENT, var1));

    expr1.addTerm (COEFFICIENT, var1).addTerm (COEFFICIENT, var2).addTerm (0.0, var3);
    expr2.addTerm (COEFFICIENT, var1).addTerm (COEFFICIENT, var2).addTerm (0.0, var3);

    assertTrue ("Expected different variables", containsTerm (expr1, 2 * COEFFICIENT, var1));
    assertTrue ("Expected different variables", containsTerm (expr1, COEFFICIENT, var2));
    assertTrue ("Expected different variables", containsTerm (expr2, 2 * COEFFICIENT, var1));
    assertTrue ("Expected different variables", containsTerm (expr2, COEFFICIENT, var2));

    expr1.addTerms (expr2);

    assertTrue ("Expected different variables", containsTerm (expr1, 4 * COEFFICIENT, var1));
    assertTrue ("Expected different variables", containsTerm (expr1, 2 * COEFFICIENT, var2));
    assertEquals ("Expected different coefficient", 0.0, expr1.getCoefficient (var3), 0.0001);
  }

  @Test
  public void testAddingVariableTermsWithErrors () {
    final double COEFFICIENT = 10.0;
    final GurobiProgram program = new GurobiProgram ();
    final GurobiProgram otherProgram = new GurobiProgram ();
    final GurobiConstraint constr = new GurobiConstraint (program, Operator.GREATER_EQUALS);
    final GurobiVariable var1 = new GurobiVariable (program, VariableType.BINARY);
    final GurobiVariable var2 = new GurobiVariable (otherProgram, VariableType.BINARY);
    final Variable var3 = mock (Variable.class);
    final GurobiVariable var4 = mock (GurobiVariable.class);
    when (var4.getProgram ()).thenReturn (null);

    final GurobiExpression expr1 = new GurobiExpression (constr);
    expr1.addTerm (COEFFICIENT, var1);

    try {
      expr1.addTerm (COEFFICIENT, var2);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      expr1.addTerm (COEFFICIENT, var3);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      expr1.addTerm (COEFFICIENT, var4);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testRemovingConstantTerms () {
    final double CONSTANT_TERM = 10.0;

    final GurobiProgram program = new GurobiProgram ();
    final GurobiObjective obj = new GurobiObjective (program, ObjectiveSense.MAXIMIZE);
    final GurobiConstraint constr = new GurobiConstraint (program, Operator.GREATER_EQUALS);

    final GurobiExpression expr1 = new GurobiExpression (constr);
    final GurobiExpression expr2 = new GurobiExpression (obj);

    expr1.addTerm (CONSTANT_TERM);
    expr2.addTerm (CONSTANT_TERM);

    assertEquals ("Expected different constant term", CONSTANT_TERM, expr1.getConstant (), 0.0001);
    assertEquals ("Expected different constant term", CONSTANT_TERM, expr2.getConstant (), 0.0001);

    expr1.removeConstant ();
    expr2.removeConstant ();

    assertEquals ("Expected different constant term", 0.0, expr1.getConstant (), 0.0001);
    assertEquals ("Expected different constant term", 0.0, expr2.getConstant (), 0.0001);
  }

  @Test
  public void testRemovingVariableTerms () {
    final double COEFFICIENT = 10.0;

    final GurobiProgram program = new GurobiProgram ();
    final GurobiObjective obj = new GurobiObjective (program, ObjectiveSense.MAXIMIZE);
    final GurobiConstraint constr = new GurobiConstraint (program, Operator.GREATER_EQUALS);
    final GurobiVariable var1 = new GurobiVariable (program, VariableType.BINARY);
    final GurobiVariable var2 = new GurobiVariable (program, VariableType.BINARY);
    final GurobiVariable var3 = new GurobiVariable (program, VariableType.BINARY);

    final GurobiExpression expr1 = new GurobiExpression (constr);
    final GurobiExpression expr2 = new GurobiExpression (obj);

    expr1.addTerm (COEFFICIENT, var1).addTerm (COEFFICIENT, var2);
    expr2.addTerm (COEFFICIENT, var1).addTerm (COEFFICIENT, var2);

    assertTrue ("Expected term in expression", containsTerm (expr1, COEFFICIENT, var1));
    assertTrue ("Expected term in expression", containsTerm (expr1, COEFFICIENT, var2));
    assertTrue ("Expected term in expression", containsTerm (expr2, COEFFICIENT, var1));
    assertTrue ("Expected term in expression", containsTerm (expr2, COEFFICIENT, var2));

    expr1.removeLinearTerm (var1);
    expr2.removeLinearTerm (var1);

    assertFalse ("Expected term not in expression", containsTerm (expr1, COEFFICIENT, var1));
    assertTrue ("Expected term in expression", containsTerm (expr1, COEFFICIENT, var2));
    assertFalse ("Expected term not in expression", containsTerm (expr2, COEFFICIENT, var1));
    assertTrue ("Expected term in expression", containsTerm (expr2, COEFFICIENT, var2));

    expr1.removeLinearTerm (var3);
    expr2.removeLinearTerm (var3);

    assertFalse ("Expected term not in expression", containsTerm (expr1, COEFFICIENT, var1));
    assertTrue ("Expected term in expression", containsTerm (expr1, COEFFICIENT, var2));
    assertFalse ("Expected term not in expression", containsTerm (expr2, COEFFICIENT, var1));
    assertTrue ("Expected term in expression", containsTerm (expr2, COEFFICIENT, var2));

    expr1.removeLinearTerm (var2);
    expr2.removeLinearTerm (var2);

    assertEquals ("Expected different amount of terms", 0, expr1.getLinearTerms ().size ());
    assertEquals ("Expected different amount of terms", 0, expr2.getLinearTerms ().size ());

  }

  @Test
  public void testCreationOfNativeExpression () {
    final double COEFFICIENT = 10.0;
    final GurobiProgram program = new GurobiProgram ();
    final GurobiConstraint constr = new GurobiConstraint (program, Operator.GREATER_EQUALS);
    final GurobiVariable var1 = new GurobiVariable (program, VariableType.BINARY);
    final GurobiVariable var2 = new GurobiVariable (program, VariableType.BINARY);
    final GurobiExpression expr = new GurobiExpression (constr);

    expr.addTerm (COEFFICIENT, var1).addTerm (COEFFICIENT, var2);
    final GRBLinExpr nativeExpression = expr.getNativeExpression ();

    assertNotNull ("Expected native expression", nativeExpression);
  }

  @Test
  @PrepareOnlyThisForTest (GurobiExpression.class)
  public void testCreationOfNativeExpressionWithError () throws Exception {
    final double COEFFICIENT = 10.0;
    final GurobiProgram program = new GurobiProgram ();
    final GurobiConstraint constr = new GurobiConstraint (program, Operator.GREATER_EQUALS);
    final GurobiVariable var = new GurobiVariable (program, VariableType.BINARY);
    final GurobiExpression expr = new GurobiExpression (constr);
    expr.addTerm (COEFFICIENT, var);

    final GRBLinExpr nativeExprMock = mock (GRBLinExpr.class);
    doThrow (new GRBException ()).when (nativeExprMock).addTerms (any (double[].class), any (GRBVar[].class));
    PowerMockito.whenNew (GRBLinExpr.class).withAnyArguments ().thenReturn (nativeExprMock);

    try {
      expr.getNativeExpression ();
      fail ("Expected IllegalStateException");
    } catch (IllegalStateException e) {
    }
  }

  @Test
  public void testQuadraticTermsAreNotSupported () {
    final double COEFFICIENT = 10.0;
    final GurobiProgram program = new GurobiProgram ();
    final GurobiConstraint constr = new GurobiConstraint (program, Operator.GREATER_EQUALS);
    final GurobiVariable var1 = new GurobiVariable (program, VariableType.BINARY);
    final GurobiVariable var2 = new GurobiVariable (program, VariableType.BINARY);

    final GurobiExpression expr = new GurobiExpression (constr);
    expr.addTerm (COEFFICIENT, var1);

    try {
      expr.addTerm (COEFFICIENT, var1, var2);
      fail ("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
    }

    assertTrue ("Expecting no qaudratic terms", expr.getQuadraticTerms ().isEmpty ());
    assertEquals ("Expecting no coefficient", 0.0, expr.getCoefficient (var1, var2), 0.0001);

    try {
      expr.removeQuadraticTerm (var1, var2);
      fail ("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
    }
  }

  @Test
  public void testEqualsAndHashCode () {
    final GurobiProgram program = new GurobiProgram ();
    final GurobiObjective obj1 = new GurobiObjective (program, ObjectiveSense.MAXIMIZE);
    final GurobiObjective obj2 = new GurobiObjective (program, ObjectiveSense.MINIMIZE);
    final GurobiConstraint constr1 = new GurobiConstraint (program, Operator.GREATER_EQUALS);
    final GurobiConstraint constr2 = new GurobiConstraint (program, Operator.LESS_EQUALS);

    EqualsVerifier.forClass (GurobiExpression.class)
                  .allFieldsShouldBeUsedExcept ("objective", "constraint")
                  .suppress (Warning.NULL_FIELDS, Warning.NONFINAL_FIELDS)
                  .withPrefabValues (GurobiObjective.class, obj1, obj2)
                  .withPrefabValues (GurobiConstraint.class, constr1, constr2)
                  .verify ();
  }

  @Test
  public void testCompareTo () {
    final GurobiProgram program = new GurobiProgram ();
    final GurobiObjective obj = new GurobiObjective (program, ObjectiveSense.MAXIMIZE);
    final GurobiConstraint constr = new GurobiConstraint (program, Operator.GREATER_EQUALS);
    final GurobiVariable variable = new GurobiVariable (program, VariableType.BINARY);

    final GurobiExpression expr1 = new GurobiExpression (constr);
    final GurobiExpression expr2 = new GurobiExpression (obj);
    assertEquals ("Expected expressions to be equal", 0, expr1.compareTo (expr2));
    assertEquals ("Expected expressions to be equal", 0, expr2.compareTo (expr1));

    final GurobiExpression expr3 = new GurobiExpression (constr);
    final GurobiExpression expr4 = new GurobiExpression (obj);
    expr3.addTerm (1.0);
    assertTrue ("Expected expr4 to be less than expr3", expr3.compareTo (expr4) > 0);
    assertTrue ("Expected expr4 to be less than expr3", expr4.compareTo (expr3) < 0);

    final GurobiExpression expr5 = new GurobiExpression (constr);
    final GurobiExpression expr6 = new GurobiExpression (obj);
    expr5.addTerm (1.0, variable);
    assertTrue ("Expected expr6 to be less than expr3", expr5.compareTo (expr6) > 0);
    assertTrue ("Expected expr6 to be less than expr3", expr6.compareTo (expr5) < 0);

    final GurobiExpression expr7 = new GurobiExpression (constr);
    final GurobiExpression expr8 = new GurobiExpression (obj);
    expr7.addTerm (10.0);
    expr8.addTerm (5.0);
    assertTrue ("Expected expr8 to be less than expr7", expr7.compareTo (expr8) > 0);
    assertTrue ("Expected expr8 to be less than expr7", expr8.compareTo (expr7) < 0);

    assertTrue ("Expected expr7 to be less than any object of other class",
                expr7.compareTo (mock (Expression.class)) < 0);
    assertTrue ("Expected expr7 to be less than null", expr7.compareTo (null) < 0);
  }

  private boolean containsTerm (GurobiExpression expr,
                                double coeff,
                                GurobiVariable var) {
    return expr.getLinearTerms ().contains (new GurobiLinearTerm (coeff, var));
  }
}
