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
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.inform.jamps.modeling.Variable;
import com.inform.jamps.modeling.VariableType;

import gurobi.GRBVar;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class GurobiVariableTest {

  @Test
  public void testObjectCreationAndGetters () {
    final GurobiProgram program = new GurobiProgram ();
    final VariableType type = VariableType.CONTINUOUS;
    final String name = "Variable";

    final GurobiVariable var1 = new GurobiVariable (program);
    final GurobiVariable var2 = new GurobiVariable (program, type);
    final GurobiVariable var3 = new GurobiVariable (program, name, type);
    final GurobiVariable var4 = new GurobiVariable (program, name, VariableType.BINARY);

    assertEquals ("Expected different program for var1", program, var1.getProgram ());
    assertEquals ("Expected different program for var2", program, var2.getProgram ());
    assertEquals ("Expected different program for var3", program, var3.getProgram ());

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
    final GurobiProgram program = new GurobiProgram ();
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
  public void testSettingNativeVar () {
    final GRBVar grbVar = mock (GRBVar.class);

    final GurobiVariable var = new GurobiVariable (new GurobiProgram ());
    var.setNativeVariable (grbVar);

    assertEquals ("Expected different native variable", grbVar, var.getNativeVariable ());

    try {
      var.setNativeVariable (null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testSettingInitialValue () {
    final double initialValue = 10.0;
    final GurobiVariable var = new GurobiVariable (new GurobiProgram ());

    assertFalse ("Expecting no initial value", var.hasInitialValue ());

    try {
      var.getInitialValue ();
      fail ("Expected IllegalStateException");
    } catch (IllegalStateException e) {
    }

    var.setInitialValue (initialValue);

    assertTrue ("Expecting initial value", var.hasInitialValue ());
    assertEquals ("Expected initial value to be 10.0", initialValue, var.getInitialValue (), 0.0001);
  }

  @Test
  public void testEqualsAndHashCode () {
    EqualsVerifier.forClass (GurobiVariable.class)
                  .allFieldsShouldBeUsedExcept ("program", "initialValue", "nativeVar")
                  .suppress (Warning.NULL_FIELDS, Warning.NONFINAL_FIELDS)
                  .verify ();
  }

  @Test
  public void testCompareTo () {
    final GurobiProgram program = new GurobiProgram ();
    final String name = "Variable";

    final GurobiVariable var1 = new GurobiVariable (program, name, VariableType.BINARY);
    final GurobiVariable var2 = new GurobiVariable (program, name, VariableType.CONTINUOUS);
    assertTrue ("Expected var2 to be less than var2", var1.compareTo (var2) > 0);
    assertTrue ("Expected var2 to be less than var2", var2.compareTo (var1) < 0);

    final GurobiVariable var3 = new GurobiVariable (program, name, VariableType.CONTINUOUS);
    final GurobiVariable var4 = new GurobiVariable (program, name, VariableType.CONTINUOUS);
    var3.setLowerBound (0.0);
    assertTrue ("Expected var4 to be less than var3", var3.compareTo (var4) > 0);
    assertTrue ("Expected var4 to be less than var3", var4.compareTo (var3) < 0);

    final GurobiVariable var5 = new GurobiVariable (program, name, VariableType.CONTINUOUS);
    final GurobiVariable var6 = new GurobiVariable (program, name, VariableType.CONTINUOUS);
    var5.setUpperBound (0.0);
    assertTrue ("Expected var5 to be less than var6", var5.compareTo (var6) < 0);
    assertTrue ("Expected var5 to be less than var6", var6.compareTo (var5) > 0);

    final GurobiVariable var7 = new GurobiVariable (program, "Name1", VariableType.CONTINUOUS);
    final GurobiVariable var8 = new GurobiVariable (program, "Name2", VariableType.CONTINUOUS);
    assertTrue ("Expected var7 to be less than var8", var7.compareTo (var8) < 0);
    assertTrue ("Expected var7 to be less than var8", var8.compareTo (var7) > 0);

    assertTrue ("Expected var7 to be less than any object of other class", var7.compareTo (mock (Variable.class)) < 0);
    assertTrue ("Expected var7 to be less than null", var7.compareTo (null) < 0);
  }
}
