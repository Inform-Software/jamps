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

import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;

import com.inform.jamps.exception.SolverException;
import com.inform.jamps.modeling.Program;
import com.inform.jamps.solver.MathProgrammingSolver;
import com.inform.jamps.solver.SolverParameters;

public class GurobiSolver implements MathProgrammingSolver {

  protected GurobiSolver () {
    super ();
  }

  @Override
  public GurobiExecutionResult solve (final Program program) {
    return solve (new GurobiSolverParameters (), program);
  }

  @Override
  public GurobiExecutionResult solve (final SolverParameters parameters,
                                      final Program program) {
    if (parameters == null) {
      throw new IllegalArgumentException ("Parameter SolverParameters is mandatory and may not be null");
    }
    if (program == null) {
      throw new IllegalArgumentException ("Parameter program is mandatory and may not be null");
    }
    if (!(parameters instanceof GurobiSolverParameters)) {
      throw new IllegalArgumentException ("SolverParameters is not of type GurobiSolverParameters");
    }
    if (!(program instanceof GurobiProgram)) {
      throw new IllegalArgumentException ("Program is not of type GurobiProgram");
    }

    return solve ((GurobiSolverParameters) parameters, (GurobiProgram) program);
  }

  protected GurobiExecutionResult solve (final GurobiSolverParameters parameters,
                                         final GurobiProgram program) {
    final GRBEnv env = parameters.getNativeEnvironment ();
    program.setNativeEnvironment (env);

    final GRBModel model = program.getNativeModel ();
    try {
      model.optimize ();
    } catch (GRBException e) {
      throw new SolverException ("Unable to solve program", e);
    }

    return new GurobiExecutionResult (program);
  }
}
