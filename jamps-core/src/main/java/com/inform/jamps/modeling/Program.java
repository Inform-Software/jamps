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

package com.inform.jamps.modeling;

public interface Program extends Comparable<Program> {

  String getName ();

  void setName (String name);

  Variable addVariable (String name,
                        VariableType variableType,
                        double lowerBound,
                        double upperBound);

  Variable addVariable (VariableType variableType,
                        double lowerBound,
                        double upperBound);

  Variable addVariable (String name,
                        VariableType variableType);

  Variable addVariable (VariableType variableType);

  Variable addVariable ();

  Objective addObjective (String name,
                          ObjectiveSense sense);

  Objective addObjective (ObjectiveSense sense);

  Objective addObjective ();

  Constraint addConstraint (String name,
                            Operator operator);

  Constraint addConstraint (Operator operator);

  Constraint addConstraint ();

}
