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

import java.io.File;

import com.inform.jamps.exception.SolverException;
import com.inform.jamps.modeling.Program;
import com.inform.jamps.solver.MathProgrammingSolver;
import com.inform.jamps.solver.SolverParameters;

import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;

public class GurobiSolver implements MathProgrammingSolver {

  protected GurobiSolver () {
    super ();
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
    writePreExecutionOutputFiles (parameters, program);

    try {
      model.optimize ();
    } catch (GRBException e) {
      try {
        // Try to get an execution result, although we had an exception
        return new GurobiExecutionResult (program);
      } catch (Exception e2) {
        throw new SolverException ("Unable to solve program", e);
      }
    }

    final GurobiExecutionResult executionResult = new GurobiExecutionResult (program);
    writePostExecutionOutputFiles (parameters, program, executionResult);
    return executionResult;
  }

  protected void writePreExecutionOutputFiles (final GurobiSolverParameters parameters,
                                               final GurobiProgram program) {
    final GRBModel model = program.getNativeModel ();

    if (parameters.isWriteLPFile ()) {
      final String extension;
      if (parameters.isUseNamesForModelFileOutput ()) {
        extension = "lp";
      } else {
        extension = "rlp";
      }

      try {
        model.write (getOutputFilePath (parameters, program, extension));
      } catch (GRBException e) {
        throw new SolverException ("Unable to write LP file", e);
      }
    }

    if (parameters.isWriteMPSFile ()) {
      final String extension;
      if (parameters.isUseNamesForModelFileOutput ()) {
        extension = "mps";
      } else {
        extension = "rew";
      }

      try {
        model.write (getOutputFilePath (parameters, program, extension));
      } catch (GRBException e) {
        throw new SolverException ("Unable to write MPS file", e);
      }
    }

    if (parameters.isWriteParameterFile ()) {
      try {
        model.write (getOutputFilePath (parameters, program, "prm"));
      } catch (GRBException e) {
        throw new SolverException ("Unable to write parameter file", e);
      }
    }
  }

  protected void writePostExecutionOutputFiles (final GurobiSolverParameters parameters,
                                                final GurobiProgram program,
                                                final GurobiExecutionResult executionResult) {

    final GRBModel model = program.getNativeModel ();

    if (parameters.isWriteIISFile () && executionResult.isProblemInfeasible ()) {
      try {
        model.computeIIS ();
        model.write (getOutputFilePath (parameters, program, "ilp"));
      } catch (GRBException e) {
        throw new SolverException ("Unable to write IIS to file", e);
      }
    }

    if (parameters.isWriteSolutionFile () && executionResult.hasSolution ()) {
      try {
        model.write (getOutputFilePath (parameters, program, "sol"));
      } catch (GRBException e) {
        throw new SolverException ("Unable to write solution file", e);
      }
    }
  }

  protected String getOutputFilePath (final GurobiSolverParameters parameters,
                                      final GurobiProgram program,
                                      final String extension) {
    final StringBuilder sb = new StringBuilder (255);
    sb.append (program.getName ().replaceAll ("[^A-Za-z0-9_]", "_")).append ('.').append (extension);

    if (parameters.isUseCompressionForFileOuput ()) {
      sb.append (".gz");
    }

    if (parameters.getOutputDirectory () == null) {
      return sb.toString ();
    } else {
      return new File (parameters.getOutputDirectory (), sb.toString ()).getAbsolutePath ();
    }
  }
}
