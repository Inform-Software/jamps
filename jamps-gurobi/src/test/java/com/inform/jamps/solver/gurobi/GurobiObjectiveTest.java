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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.inform.jamps.modeling.Objective;
import com.inform.jamps.modeling.ObjectiveSense;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class GurobiObjectiveTest {

  @Test
  public void testObjectCreationAndGetters () {
    final GurobiProgram program = new GurobiProgram ();
    final ObjectiveSense sense = ObjectiveSense.MAXIMIZE;
    final String name = "Objective";

    final GurobiObjective obj1 = new GurobiObjective (program);
    final GurobiObjective obj2 = new GurobiObjective (program, sense);
    final GurobiObjective obj3 = new GurobiObjective (program, name, sense);

    assertEquals ("Expected different program for obj1", program, obj1.getProgram ());
    assertEquals ("Expected different program for obj2", program, obj2.getProgram ());
    assertEquals ("Expected different program for obj3", program, obj3.getProgram ());

    assertEquals ("Expected different operator for obj1", GurobiObjective.DEFAULT_SENSE, obj1.getObjectiveSense ());
    assertEquals ("Expected different operator for obj2", sense, obj2.getObjectiveSense ());
    assertEquals ("Expected different operator for obj3", sense, obj3.getObjectiveSense ());

    assertFalse ("Expected auto generated name for obj1", obj1.getName ().isEmpty ());
    assertFalse ("Expected auto generated name for obj2", obj2.getName ().isEmpty ());
    assertEquals ("Expected different name for obj3", name, obj3.getName ());

    assertNotNull ("Expected linear expression for obj1", obj1.getExpression ());
    assertNotNull ("Expected linear expression for obj2", obj2.getExpression ());
    assertNotNull ("Expected linear expression for obj3", obj3.getExpression ());
  }

  @Test
  public void testObjectCreationWithErrors () {
    final GurobiProgram program = new GurobiProgram ();
    final ObjectiveSense sense = ObjectiveSense.MAXIMIZE;
    final String name = "Objective";

    try {
      new GurobiObjective (null, name, sense);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      new GurobiObjective (program, name, null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      new GurobiObjective (program, null, sense);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testSetters () {
    final ObjectiveSense sense = ObjectiveSense.MAXIMIZE;

    final GurobiObjective objective = new GurobiObjective (new GurobiProgram ());
    objective.setObjectiveSense (sense);

    assertEquals ("Expected different objective sense", sense, objective.getObjectiveSense ());

    try {
      objective.setObjectiveSense (null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testEqualsAndHashCode () {
    final GurobiLinearExpression linExp1 = new GurobiLinearExpression (new GurobiConstraint (new GurobiProgram ()));
    final GurobiLinearExpression linExp2 = new GurobiLinearExpression (new GurobiConstraint (new GurobiProgram ()));

    linExp1.addTerm (9.0);
    linExp2.addTerm (10.0);

    EqualsVerifier.forClass (GurobiObjective.class)
                  .allFieldsShouldBeUsedExcept ("program")
                  .withPrefabValues (GurobiLinearExpression.class, linExp1, linExp2)
                  .suppress (Warning.NULL_FIELDS, Warning.NONFINAL_FIELDS)
                  .verify ();
  }

  @Test
  public void testCompareTo () {
    final GurobiProgram program = new GurobiProgram ();
    final String name = "Constraint";

    final GurobiObjective obj1 = new GurobiObjective (program, name, ObjectiveSense.MINIMIZE);
    final GurobiObjective obj2 = new GurobiObjective (program, name, ObjectiveSense.MAXIMIZE);
    assertTrue ("Expected obj1 to be less than obj2", obj1.compareTo (obj2) < 0);
    assertTrue ("Expected obj1 to be less than obj2", obj2.compareTo (obj1) > 0);

    final GurobiObjective obj3 = new GurobiObjective (program, name, ObjectiveSense.MINIMIZE);
    final GurobiObjective obj4 = new GurobiObjective (program, name, ObjectiveSense.MINIMIZE);
    obj3.getExpression ().addTerm (2.0);
    assertTrue ("Expected obj4 to be less than obj3", obj3.compareTo (obj4) > 0);
    assertTrue ("Expected obj4 to be less than obj3", obj4.compareTo (obj3) < 0);

    final GurobiObjective obj5 = new GurobiObjective (program, "Name1", ObjectiveSense.MINIMIZE);
    final GurobiObjective obj6 = new GurobiObjective (program, "Name2", ObjectiveSense.MINIMIZE);
    assertTrue ("Expected obj5 to be less than obj6", obj5.compareTo (obj6) < 0);
    assertTrue ("Expected obj5 to be less than obj6", obj6.compareTo (obj5) > 0);

    assertTrue ("Expected obj5 to be less than any object of other class", obj5.compareTo (mock (Objective.class)) < 0);
    assertTrue ("Expected obj5 to be less than null", obj5.compareTo (null) < 0);
  }
}
