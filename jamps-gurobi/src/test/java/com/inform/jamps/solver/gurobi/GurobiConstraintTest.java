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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import gurobi.GRBConstr;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

import com.inform.jamps.modeling.Constraint;
import com.inform.jamps.modeling.Operator;
import com.inform.jamps.solver.gurobi.GurobiConstraint;
import com.inform.jamps.solver.gurobi.GurobiExpression;
import com.inform.jamps.solver.gurobi.GurobiProgram;

public class GurobiConstraintTest {

  @Test
  public void testObjectCreationAndGetters () {
    final GurobiProgram program = new GurobiProgram ();
    final Operator op = Operator.GREATER_EQUALS;
    final String name = "Constraint";

    final GurobiConstraint c1 = new GurobiConstraint (program);
    final GurobiConstraint c2 = new GurobiConstraint (program, op);
    final GurobiConstraint c3 = new GurobiConstraint (program, name, op);

    assertEquals ("Expected different program for c1", program, c1.getProgram ());
    assertEquals ("Expected different program for c2", program, c2.getProgram ());
    assertEquals ("Expected different program for c3", program, c3.getProgram ());

    assertEquals ("Expected different operator for c1", GurobiConstraint.DEFAULT_OPERATOR, c1.getOperator ());
    assertEquals ("Expected different operator for c2", op, c2.getOperator ());
    assertEquals ("Expected different operator for c3", op, c3.getOperator ());

    assertFalse ("Expected auto generated name for c1", c1.getName ().isEmpty ());
    assertFalse ("Expected auto generated name for c2", c2.getName ().isEmpty ());
    assertEquals ("Expected different name for c3", name, c3.getName ());

    assertNotNull ("Expected RHS expression for c1", c1.getRhs ());
    assertNotNull ("Expected RHS expression for c2", c2.getRhs ());
    assertNotNull ("Expected RHS expression for c3", c3.getRhs ());

    assertNotNull ("Expected LHS expression for c1", c1.getLhs ());
    assertNotNull ("Expected LHS expression for c2", c2.getLhs ());
    assertNotNull ("Expected LHS expression for c3", c3.getLhs ());
  }

  @Test
  public void testObjectCreationWithErrors () {
    final GurobiProgram program = new GurobiProgram ();
    final Operator op = Operator.GREATER_EQUALS;
    final String name = "Constraint";

    try {
      new GurobiConstraint (null, name, op);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      new GurobiConstraint (program, name, null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      new GurobiConstraint (program, null, op);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testSetters () {
    final Operator operator = Operator.GREATER_EQUALS;
    final GRBConstr grbConstr = mock (GRBConstr.class);

    final GurobiConstraint constr = new GurobiConstraint (new GurobiProgram ());
    constr.setOperator (operator);
    constr.setNativeConstraint (grbConstr);

    assertEquals ("Expected different operator", operator, constr.getOperator ());
    assertEquals ("Expected different native constraint", grbConstr, constr.getNativeConstraint ());

    try {
      constr.setOperator (null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    try {
      constr.setNativeConstraint (null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testEqualsAndHashCode () {
    final GurobiExpression linExp1 = new GurobiExpression (new GurobiConstraint (new GurobiProgram ()));
    final GurobiExpression linExp2 = new GurobiExpression (new GurobiConstraint (new GurobiProgram ()));

    linExp1.addTerm (9.0);
    linExp2.addTerm (10.0);

    EqualsVerifier.forClass (GurobiConstraint.class)
                  .allFieldsShouldBeUsedExcept ("program", "nativeConstraint")
                  .withPrefabValues (GurobiExpression.class, linExp1, linExp2)
                  .suppress (Warning.NULL_FIELDS, Warning.NONFINAL_FIELDS)
                  .verify ();
  }

  @Test
  public void testCompareTo () {
    final GurobiProgram program = new GurobiProgram ();
    final String name = "Constraint";

    final GurobiConstraint c1 = new GurobiConstraint (program, name, Operator.GREATER_EQUALS);
    final GurobiConstraint c2 = new GurobiConstraint (program, name, Operator.EQUALS);
    assertTrue ("Expected c2 to be less than c1", c1.compareTo (c2) > 0);
    assertTrue ("Expected c2 to be less than c1", c2.compareTo (c1) < 0);

    final GurobiConstraint c3 = new GurobiConstraint (program, name, Operator.EQUALS);
    final GurobiConstraint c4 = new GurobiConstraint (program, name, Operator.EQUALS);
    c3.getLhs ().addTerm (2.0);
    assertTrue ("Expected c4 to be less than c3", c3.compareTo (c4) > 0);
    assertTrue ("Expected c4 to be less than c3", c4.compareTo (c3) < 0);

    final GurobiConstraint c5 = new GurobiConstraint (program, name, Operator.EQUALS);
    final GurobiConstraint c6 = new GurobiConstraint (program, name, Operator.EQUALS);
    c5.getRhs ().addTerm (2.0);
    assertTrue ("Expected c6 to be less than c5", c5.compareTo (c6) > 0);
    assertTrue ("Expected c6 to be less than c5", c6.compareTo (c5) < 0);

    final GurobiConstraint c7 = new GurobiConstraint (program, "Name1", Operator.EQUALS);
    final GurobiConstraint c8 = new GurobiConstraint (program, "Name2", Operator.EQUALS);
    assertTrue ("Expected c7 to be less than c8", c7.compareTo (c8) < 0);
    assertTrue ("Expected c7 to be less than c8", c8.compareTo (c7) > 0);

    assertTrue ("Expected c7 to be less than any object of other class", c7.compareTo (mock (Constraint.class)) < 0);
    assertTrue ("Expected c7 to be less than null", c7.compareTo (null) < 0);
  }
}
