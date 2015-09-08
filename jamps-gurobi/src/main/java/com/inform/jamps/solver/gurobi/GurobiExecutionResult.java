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

import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntAttr;
import gurobi.GRB.IntParam;
import gurobi.GRB.Status;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.inform.jamps.modeling.ObjectiveSense;
import com.inform.jamps.modeling.Program;
import com.inform.jamps.solver.ExecutionResult;
import com.inform.jamps.solver.Solution;
import com.inform.jamps.solver.TerminationReason;

public class GurobiExecutionResult implements ExecutionResult<Program> {

  private final static int           MILLIS_PER_SECOND = 1000;

  private final GurobiProgram        program;

  private final List<GurobiSolution> solutions         = new ArrayList<GurobiSolution> ();

  private boolean                    infeasible        = false;

  private boolean                    unbounded         = false;

  private TerminationReason          terminationReason = TerminationReason.NONE;

  private long                       executionTime     = 0;

  protected GurobiExecutionResult (final GurobiProgram program) {
    if (program == null) {
      throw new IllegalArgumentException ("Parameter program is mandatory and may not be null");
    }

    this.program = program;

    try {
      determineExecutionResult ();
    } catch (GRBException e) {
      final String errorMsg = GurobiErrorCodeMapping.getMessage (e);
      throw new IllegalStateException ("Unable to determine execution result from program: " + errorMsg, e);
    }
  }

  @Override
  public Program getProblem () {
    return program;
  }

  @Override
  public boolean isProblemInfeasible () {
    return infeasible;
  }

  @Override
  public boolean isProblemUnbounded () {
    return unbounded;
  }

  @Override
  public boolean hasExecutionTerminated () {
    return (terminationReason != TerminationReason.NONE);
  }

  @Override
  public TerminationReason getTerminationReason () {
    return terminationReason;
  }

  @Override
  public boolean hasSolution () {
    return !solutions.isEmpty ();
  }

  @Override
  public boolean hasOptimalSolution () {
    return hasSolution () && getBestSolution ().isOptimal ();
  }

  @Override
  public int getSolutionsCount () {
    return solutions.size ();
  }

  @Override
  public Solution getBestSolution () {
    if (!hasSolution ()) {
      throw new IllegalStateException ("There is no best solution available");
    }
    return solutions.get (0);
  }

  @Override
  public Solution getSolution (final int index) {
    if (index >= solutions.size ()) {
      throw new IllegalArgumentException ("There is no solution with index " + index + " available");
    }
    return solutions.get (index);
  }

  @Override
  public List<Solution> getSolutions () {
    return new ArrayList<Solution> (solutions);
  }

  @Override
  public long getExecutionTimeMillis () {
    return executionTime;
  }

  private void determineExecutionResult () throws GRBException {
    final GRBModel nativeModel = program.getNativeModel ();

    final int status = nativeModel.get (IntAttr.Status);

    if (status != Status.LOADED) {
      final double runtime = nativeModel.get (DoubleAttr.Runtime);
      executionTime = Math.round (MILLIS_PER_SECOND * runtime);
    }

    switch (status) {
      case Status.LOADED:
      case Status.INPROGRESS:
        throw new IllegalStateException ("Solver run must be completed before creating execution result");
      case Status.CUTOFF:
        terminationReason = TerminationReason.OBJECTIVE_CUTOFF_REACHED;
        break;
      case Status.INF_OR_UNBD:
        unbounded = true;
        infeasible = true;
        break;
      case Status.INFEASIBLE:
        infeasible = true;
        break;
      case Status.UNBOUNDED:
        unbounded = true;
        break;
      case Status.INTERRUPTED:
        terminationReason = TerminationReason.INTERRUPTED_BY_USER;
        determineSolutions (nativeModel);
        break;
      case Status.TIME_LIMIT:
        terminationReason = TerminationReason.TIME_LIMIT_REACHED;
        determineSolutions (nativeModel);
        break;
      case Status.ITERATION_LIMIT:
        terminationReason = TerminationReason.ITERATION_LIMIT_REACHED;
        determineSolutions (nativeModel);
        break;
      case Status.NODE_LIMIT:
        terminationReason = TerminationReason.NODE_LIMIT_REACHED;
        determineSolutions (nativeModel);
        break;
      case Status.SOLUTION_LIMIT:
        terminationReason = TerminationReason.SOLUTION_LIMIT_REACHED;
        determineSolutions (nativeModel);
        break;
      case Status.NUMERIC:
        terminationReason = TerminationReason.NUMERICAL_INSTABLE;
        determineSolutions (nativeModel);
        break;
      case Status.OPTIMAL:
      case Status.SUBOPTIMAL:
        determineSolutions (nativeModel);
        break;
      default:
        return;
    }
  }

  private void determineSolutions (final GRBModel nativeModel) throws GRBException {
    final int solutionCount = nativeModel.get (IntAttr.SolCount);
    final boolean isOptimal = (nativeModel.get (IntAttr.Status) == Status.OPTIMAL);
    final boolean isMip = (nativeModel.get (IntAttr.IsMIP) == 1);

    final double bestBound = (isMip ? nativeModel.get (DoubleAttr.ObjBound) : nativeModel.get (DoubleAttr.ObjVal));

    final List<GurobiVariable> variables = program.getVariables ();

    for (int i = 0; i < solutionCount; i++) {
      nativeModel.getEnv ().set (IntParam.SolutionNumber, i);

      final GurobiSolution solution = new GurobiSolution (program, i == 0 && isOptimal);
      solution.setBestObjectiveBound (bestBound);

      for (GurobiVariable var: variables) {
        final GRBVar nativeVariable = var.getNativeVariable ();
        final double value = (isMip ? nativeVariable.get (DoubleAttr.Xn) : nativeVariable.get (DoubleAttr.X));

        // Storing value 0.0 is not necessary due to it is the default value
        if (Math.abs (value) == 0.0) {
          continue;
        }

        solution.setVariableValue (var, value);
      }

      solutions.add (solution);
    }

    Collections.sort (solutions);
    if (program.determineProgramObjectiveSense () == ObjectiveSense.MAXIMIZE) {
      Collections.reverse (solutions);
    }
  }
}
