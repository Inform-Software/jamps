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

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inform.jamps.exception.SolverException;
import com.inform.jamps.modeling.Constraint;
import com.inform.jamps.modeling.Objective;
import com.inform.jamps.modeling.Program;
import com.inform.jamps.modeling.Variable;
import com.inform.jamps.solver.SolverParameters;

import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

@RunWith (PowerMockRunner.class)
@PrepareOnlyThisForTest (GurobiProgram.class)
public class GurobiSolverTest {

  @Test
  public void testSolving () throws Exception {
    final GRBModel nativeModel = mock (GRBModel.class);
    final GurobiProgram p = createProgram (nativeModel);

    final GurobiSolver solver = new GurobiSolver ();
    solver.solve (p);
    solver.solve (new GurobiSolverParameters (), p);

    verify (nativeModel, times (2)).optimize ();
  }

  @Test
  public void testSolvingWithGurobiError () throws Exception {
    final GRBModel nativeModel = mock (GRBModel.class);
    doThrow (new GRBException ()).when (nativeModel).optimize ();

    final GurobiProgram p = createProgram (nativeModel);

    final GurobiSolver solver = new GurobiSolver ();

    try {
      solver.solve (p);
      fail ("Expected SolverException");
    } catch (SolverException e) {
    }
  }

  @Test
  public void testSolvingWithArgumentErrors () throws Exception {
    final GRBModel nativeModel = mock (GRBModel.class);
    final Program p = createProgram (nativeModel);
    final SolverParameters parameters = new GurobiSolverParameters ();

    final GurobiSolver solver = new GurobiSolver ();

    try {
      solver.solve (null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      solver.solve (null, p);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      solver.solve (parameters, null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      solver.solve (parameters, mock (Program.class));
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      solver.solve (mock (SolverParameters.class), p);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  protected GurobiProgram createProgram (GRBModel grbModel) throws Exception {
    final GRBEnv grbEnv = mock (GRBEnv.class);
    whenNew (GRBModel.class).withAnyArguments ().thenReturn (grbModel);

    when (grbModel.addVars (any (double[].class),
                            any (double[].class),
                            any (double[].class),
                            any (char[].class),
                            any (String[].class))).thenReturn (new GRBVar[] {mock (GRBVar.class)});

    when (grbModel.addConstrs (any (GRBLinExpr[].class),
                               any (char[].class),
                               any (double[].class),
                               any (String[].class))).thenReturn (new GRBConstr[] {mock (GRBConstr.class)});

    final GurobiProgram p = new GurobiProgram ();
    p.setNativeEnvironment (grbEnv);

    final Variable var = p.addVariable ();

    final Objective obj = p.addObjective ();
    obj.getExpression ().addTerm (1.0, var);

    final Constraint c = p.addConstraint ();
    c.getLhs ().addTerm (1.0, var);
    return p;
  }
}
