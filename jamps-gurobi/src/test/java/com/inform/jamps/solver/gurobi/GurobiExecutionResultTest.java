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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntAttr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;

import org.junit.Test;

import com.inform.jamps.modeling.Constraint;
import com.inform.jamps.modeling.Objective;
import com.inform.jamps.modeling.ObjectiveSense;
import com.inform.jamps.modeling.Variable;
import com.inform.jamps.solver.TerminationReason;
import com.inform.jamps.solver.gurobi.GurobiExecutionResult;
import com.inform.jamps.solver.gurobi.GurobiProgram;
import com.inform.jamps.solver.gurobi.GurobiVariable;

public class GurobiExecutionResultTest {

  @Test
  public void testObjectCreation () throws GRBException {
    final GurobiProgram program = createProgram ();
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertSame ("Expecting same object", program, result.getProblem ());

    assertFalse ("Problem is not infeasible", result.isProblemInfeasible ());
    assertFalse ("Problem is not unbounded", result.isProblemUnbounded ());
    assertFalse ("Execution has not terminated", result.hasExecutionTerminated ());
    assertEquals ("Termination reason should be NONE", TerminationReason.NONE, result.getTerminationReason ());
    assertFalse ("There is no optimal solution", result.hasOptimalSolution ());
    assertFalse ("There is not solution at all", result.hasSolution ());
    assertTrue ("There is not solution at all", result.getSolutions ().isEmpty ());
    assertEquals ("Solution count should be 0", 0, result.getSolutionsCount ());
    assertEquals ("Exeuction time should be 0", 0, result.getExecutionTimeMillis ());

    try {
      result.getBestSolution ();
      fail ("Expected IllegalStateException");
    } catch (IllegalStateException e) {
    }

    try {
      result.getSolution (0);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

  }

  @Test
  public void testObjectCreationWithErrors () throws GRBException {
    try {
      new GurobiExecutionResult (null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (any (IntAttr.class))).thenThrow (new GRBException ());

    try {
      new GurobiExecutionResult (program);
    } catch (IllegalStateException e) {
    }
  }

  @Test
  public void testCreationBeforeSolverCompleted () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.LOADED);

    try {
      new GurobiExecutionResult (program);
      fail ("Expected IllegalStateException");
    } catch (IllegalStateException e) {
    }

    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.INPROGRESS);

    try {
      new GurobiExecutionResult (program);
      fail ("Expected IllegalStateException");
    } catch (IllegalStateException e) {
    }
  }

  @Test
  public void testRunWithCutOff () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.CUTOFF);
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertTrue ("Expected termination", result.hasExecutionTerminated ());
    assertEquals ("Expected termination reason",
                  TerminationReason.OBJECTIVE_CUTOFF_REACHED,
                  result.getTerminationReason ());
    assertFalse ("There is not solution at all", result.hasSolution ());
  }

  @Test
  public void testRunWithInfeasibleOrUnboundedModel () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.INF_OR_UNBD);
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertTrue ("Expected model to be infeasible", result.isProblemInfeasible ());
    assertTrue ("Expected model to be unbounded", result.isProblemUnbounded ());
    assertFalse ("There is not solution at all", result.hasSolution ());
  }

  @Test
  public void testRunWithInfeasibleModel () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.INFEASIBLE);
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertTrue ("Expected model to be infeasible", result.isProblemInfeasible ());
    assertFalse ("Expected model to be not unbounded", result.isProblemUnbounded ());
    assertFalse ("There is not solution at all", result.hasSolution ());
  }

  @Test
  public void testRunWithUnboundedModel () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.UNBOUNDED);
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertFalse ("Expected model to be not infeasible", result.isProblemInfeasible ());
    assertTrue ("Expected model to be unbounded", result.isProblemUnbounded ());
    assertFalse ("There is not solution at all", result.hasSolution ());
  }

  @Test
  public void testRunWithInterruption () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.INTERRUPTED);
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertTrue ("Expected termination", result.hasExecutionTerminated ());
    assertEquals ("Expected termination reason", TerminationReason.INTERRUPTED_BY_USER, result.getTerminationReason ());
    assertTrue ("There must be at least one solution", result.hasSolution ());
  }

  @Test
  public void testRunWithTimeLimit () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.TIME_LIMIT);
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertTrue ("Expected termination", result.hasExecutionTerminated ());
    assertEquals ("Expected termination reason", TerminationReason.TIME_LIMIT_REACHED, result.getTerminationReason ());
    assertTrue ("There must be at least one solution", result.hasSolution ());
  }

  @Test
  public void testRunWithIteration () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.ITERATION_LIMIT);
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertTrue ("Expected termination", result.hasExecutionTerminated ());
    assertEquals ("Expected termination reason",
                  TerminationReason.ITERATION_LIMIT_REACHED,
                  result.getTerminationReason ());
    assertTrue ("There must be at least one solution", result.hasSolution ());
  }

  @Test
  public void testRunWithNodeLimit () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.NODE_LIMIT);
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertTrue ("Expected termination", result.hasExecutionTerminated ());
    assertEquals ("Expected termination reason", TerminationReason.NODE_LIMIT_REACHED, result.getTerminationReason ());
    assertTrue ("There must be at least one solution", result.hasSolution ());
  }

  @Test
  public void testRunWithSolutionLimit () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.SOLUTION_LIMIT);
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertTrue ("Expected termination", result.hasExecutionTerminated ());
    assertEquals ("Expected termination reason",
                  TerminationReason.SOLUTION_LIMIT_REACHED,
                  result.getTerminationReason ());
    assertTrue ("There must be at least one solution", result.hasSolution ());
  }

  @Test
  public void testRunWithNumericalIssues () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.NUMERIC);
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertTrue ("Expected termination", result.hasExecutionTerminated ());
    assertEquals ("Expected termination reason", TerminationReason.NUMERICAL_INSTABLE, result.getTerminationReason ());
    assertTrue ("There must be at least one solution", result.hasSolution ());
  }

  @Test
  public void testRunWithSuboptimalResult () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.SUBOPTIMAL);
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertFalse ("Expected no termination reason", result.hasExecutionTerminated ());
    assertTrue ("There must be at least one solution", result.hasSolution ());
    assertFalse ("Expected no optimal solution", result.hasOptimalSolution ());
    assertFalse ("Expected best solution to be not optimal", result.getBestSolution ().isOptimal ());
  }

  @Test
  public void testRunWithOptimalResult () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.OPTIMAL);
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertFalse ("Expected no termination reason", result.hasExecutionTerminated ());
    assertTrue ("There must be at least one solution", result.hasSolution ());
    assertTrue ("Expected optimal solution", result.hasOptimalSolution ());
    assertTrue ("Expected best solution to be optimal", result.getBestSolution ().isOptimal ());
  }

  @Test
  public void testRunOfMIP () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.OPTIMAL);
    when (program.getNativeModel ().get (IntAttr.IsMIP)).thenReturn (1);
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertFalse ("Expected no termination reason", result.hasExecutionTerminated ());
    assertTrue ("There must be at least one solution", result.hasSolution ());
    assertTrue ("Expected best solution to be optimal", result.getBestSolution ().isOptimal ());
  }

  @Test
  public void testRunOfMIPWithMultipleSolutions () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.OPTIMAL);
    when (program.getNativeModel ().get (IntAttr.IsMIP)).thenReturn (1);
    when (program.getNativeModel ().get (IntAttr.SolCount)).thenReturn (2);
    final GurobiExecutionResult result = new GurobiExecutionResult (program);

    assertFalse ("Expected no termination reason", result.hasExecutionTerminated ());
    assertTrue ("There must be at least one solution", result.hasSolution ());
    assertTrue ("Expected best solution to be optimal", result.getBestSolution ().isOptimal ());
    assertEquals ("There should be two solutions", 2, result.getSolutions ().size ());

    assertNotNull ("Expected solution with index 0", result.getSolution (0));
    assertNotNull ("Expected solution with index 1", result.getSolution (1));

    try {
      assertNotNull ("Expected no solution with index 2", result.getSolution (2));
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testRunWithOppositeObjectiveSense () throws GRBException {
    final GurobiProgram program = createProgram ();
    when (program.getNativeModel ().get (IntAttr.Status)).thenReturn (GRB.Status.OPTIMAL);
    when (program.getNativeModel ().get (IntAttr.SolCount)).thenReturn (2);

    final GurobiExecutionResult result1 = new GurobiExecutionResult (program);

    program.getObjectives ().get (0).setObjectiveSense (ObjectiveSense.MAXIMIZE);

    final GRBVar nativeVar = mock (GRBVar.class);
    when (nativeVar.get (DoubleAttr.Xn)).thenReturn (0.0, 10.0);
    when (nativeVar.get (DoubleAttr.X)).thenReturn (-0.0, 5.0);
    program.getVariables ().get (0).setNativeVariable (nativeVar);

    final GurobiExecutionResult result2 = new GurobiExecutionResult (program);

    assertEquals ("Expected reverse order of solutions", result1.getSolution (0), result2.getSolution (1));
    assertEquals ("Expected reverse order of solutions", result1.getSolution (1), result2.getSolution (0));
  }

  protected GurobiProgram createProgram () throws GRBException {
    final GRBEnv grbEnv = mock (GRBEnv.class);
    GRBModel grbModel = mock (GRBModel.class);
    when (grbModel.getEnv ()).thenReturn (grbEnv);
    when (grbModel.get (IntAttr.SolCount)).thenReturn (1);
    when (grbModel.get (IntAttr.IsMIP)).thenReturn (0);
    when (grbModel.get (DoubleAttr.ObjBound)).thenReturn (100.0);
    when (grbModel.get (DoubleAttr.ObjVal)).thenReturn (50.0);

    final GurobiProgram p = spy (new GurobiProgram ());
    p.setNativeEnvironment (grbEnv);
    doReturn (grbModel).when (p).getNativeModel ();

    final GRBVar nativeVar = mock (GRBVar.class);
    when (nativeVar.get (DoubleAttr.Xn)).thenReturn (0.0, 10.0);
    when (nativeVar.get (DoubleAttr.X)).thenReturn (-0.0, 5.0);

    final Variable var1 = p.addVariable ();
    ((GurobiVariable) var1).setNativeVariable (nativeVar);

    final Variable var2 = p.addVariable ();
    ((GurobiVariable) var2).setNativeVariable (nativeVar);

    final Objective obj = p.addObjective ();
    obj.getExpression ().addTerm (1.0, var1).addTerm (1.0, var2);

    final Constraint c = p.addConstraint ();
    c.getLhs ().addTerm (1.0, var1).addTerm (1.0, var2);
    return p;
  }
}
