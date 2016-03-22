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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inform.jamps.exception.SolverException;
import com.inform.jamps.modeling.Constraint;
import com.inform.jamps.modeling.Objective;
import com.inform.jamps.modeling.Program;
import com.inform.jamps.modeling.Variable;
import com.inform.jamps.solver.SolverParameters;

import gurobi.GRB.IntAttr;
import gurobi.GRB.Status;
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
    final Program p = createProgram (nativeModel);

    final GurobiSolver solver = new GurobiSolver ();
    solver.solve (new GurobiSolverParameters (), p);

    verify (nativeModel).optimize ();
  }

  @Test
  public void testSolvingWithArgumentErrors () throws Exception {
    final GRBModel nativeModel = mock (GRBModel.class);
    final Program p = createProgram (nativeModel);
    final SolverParameters parameters = new GurobiSolverParameters ();

    final GurobiSolver solver = new GurobiSolver ();

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

  @Test
  public void testWritingOutputFiles () throws Exception {
    final GRBModel nativeModel = mock (GRBModel.class);
    final Program p = createProgram (nativeModel);
    p.setName ("test");

    final GurobiSolverParameters parameters = new GurobiSolverParameters ();
    parameters.setWriteLPFile (true);
    parameters.setWriteMPSFile (true);
    parameters.setWriteParameterFile (true);

    final GurobiSolver solver = new GurobiSolver ();
    solver.solve (parameters, p);

    verify (nativeModel).write ("test.lp");
    verify (nativeModel).write ("test.mps");
    verify (nativeModel).write ("test.prm");

    parameters.setUseCompressionForFileOuput (true);
    solver.solve (parameters, p);

    verify (nativeModel).write ("test.lp.gz");
    verify (nativeModel).write ("test.mps.gz");
    verify (nativeModel).write ("test.prm.gz");
  }

  @Test
  public void testWritingOutputFilesWithoutNames () throws Exception {
    final GRBModel nativeModel = mock (GRBModel.class);
    final Program p = createProgram (nativeModel);
    p.setName ("test");

    final GurobiSolverParameters parameters = new GurobiSolverParameters ();
    parameters.setUseNamesForModelFileOutput (false);
    parameters.setWriteLPFile (true);
    parameters.setWriteMPSFile (true);

    final GurobiSolver solver = new GurobiSolver ();
    solver.solve (parameters, p);

    verify (nativeModel).write ("test.rlp");
    verify (nativeModel).write ("test.rew");

    parameters.setUseCompressionForFileOuput (true);
    solver.solve (parameters, p);

    verify (nativeModel).write ("test.rlp.gz");
    verify (nativeModel).write ("test.rew.gz");
  }

  @Test
  public void testWritingIISFile () throws Exception {
    final GRBModel nativeModel = mock (GRBModel.class);
    final Program p = createProgram (nativeModel);
    p.setName ("test");

    final GurobiSolverParameters parameters = new GurobiSolverParameters ();
    parameters.setWriteIISFile (true);

    final GurobiSolver solver = new GurobiSolver ();
    solver.solve (parameters, p);

    verify (nativeModel, never ()).write ("test.ilp");

    when (nativeModel.get (IntAttr.Status)).thenReturn (Status.INFEASIBLE);
    solver.solve (parameters, p);

    verify (nativeModel).write ("test.ilp");

    parameters.setUseCompressionForFileOuput (true);
    solver.solve (parameters, p);

    verify (nativeModel).write ("test.ilp.gz");
  }

  @Test
  public void testWritingSolutionFile () throws Exception {
    final GRBModel nativeModel = mock (GRBModel.class);
    final Program p = createProgram (nativeModel);
    p.setName ("test");

    final GurobiSolverParameters parameters = new GurobiSolverParameters ();
    parameters.setWriteSolutionFile (true);

    final GurobiSolver solver = new GurobiSolver ();
    solver.solve (parameters, p);

    verify (nativeModel, never ()).write ("test.sol");

    when (nativeModel.get (IntAttr.Status)).thenReturn (Status.OPTIMAL);
    when (nativeModel.get (IntAttr.SolCount)).thenReturn (1);
    solver.solve (parameters, p);

    verify (nativeModel).write ("test.sol");

    parameters.setUseCompressionForFileOuput (true);
    solver.solve (parameters, p);

    verify (nativeModel).write ("test.sol.gz");
  }

  @Test
  public void testSolvingWithGurobiError () throws Exception {
    final GRBModel nativeModel = mock (GRBModel.class);
    doThrow (new GRBException ()).when (nativeModel).optimize ();

    final Program p = createProgram (nativeModel);
    final GurobiSolver solver = new GurobiSolver ();

    final GurobiExecutionResult result = solver.solve (new GurobiSolverParameters (), p);
    assertNotNull ("Gurobi should try to gather result even in case of an error", result);

    doThrow (new GRBException ()).when (nativeModel).get (any (IntAttr.class));

    try {
      solver.solve (new GurobiSolverParameters (), p);
      fail ("Expected SolverException when eveluating execution result failed");
    } catch (SolverException e) {
    }
  }

  @Test (expected = SolverException.class)
  public void testErrorWritingLPFile () throws Exception {
    final GRBModel nativeModel = mock (GRBModel.class);
    doThrow (new GRBException ()).when (nativeModel).write (anyString ());

    final Program p = createProgram (nativeModel);
    final GurobiSolverParameters parameters = new GurobiSolverParameters ();
    parameters.setWriteLPFile (true);

    final GurobiSolver solver = new GurobiSolver ();
    solver.solve (parameters, p);
  }

  @Test (expected = SolverException.class)
  public void testErrorWritingMPSFile () throws Exception {
    final GRBModel nativeModel = mock (GRBModel.class);
    doThrow (new GRBException ()).when (nativeModel).write (anyString ());

    final Program p = createProgram (nativeModel);
    final GurobiSolverParameters parameters = new GurobiSolverParameters ();
    parameters.setWriteMPSFile (true);

    final GurobiSolver solver = new GurobiSolver ();
    solver.solve (parameters, p);
  }

  @Test (expected = SolverException.class)
  public void testErrorWritingParameterFile () throws Exception {
    final GRBModel nativeModel = mock (GRBModel.class);
    doThrow (new GRBException ()).when (nativeModel).write (anyString ());

    final Program p = createProgram (nativeModel);
    final GurobiSolverParameters parameters = new GurobiSolverParameters ();
    parameters.setWriteParameterFile (true);

    final GurobiSolver solver = new GurobiSolver ();
    solver.solve (parameters, p);
  }

  @Test (expected = SolverException.class)
  public void testErrorWritingIISFile () throws Exception {
    final GRBModel nativeModel = mock (GRBModel.class);
    when (nativeModel.get (IntAttr.Status)).thenReturn (Status.INFEASIBLE);
    doThrow (new GRBException ()).when (nativeModel).write (anyString ());

    final Program p = createProgram (nativeModel);
    final GurobiSolverParameters parameters = new GurobiSolverParameters ();
    parameters.setWriteIISFile (true);

    final GurobiSolver solver = new GurobiSolver ();
    solver.solve (parameters, p);
  }

  @Test (expected = SolverException.class)
  public void testErrorWritingSolutionFile () throws Exception {
    final GRBModel nativeModel = mock (GRBModel.class);
    when (nativeModel.get (IntAttr.Status)).thenReturn (Status.OPTIMAL);
    when (nativeModel.get (IntAttr.SolCount)).thenReturn (1);
    doThrow (new GRBException ()).when (nativeModel).write (anyString ());

    final Program p = createProgram (nativeModel);
    final GurobiSolverParameters parameters = new GurobiSolverParameters ();
    parameters.setWriteSolutionFile (true);

    final GurobiSolver solver = new GurobiSolver ();
    solver.solve (parameters, p);
  }

  protected Program createProgram (GRBModel grbModel) throws Exception {
    final GRBEnv grbEnv = mock (GRBEnv.class);
    when (grbModel.getEnv ()).thenReturn (grbEnv);
    PowerMockito.whenNew (GRBModel.class).withAnyArguments ().thenReturn (grbModel);

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
