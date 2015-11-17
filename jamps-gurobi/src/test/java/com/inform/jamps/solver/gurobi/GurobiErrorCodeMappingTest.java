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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Test;

import gurobi.GRBException;

public class GurobiErrorCodeMappingTest {

  private static final int[] DEFINED_ERROR_CODES  = new int[] {10001, 10002, 10003, 10004, 10005, 10006, 10007, 10008,
                                                               10009, 10010, 10011, 10012, 10013, 10014, 10015, 10016,
                                                               10017, 10018, 10019, 10020, 10021, 10022, 10023, 10024,
                                                               10025, 10026, 20001, 20002, 20003};

  private static final int   UNDEFINED_ERROR_CODE = 50000;

  @Test
  public void testErrorCodeCoverage () {
    for (int errCode: DEFINED_ERROR_CODES) {
      final GRBException exception = mock (GRBException.class);
      when (exception.getErrorCode ()).thenReturn (errCode);
      when (exception.getMessage ()).thenReturn ("");

      final String message = GurobiErrorCodeMapping.getMessage (exception);
      assertNotNull ("Expected error message", message);
      assertFalse ("Expected error message", message.isEmpty ());
    }
  }

  @Test
  public void testUndefinedErrorCode () {
    final String errorMessageContent = "This is an error";

    final GRBException exception = mock (GRBException.class);
    when (exception.getErrorCode ()).thenReturn (UNDEFINED_ERROR_CODE);
    when (exception.getMessage ()).thenReturn (errorMessageContent);

    final String message = GurobiErrorCodeMapping.getMessage (exception);
    assertNotNull ("Expected error message", message);
    assertEquals ("Expected a different error message", errorMessageContent, message);
  }

  @Test
  public void testClassIsWellDefined () throws SecurityException,
                                        NoSuchMethodException,
                                        IllegalArgumentException,
                                        InstantiationException,
                                        IllegalAccessException,
                                        InvocationTargetException {

    assertEquals ("There must be only one constructor",
                  1,
                  GurobiErrorCodeMapping.class.getDeclaredConstructors ().length);
    final Constructor<?> constructor = GurobiErrorCodeMapping.class.getDeclaredConstructor ();
    if (constructor.isAccessible () || !Modifier.isPrivate (constructor.getModifiers ())) {
      fail ("Constructor is not private");
    }
    constructor.setAccessible (true);
    constructor.newInstance ();
    constructor.setAccessible (false);

    final Method[] methods = GurobiErrorCodeMapping.class.getMethods ();
    for (final Method method: methods) {
      if (!Modifier.isStatic (method.getModifiers ()) &&
          method.getDeclaringClass ().equals (GurobiErrorCodeMapping.class)) {
        fail ("There exists a non-static method:" + method);
      }
    }

    final Field[] fields = GurobiErrorCodeMapping.class.getFields ();
    for (Field field: fields) {
      if (!Modifier.isStatic (field.getModifiers ()) &&
          field.getDeclaringClass ().equals (GurobiErrorCodeMapping.class)) {
        fail ("There exists a non-static field:" + field);
      }
      if (!Modifier.isFinal (field.getModifiers ()) &&
          field.getDeclaringClass ().equals (GurobiErrorCodeMapping.class)) {
        fail ("There exists a non-final field:" + field);
      }
    }
  }
}
