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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.inform.jamps.modeling.Objective;
import com.inform.jamps.modeling.ObjectiveSense;
import com.inform.jamps.modeling.Variable;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class GurobiSolutionTest {

  @Test
  public void testObjectCreationAndGetters () {
    final GurobiProgram program = new GurobiProgram ();

    final GurobiSolution sol1 = new GurobiSolution (program);
    final GurobiSolution sol2 = new GurobiSolution (program, true);
    final GurobiSolution sol3 = new GurobiSolution (program, false);

    assertEquals ("Expected different program for sol1", program, sol1.getProgram ());
    assertEquals ("Expected different program for sol2", program, sol2.getProgram ());
    assertEquals ("Expected different program for sol3", program, sol3.getProgram ());

    assertFalse ("Expected different optimality value for sol1", sol1.isOptimal ());
    assertTrue ("Expected different optimality value for sol2", sol2.isOptimal ());
    assertFalse ("Expected different optimality value for sol3", sol3.isOptimal ());

    assertEquals ("Expected different gap for sol1",
                  Double.POSITIVE_INFINITY,
                  sol1.getRelativeOptimalityGap (),
                  0.0001);
    assertEquals ("Expected different gap for sol2",
                  Double.POSITIVE_INFINITY,
                  sol2.getRelativeOptimalityGap (),
                  0.0001);
    assertEquals ("Expected different gap for sol3",
                  Double.POSITIVE_INFINITY,
                  sol3.getRelativeOptimalityGap (),
                  0.0001);
  }

  @Test
  public void testObjectCreationWithErrors () {
    try {
      new GurobiSolution (null, false);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testSettingVariableValues () {
    final GurobiProgram program = new GurobiProgram ();
    final GurobiSolution sol = new GurobiSolution (program);

    final GurobiVariable var1 = new GurobiVariable (program);
    final GurobiVariable var2 = new GurobiVariable (program);
    final GurobiVariable var3 = new GurobiVariable (program);

    assertEquals ("Expecting no variable value", 0.0, sol.getVariableValue (var1), 0.00001);
    assertEquals ("Expecting no variable value", 0, sol.getIntegerValue (var1));
    assertEquals ("Expecting no variable value", false, sol.getBinaryValue (var1));

    sol.setVariableValue (var1, 10.5);
    sol.setVariableValue (var2, 10.4);
    sol.setVariableValue (var3, 0.0);

    assertEquals ("Expecting different variable value", 10.5, sol.getVariableValue (var1), 0.00001);
    assertEquals ("Expecting different variable value", 11, sol.getIntegerValue (var1), 0.00001);
    assertEquals ("Expecting different variable value", true, sol.getBinaryValue (var1));

    assertEquals ("Expecting different variable value", 10.4, sol.getVariableValue (var2), 0.00001);
    assertEquals ("Expecting different variable value", 10, sol.getIntegerValue (var2), 0.00001);
    assertEquals ("Expecting different variable value", true, sol.getBinaryValue (var2));

    assertEquals ("Expecting different variable value", 0.0, sol.getVariableValue (var3), 0.00001);
    assertEquals ("Expecting different variable value", 0, sol.getIntegerValue (var3), 0.00001);
    assertEquals ("Expecting different variable value", false, sol.getBinaryValue (var3));
  }

  @Test
  public void testSettingVariableValuesWithError () {
    final GurobiProgram program = new GurobiProgram ();
    final GurobiSolution sol = new GurobiSolution (program);

    try {
      sol.getVariableValue (null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      sol.setVariableValue (null, 1.0);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testCalculatingObjectivesAndGap () {
    final GurobiProgram program = new GurobiProgram ();
    final Objective obj1 = program.addObjective (ObjectiveSense.MAXIMIZE);
    final Objective obj2 = program.addObjective (ObjectiveSense.MINIMIZE);
    final Variable var1 = program.addVariable ();
    final Variable var2 = program.addVariable ();

    obj1.getExpression ().addTerm (1.0, var1).addTerm (2.0, var2);
    obj2.getExpression ().addTerm (2.0, var1).addTerm (4.0, var2);

    final GurobiSolution sol = new GurobiSolution (program);
    sol.setVariableValue ((GurobiVariable) var1, 10.0);
    sol.setVariableValue ((GurobiVariable) var2, 20.0);

    assertEquals ("Expecting different total objective value", -50.0, sol.getObjectiveValue (), 0.0001);
    assertEquals ("Expecting different total objective value (cached)", -50.0, sol.getObjectiveValue (), 0.0001);

    assertEquals ("Expecting different objective value of obj1", 50.0, sol.getObjectiveValue (obj1), 0.0001);
    assertEquals ("Expecting different objective value  of obj2", 100.0, sol.getObjectiveValue (obj2), 0.0001);

    sol.setBestObjectiveBound (0.0);

    assertEquals ("Expecting different gap value", 1.0, sol.getRelativeOptimalityGap (), 0.0001);

    sol.setBestObjectiveBound (-50.0);

    assertEquals ("Expecting different gap value", 0.0, sol.getRelativeOptimalityGap (), 0.0001);
  }

  @Test
  public void testCalculatingObjectivesWithError () {
    final GurobiProgram program = new GurobiProgram ();
    final GurobiSolution sol = new GurobiSolution (program);

    try {
      sol.getObjectiveValue (null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testEqualsAndHashCode () {
    EqualsVerifier.forClass (GurobiSolution.class)
                  .allFieldsShouldBeUsedExcept ("program", "objectiveValuesCache", "objectiveValue", "gap")
                  .suppress (Warning.NULL_FIELDS, Warning.NONFINAL_FIELDS)
                  .verify ();

    final GurobiProgram program = new GurobiProgram ();
    final GurobiSolution sol1 = new GurobiSolution (program);
    final GurobiSolution sol2 = new GurobiSolution (program);

    assertTrue ("Solutions should be equal", sol1.equals (sol2));
    assertEquals ("HashCodes should be equal", sol1.hashCode (), sol2.hashCode ());

    sol2.setBestObjectiveBound (0.0);

    assertFalse ("Solutions should not be equal", sol1.equals (sol2));
  }

  @Test
  public void testCompareTo () {
    final GurobiProgram program = new GurobiProgram ();
    final Objective obj = program.addObjective (ObjectiveSense.MAXIMIZE);
    final Variable var = program.addVariable ();
    obj.getExpression ().addTerm (1.0, var);

    final GurobiSolution sol1 = new GurobiSolution (program);
    sol1.setVariableValue ((GurobiVariable) var, 10.0);

    final GurobiSolution sol2 = new GurobiSolution (program);
    sol2.setVariableValue ((GurobiVariable) var, 20.0);

    assertTrue ("Expected sol1 to be less than sol2", sol1.compareTo (sol2) < 0);
    assertTrue ("Expected sol1 to be less than sol2", sol2.compareTo (sol1) > 0);

    assertTrue ("Expected sol1 to be less than null", sol1.compareTo (null) < 0);
  }
}
