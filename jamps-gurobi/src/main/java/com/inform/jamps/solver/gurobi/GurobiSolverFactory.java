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

import com.inform.jamps.modeling.Program;
import com.inform.jamps.solver.MathProgrammingSolverFactory;

public class GurobiSolverFactory implements MathProgrammingSolverFactory<GurobiSolver, GurobiSolverParameters> {

  private GurobiSolverParameters parameters = new GurobiSolverParameters ();

  @Override
  public GurobiSolver createSolver () {
    return new GurobiSolver (parameters);
  }

  @Override
  public GurobiSolverParameters getParameters () {
    return parameters;
  }

  @Override
  public void setParameters (final GurobiSolverParameters parameters) {
    if (parameters == null) {
      throw new IllegalArgumentException ("SolverParameters is mandatory and may not be null");
    }
    this.parameters = parameters;
  }

  @Override
  public Program createProgram () {
    return new GurobiProgram (parameters);
  }

  @Override
  public Program createProgram (final String name) {
    return new GurobiProgram (parameters, name);
  }

}
