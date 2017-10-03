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

import org.junit.Test;

import com.inform.jamps.modeling.VariableType;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class GurobiVariableTest {

  @Test
  public void testObjectCreationAndGetters () {
    final GurobiProgram program = createProgram ();
    final VariableType type = VariableType.CONTINUOUS;
    final String name = "Variable";

    final GurobiVariable var1 = new GurobiVariable (program);
    final GurobiVariable var2 = new GurobiVariable (program, type);
    final GurobiVariable var3 = new GurobiVariable (program, name, type);
    final GurobiVariable var4 = new GurobiVariable (program, name, VariableType.BINARY);

    program.updateChanges ();

    assertEquals ("Expected different operator for var1", GurobiVariable.DEFAULT_VARIABLE_TYPE, var1.getType ());
    assertEquals ("Expected different operator for var2", type, var2.getType ());
    assertEquals ("Expected different operator for var3", type, var3.getType ());

    assertFalse ("Expected auto generated name for var1", var1.getName ().isEmpty ());
    assertFalse ("Expected auto generated name for var2", var2.getName ().isEmpty ());
    assertEquals ("Expected different name for var3", name, var3.getName ());

    assertEquals ("Expected different lower bound for var1", Double.NEGATIVE_INFINITY, var1.getLowerBound (), 0.0001);
    assertEquals ("Expected different lower bound for var2", Double.NEGATIVE_INFINITY, var2.getLowerBound (), 0.0001);
    assertEquals ("Expected different lower bound for var3", Double.NEGATIVE_INFINITY, var3.getLowerBound (), 0.0001);
    assertEquals ("Expected different lower bound for var4", 0.0, var4.getLowerBound (), 0.0001);

    assertEquals ("Expected different upper bound for var1", Double.POSITIVE_INFINITY, var1.getUpperBound (), 0.0001);
    assertEquals ("Expected different upper bound for var2", Double.POSITIVE_INFINITY, var2.getUpperBound (), 0.0001);
    assertEquals ("Expected different upper bound for var3", Double.POSITIVE_INFINITY, var3.getUpperBound (), 0.0001);
    assertEquals ("Expected different upper bound for var4", 1.0, var4.getUpperBound (), 0.0001);
  }

  @Test
  public void testObjectCreationWithErrors () {
    final GurobiProgram program = createProgram ();
    final VariableType type = VariableType.CONTINUOUS;
    final String name = "Variable";

    try {
      new GurobiVariable (null, name, type);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      new GurobiVariable (program, null, type);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      new GurobiVariable (program, name, null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testSettingInitialValue () {
    final double initialValue = 10.0;
    final GurobiProgram program = createProgram ();
    final GurobiVariable var = new GurobiVariable (program);
    program.updateChanges ();

    assertFalse ("Expecting no initial value", var.hasInitialValue ());

    try {
      var.getInitialValue ();
      fail ("Expected IllegalStateException");
    } catch (IllegalStateException e) {
    }

    var.setInitialValue (initialValue);
    program.updateChanges ();

    assertTrue ("Expecting initial value", var.hasInitialValue ());
    assertEquals ("Expected initial value to be 10.0", initialValue, var.getInitialValue (), 0.0001);
  }

  @Test
  public void testEqualsAndHashCode () {
    EqualsVerifier.forClass (GurobiVariable.class).suppress (Warning.NULL_FIELDS).verify ();
  }

  private GurobiProgram createProgram () {
    return new GurobiProgram (new GurobiSolverParameters ());
  }

}
