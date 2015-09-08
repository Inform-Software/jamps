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

import static org.junit.Assert.assertNotNull;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import com.inform.jamps.modeling.Program;
import com.inform.jamps.solver.AbstractSolvingIT;
import com.inform.jamps.solver.ExecutionResult;
import com.inform.jamps.solver.MathProgrammingSolver;
import com.inform.jamps.solver.SolverParameters;

public class GurobiSolvingIT extends AbstractSolvingIT {

  private final GurobiSolverFactory factory    = new GurobiSolverFactory ();

  private final SolverParameters    parameters = factory.createParameters ();

  @BeforeClass
  public static void checkForGurobiLib () {
    Assume.assumeTrue (isClassAvailable ("gurobi.GRB"));
  }

  @Test
  public void solveKnapsack () {
    final Program mip = createKnapsackMIP (factory);

    assertNotNull ("Expecting a MIP", mip);

    final MathProgrammingSolver solver = factory.createSolver ();
    final ExecutionResult<Program> result = solver.solve (parameters, mip);
    verifyKnapsackExecutionResult (result);
  }

  @Test
  public void solveLargeKnapsack () {
    final Program mip = createLargeKnapsackMIP (factory);

    assertNotNull ("Expecting a MIP", mip);

    final MathProgrammingSolver solver = factory.createSolver ();
    final ExecutionResult<Program> result = solver.solve (parameters, mip);
    verifyLargeKnapsackExecutionResult (result);
  }

  @Test
  public void solveDiet () {
    final Program mip = createDietMIP (factory);

    assertNotNull ("Expecting a MIP", mip);

    final MathProgrammingSolver solver = factory.createSolver ();
    final ExecutionResult<Program> result = solver.solve (parameters, mip);
    verifyDietExecutionResult (result);
  }

  @Test
  public void solveTransport () {
    final Program mip = createTransportMIP (factory);

    assertNotNull ("Expecting a MIP", mip);

    final MathProgrammingSolver solver = factory.createSolver ();
    final ExecutionResult<Program> result = solver.solve (parameters, mip);
    verifyTransportExecutionResult (result);
  }
}
