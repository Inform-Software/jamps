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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.inform.jamps.modeling.VariableType;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class GurobiLinearTermTest {

  @Test
  public void testObjectCreationAndGetters () {
    final double COEFFICIENT = 1.0;
    final GurobiVariable variable = new GurobiVariable (new GurobiProgram ());

    final GurobiLinearTerm linearTerm = new GurobiLinearTerm (COEFFICIENT, variable);

    assertEquals ("Expected different variable", variable, linearTerm.getVariable ());
    assertEquals ("Expected different coefficient", COEFFICIENT, linearTerm.getCoefficient (), 0.0001);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testObjectCreationWithErrors () {
    new GurobiLinearTerm (0.0, null);
  }

  @Test
  public void testAddingCoefficient () {
    final double COEFFICIENT = 1.0;
    final GurobiVariable variable = new GurobiVariable (new GurobiProgram ());

    final GurobiLinearTerm linearTerm = new GurobiLinearTerm (COEFFICIENT, variable);

    assertEquals ("Expected different coefficient", COEFFICIENT, linearTerm.getCoefficient (), 0.0001);

    linearTerm.addCoefficient (COEFFICIENT);

    assertEquals ("Expected different coefficient", 2 * COEFFICIENT, linearTerm.getCoefficient (), 0.0001);

    linearTerm.addCoefficient (-1 * COEFFICIENT);

    assertEquals ("Expected different coefficient", COEFFICIENT, linearTerm.getCoefficient (), 0.0001);
  }

  @Test
  public void testEqualsAndHashCode () {
    EqualsVerifier.forClass (GurobiLinearTerm.class).suppress (Warning.NULL_FIELDS, Warning.NONFINAL_FIELDS).verify ();
  }

  @Test
  public void testCompareTo () {
    final double COEFFICIENT = 1.0;
    final GurobiProgram program = new GurobiProgram ();
    final String name = "Variable";

    final GurobiVariable var1 = new GurobiVariable (program, name, VariableType.CONTINUOUS);
    final GurobiVariable var2 = new GurobiVariable (program, name, VariableType.BINARY);

    final GurobiLinearTerm term1 = new GurobiLinearTerm (2 * COEFFICIENT, var1);
    final GurobiLinearTerm term2 = new GurobiLinearTerm (3 * COEFFICIENT, var1);
    assertTrue ("Expected term1 to be less than term2", term1.compareTo (term2) < 0);
    assertTrue ("Expected term1 to be less than term2", term2.compareTo (term1) > 0);

    final GurobiLinearTerm term3 = new GurobiLinearTerm (COEFFICIENT, var1);
    final GurobiLinearTerm term4 = new GurobiLinearTerm (COEFFICIENT, var2);
    assertTrue ("Expected term3 to be less than term4", term3.compareTo (term4) < 0);
    assertTrue ("Expected term3 to be less than term4", term4.compareTo (term3) > 0);

    assertTrue ("Expected term3 to be less than null", term3.compareTo (null) < 0);
  }
}
