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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inform.jamps.exception.DuplicateEntryException;
import com.inform.jamps.modeling.Constraint;
import com.inform.jamps.modeling.Objective;
import com.inform.jamps.modeling.ObjectiveSense;
import com.inform.jamps.modeling.Operator;
import com.inform.jamps.modeling.Variable;
import com.inform.jamps.modeling.VariableType;

import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntAttr;
import gurobi.GRB.StringAttr;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

@RunWith (PowerMockRunner.class)
public class GurobiProgramTest {

  @Test
  public void testObjectCreationAndGetters () {
    final String name = "Program";

    final GurobiProgram p1 = new GurobiProgram ();
    final GurobiProgram p2 = new GurobiProgram (name);

    assertFalse ("Expected auto generated name for p1", p1.getName ().isEmpty ());
    assertEquals ("Expected different name for p2", name, p2.getName ());
  }

  @Test
  public void testObjectCreationWithErrors () {
    try {
      new GurobiProgram (null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testSettingName () {
    final String name = "Program";
    final GurobiProgram p = new GurobiProgram ();

    assertNotEquals ("Expecting different name", name, p.getName ());

    p.setName (name);

    assertEquals ("Expecting different name", name, p.getName ());

    try {
      p.setName (null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

    assertEquals ("Expecting different name", name, p.getName ());
  }

  @Test
  public void testAddingVariables () {
    final GurobiProgram p = new GurobiProgram ();
    final String varName = "Variable";
    final VariableType type = VariableType.INTEGER;
    final double lb = -5.0;
    final double ub = 5.0;

    final Variable var1 = p.addVariable ();
    final Variable var2 = p.addVariable (type);
    final Variable var3 = p.addVariable (varName, type);
    final Variable var4 = p.addVariable (type, lb, ub);
    final Variable var5 = p.addVariable (varName, type, lb, ub);

    assertEquals ("Expecting different amount of variables", 5, p.getVariablesCount ());
    assertNotNull ("Expecting variable", var1);
    assertNotNull ("Expecting variable", var2);
    assertNotNull ("Expecting variable", var3);
    assertNotNull ("Expecting variable", var4);
    assertNotNull ("Expecting variable", var5);

    assertEquals ("Expecting different variable type", type, var2.getType ());
    assertEquals ("Expecting different variable type", type, var3.getType ());
    assertEquals ("Expecting different variable type", type, var4.getType ());
    assertEquals ("Expecting different variable type", type, var5.getType ());

    assertEquals ("Expecting different name", varName, var3.getName ());
    assertEquals ("Expecting different name", varName, var5.getName ());

    assertEquals ("Expecting different ower bound", lb, var4.getLowerBound (), 0.00001);
    assertEquals ("Expecting different ower bound", lb, var5.getLowerBound (), 0.00001);
    assertEquals ("Expecting different upper bound", ub, var4.getUpperBound (), 0.00001);
    assertEquals ("Expecting different upper bound", ub, var5.getUpperBound (), 0.00001);

    assertThat ("Expecting different set of variables",
                p.getVariables (),
                Matchers.containsInAnyOrder (var1, var2, var3, var4, var5));
  }

  @Test (expected = DuplicateEntryException.class)
  public void testAddingDuplicateVariables () {
    final GurobiProgram p = new GurobiProgram ();
    final String varName = "Variable";
    final VariableType type = VariableType.INTEGER;
    final double lb = -5.0;
    final double ub = 5.0;

    p.addVariable (varName, type, lb, ub);
    p.addVariable (varName, type, lb, ub);
  }

  @Test
  public void testAddingObjectives () {
    final GurobiProgram p = new GurobiProgram ();
    final String name = "Objective";
    final ObjectiveSense sense = ObjectiveSense.MAXIMIZE;

    final Objective obj1 = p.addObjective ();
    final Objective obj2 = p.addObjective (sense);
    final Objective obj3 = p.addObjective (name, sense);

    assertEquals ("Expecting different amount of objectives", 3, p.getObjectivesCount ());
    assertNotNull ("Expecting objective", obj1);
    assertNotNull ("Expecting objective", obj2);
    assertNotNull ("Expecting objective", obj3);

    assertEquals ("Expecting different objective sense", sense, obj2.getObjectiveSense ());
    assertEquals ("Expecting different objective sense", sense, obj3.getObjectiveSense ());

    assertEquals ("Expecting different name", name, obj3.getName ());

    assertThat ("Expecting different set of objectives",
                p.getObjectives (),
                Matchers.containsInAnyOrder (obj1, obj2, obj3));
  }

  @Test
  public void testAddingConstraints () {
    final GurobiProgram p = new GurobiProgram ();
    final String name = "Constraint";
    final Operator op = Operator.GREATER_EQUALS;

    final Constraint c1 = p.addConstraint ();
    final Constraint c2 = p.addConstraint (op);
    final Constraint c3 = p.addConstraint (name, op);

    assertEquals ("Expecting different amount of constraints", 3, p.getConstraintsCount ());
    assertNotNull ("Expecting constraint", c1);
    assertNotNull ("Expecting constraint", c2);
    assertNotNull ("Expecting constraint", c3);

    assertEquals ("Expecting different operator", op, c2.getOperator ());
    assertEquals ("Expecting different objective sense", op, c3.getOperator ());

    assertEquals ("Expecting different name", name, c3.getName ());

    assertThat ("Expecting different set of constraints",
                p.getConstraints (),
                Matchers.containsInAnyOrder (c1, c2, c3));
  }

  @Test
  public void testSettingNativeEnvironment () {
    final GRBEnv grbEnv = mock (GRBEnv.class);

    final GurobiProgram p = new GurobiProgram ();
    p.setNativeEnvironment (grbEnv);

    assertEquals ("Expected different native environment", grbEnv, p.getNativeEnvironment ());

    try {
      p.setNativeEnvironment (null);
      fail ("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  @PrepareOnlyThisForTest (GurobiProgram.class)
  public void testCreationOfNativeModel () throws Exception {
    final GRBEnv grbEnv = mock (GRBEnv.class);
    final GRBModel grbModel = mock (GRBModel.class);
    PowerMockito.whenNew (GRBModel.class).withAnyArguments ().thenReturn (grbModel);

    when (grbModel.addVars (any (double[].class),
                            any (double[].class),
                            any (double[].class),
                            any (char[].class),
                            any (String[].class))).thenReturn (new GRBVar[] {mock (GRBVar.class), mock (GRBVar.class),
                                                                             mock (GRBVar.class), mock (GRBVar.class),
                                                                             mock (GRBVar.class)});

    when (grbModel.addConstrs (any (GRBLinExpr[].class),
                               any (char[].class),
                               any (double[].class),
                               any (String[].class))).thenReturn (new GRBConstr[] {mock (GRBConstr.class),
                                                                                   mock (GRBConstr.class),
                                                                                   mock (GRBConstr.class)});

    final GurobiProgram p1 = createProgram (ObjectiveSense.MINIMIZE);
    final GurobiProgram p2 = createProgram (ObjectiveSense.MAXIMIZE);
    p1.setNativeEnvironment (grbEnv);
    p2.setNativeEnvironment (grbEnv);

    final GRBModel nativeModel1 = p1.getNativeModel ();
    final GRBModel nativeModel2 = p2.getNativeModel ();

    assertNotNull ("Expecting native model", nativeModel1);
    assertNotNull ("Expecting native model", nativeModel2);

    verify (grbModel).set (GRB.IntAttr.ModelSense, -1);
    verify (grbModel).set (GRB.IntAttr.ModelSense, 1);

    final GRBModel nativeModel1Cached = p1.getNativeModel ();

    assertSame ("Expecting same object", nativeModel1, nativeModel1Cached);
  }

  @Test
  @PrepareOnlyThisForTest (GurobiProgram.class)
  public void testCreationOfNativeModelWithGurobiErrors () throws Exception {
    final GRBEnv grbEnv = mock (GRBEnv.class);
    final GRBModel grbModel = mock (GRBModel.class);
    PowerMockito.whenNew (GRBModel.class).withAnyArguments ().thenReturn (grbModel);

    doThrow (new GRBException ()).when (grbModel).addVars (any (double[].class),
                                                           any (double[].class),
                                                           any (double[].class),
                                                           any (char[].class),
                                                           any (String[].class));

    final GurobiProgram p = createProgram (ObjectiveSense.MINIMIZE);
    p.setNativeEnvironment (grbEnv);

    try {
      p.getNativeModel ();
      fail ("Expected IllegalStateException");
    } catch (IllegalStateException e) {
    }

    doReturn (new GRBVar[] {mock (GRBVar.class), mock (GRBVar.class), mock (GRBVar.class), mock (GRBVar.class),
                            mock (GRBVar.class)}).when (grbModel).addVars (any (double[].class),
                                                                           any (double[].class),
                                                                           any (double[].class),
                                                                           any (char[].class),
                                                                           any (String[].class));

    doThrow (new GRBException ()).when (grbModel).addConstrs (any (GRBLinExpr[].class),
                                                              any (char[].class),
                                                              any (double[].class),
                                                              any (String[].class));

    try {
      p.getNativeModel ();
      fail ("Expected IllegalStateException");
    } catch (IllegalStateException e) {
    }

    doReturn (new GRBConstr[] {mock (GRBConstr.class), mock (GRBConstr.class),
                               mock (GRBConstr.class)}).when (grbModel).addConstrs (any (GRBLinExpr[].class),
                                                                                    any (char[].class),
                                                                                    any (double[].class),
                                                                                    any (String[].class));

    doThrow (new GRBException ()).when (grbModel).set (any (DoubleAttr.class), anyDouble ());
    doThrow (new GRBException ()).when (grbModel).set (any (IntAttr.class), anyInt ());
    doThrow (new GRBException ()).when (grbModel).set (any (StringAttr.class), any (String.class));

    try {
      p.getNativeModel ();
      fail ("Expected IllegalStateException");
    } catch (IllegalStateException e) {
    }
  }

  @Test
  public void testCreationOfNativeModelWithoutNativeEnv () throws Exception {
    final GurobiProgram p = createProgram (ObjectiveSense.MINIMIZE);

    try {
      p.getNativeModel ();
      fail ("Expected IllegalStateException");
    } catch (IllegalStateException e) {
    }
  }

  @Test
  public void testCreationOfNativeModelForEmptyProgram () throws Exception {
    final GRBEnv grbEnv = mock (GRBEnv.class);
    final GurobiProgram emptyProgram = new GurobiProgram ();
    emptyProgram.setNativeEnvironment (grbEnv);

    try {
      emptyProgram.getNativeModel ();
      fail ("Expected IllegalStateException");
    } catch (IllegalStateException e) {
    }
  }

  @Test
  @PrepareOnlyThisForTest ({GurobiProgram.class, VariableType.class})
  public void testCreationOfNativeModelWithUnkownVarType () throws Exception {
    final List<VariableType> enumElements = new ArrayList<VariableType> (Arrays.asList (VariableType.values ()));

    final VariableType UNKNOWN = PowerMockito.mock (VariableType.class);
    when (UNKNOWN.ordinal ()).thenReturn (enumElements.size ());
    when (UNKNOWN.name ()).thenReturn ("UNKNOWN");
    enumElements.add (UNKNOWN);

    PowerMockito.mockStatic (VariableType.class);
    PowerMockito.when (VariableType.values ())
                .thenReturn (enumElements.toArray (new VariableType[enumElements.size ()]));

    final GRBEnv grbEnv = mock (GRBEnv.class);
    final GRBModel grbModel = mock (GRBModel.class);
    PowerMockito.whenNew (GRBModel.class).withAnyArguments ().thenReturn (grbModel);

    final GurobiProgram p = createProgram (ObjectiveSense.MINIMIZE);
    p.setNativeEnvironment (grbEnv);
    p.addVariable (UNKNOWN);

    try {
      p.getNativeModel ();
      fail ("Expected IllegalStateException");
    } catch (IllegalStateException e) {
    }
  }

  @Test
  @PrepareOnlyThisForTest ({GurobiProgram.class, Operator.class})
  public void testCreationOfNativeModelWithUnkownOperator () throws Exception {
    final Operator UNKNOWN = PowerMockito.mock (Operator.class);
    when (UNKNOWN.ordinal ()).thenReturn (3);
    when (UNKNOWN.name ()).thenReturn ("UNKNOWN");

    PowerMockito.mockStatic (Operator.class);
    PowerMockito.when (Operator.values ())
                .thenReturn (new Operator[] {UNKNOWN, Operator.EQUALS, Operator.GREATER_EQUALS, Operator.LESS_EQUALS});

    final GRBEnv grbEnv = mock (GRBEnv.class);
    final GRBModel grbModel = mock (GRBModel.class);
    PowerMockito.whenNew (GRBModel.class).withAnyArguments ().thenReturn (grbModel);

    when (grbModel.addVars (any (double[].class),
                            any (double[].class),
                            any (double[].class),
                            any (char[].class),
                            any (String[].class))).thenReturn (new GRBVar[] {mock (GRBVar.class), mock (GRBVar.class),
                                                                             mock (GRBVar.class), mock (GRBVar.class),
                                                                             mock (GRBVar.class)});

    final GurobiProgram p = createProgram (ObjectiveSense.MINIMIZE);
    p.setNativeEnvironment (grbEnv);
    p.addConstraint (UNKNOWN);

    try {
      p.getNativeModel ();
      fail ("Expected IllegalStateException");
    } catch (IllegalStateException e) {
    }
  }

  @Test
  public void testEqualsAndHashCode () {
    EqualsVerifier.forClass (GurobiProgram.class)
                  .allFieldsShouldBeUsedExcept ("nativeEnvironment", "nativeModel")
                  .suppress (Warning.NULL_FIELDS, Warning.NONFINAL_FIELDS)
                  .verify ();
  }

  @Test
  public void testCompareTo () {
    final GurobiProgram p1 = new GurobiProgram ("Name1");
    final GurobiProgram p2 = new GurobiProgram ("Name2");
    assertTrue ("Expected p1 to be less than p2", p1.compareTo (p2) < 0);
    assertTrue ("Expected p1 to be less than p2", p2.compareTo (p1) > 0);

    assertTrue ("Expected p1 to be less than null", p1.compareTo (null) < 0);
  }

  protected GurobiProgram createProgram (ObjectiveSense sense) {
    final GurobiProgram p = new GurobiProgram ();
    final Variable var1 = p.addVariable (VariableType.CONTINUOUS);
    final Variable var2 = p.addVariable (VariableType.BINARY);
    final Variable var3 = p.addVariable (VariableType.INTEGER);
    final Variable var4 = p.addVariable (VariableType.SEMI_CONTINUOUS);
    final Variable var5 = p.addVariable (VariableType.SEMI_INTEGER);
    var3.setInitialValue (100.0);

    final Objective obj1 = p.addObjective (sense);
    obj1.getExpression ()
        .addTerm (1.0, var1)
        .addTerm (2.0, var2)
        .addTerm (3.0, var3)
        .addTerm (4.0, var4)
        .addTerm (5.0, var5);

    if (sense == ObjectiveSense.MAXIMIZE) {
      final Objective obj2 = p.addObjective (ObjectiveSense.MINIMIZE);
      obj2.getExpression ()
          .addTerm (1.0, var1)
          .addTerm (2.0, var2)
          .addTerm (3.0, var3)
          .addTerm (4.0, var4)
          .addTerm (5.0, var5);
    } else {
      final Objective obj2 = p.addObjective (ObjectiveSense.MAXIMIZE);
      obj2.getExpression ()
          .addTerm (1.0, var1)
          .addTerm (2.0, var2)
          .addTerm (3.0, var3)
          .addTerm (4.0, var4)
          .addTerm (5.0, var5);
    }

    final Constraint c1 = p.addConstraint (Operator.EQUALS);
    c1.getLhs ().addTerm (1.0, var1).addTerm (2.0, var2).addTerm (3.0, var3).addTerm (4.0, var4).addTerm (5.0, var5);
    c1.getRhs ().addTerm (10.0);

    final Constraint c2 = p.addConstraint (Operator.GREATER_EQUALS);
    c2.getLhs ().addTerm (6.0, var1).addTerm (7.0, var2).addTerm (8.0, var3).addTerm (9.0, var4).addTerm (10.0, var5);
    c1.getRhs ().addTerm (20.0);

    final Constraint c3 = p.addConstraint (Operator.LESS_EQUALS);
    c3.getLhs ().addTerm (7.0, var1).addTerm (11.0, var2).addTerm (12.0, var3).addTerm (13.0, var4).addTerm (14.0,
                                                                                                             var5);
    c1.getRhs ().addTerm (30.0);
    return p;
  }
}
