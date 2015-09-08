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

package com.inform.jamps.solver;

public enum TerminationReason {
  /**
   * No termination reason
   */
  NONE,

  /**
   * Termination reason is unknown
   */
  UNKOWN,

  /**
   * Execution of solver has been manually interrupted by an user
   */
  INTERRUPTED_BY_USER,

  /**
   * Maximum number of iterations in the solver (e.g. simplex iterations) has been reached
   */
  ITERATION_LIMIT_REACHED,

  /**
   * Maximum number of nodes in a branch-and-bound algorithm has been explored
   */
  NODE_LIMIT_REACHED,

  /**
   * Execution time has reached is maximum
   */
  TIME_LIMIT_REACHED,

  /**
   * During execution solver found a maximum amount of valid solutions
   */
  SOLUTION_LIMIT_REACHED,

  /**
   * During execution unrecoverable numerical difficulties occurred
   */
  NUMERICAL_INSTABLE,

  /**
   * Objective value of optimal solution is proven to be worse than the worst objective value the 
   * user is interested in (so called objective cut-off value)
   */
  OBJECTIVE_CUTOFF_REACHED
}
