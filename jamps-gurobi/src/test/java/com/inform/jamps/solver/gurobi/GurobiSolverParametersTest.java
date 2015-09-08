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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import gurobi.GRBEnv;
import gurobi.GRBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inform.jamps.solver.gurobi.GurobiSolverParameters;

@RunWith (PowerMockRunner.class)
@PrepareOnlyThisForTest ({GurobiSolverParameters.class, GRBEnv.class})
public class GurobiSolverParametersTest {

  @Test
  public void testCreationOfNativeEnvironment () throws Exception {
    final GRBEnv grbEnv = mock (GRBEnv.class);
    PowerMockito.whenNew (GRBEnv.class).withNoArguments ().thenReturn (grbEnv);

    final GurobiSolverParameters parameters = new GurobiSolverParameters ();

    assertSame ("Expecting same objects", grbEnv, parameters.getNativeEnvironment ());
  }

  @Test
  public void testCreationOfNativeEnvironmentWithError () throws Exception {
    final GurobiSolverParameters parameters = new GurobiSolverParameters ();
    PowerMockito.whenNew (GRBEnv.class).withNoArguments ().thenThrow (new GRBException ());

    try {
      parameters.getNativeEnvironment ();
      fail ("Expected IllegalStateException");
    } catch (IllegalStateException e) {
    }
  }
}
