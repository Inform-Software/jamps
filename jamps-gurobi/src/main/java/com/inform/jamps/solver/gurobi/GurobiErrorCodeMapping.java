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

import gurobi.GRB;
import gurobi.GRBException;

import java.util.HashMap;
import java.util.Map;

public class GurobiErrorCodeMapping {

  private final static Map<Integer, GurobiErrorCode> ERRORCODES = new HashMap<Integer, GurobiErrorCode> ();

  private GurobiErrorCodeMapping () {
    // Do not instantiate
  }

  public static String getMessage (final GRBException e) {
    if (ERRORCODES.isEmpty ()) {
      initialize ();
    }

    final StringBuilder sb = new StringBuilder ();
    sb.append (e.getMessage ());

    final GurobiErrorCode errorCode = ERRORCODES.get (e.getErrorCode ());
    if (errorCode != null) {
      sb.append (" (");
      sb.append (errorCode.getSection ());
      sb.append (", ");
      sb.append (errorCode.getErrorCode ());
      sb.append (", ");
      sb.append (errorCode.getDescription ());
      sb.append (")");
    }

    return sb.toString ();
  }

  private static void initialize () {
    add ("OUT_OF_MEMORY", GRB.ERROR_OUT_OF_MEMORY, "Available memory was exhausted");
    add ("NULL_ARGUMENT", GRB.ERROR_NULL_ARGUMENT, "NULL input value provided for a required argument");
    add ("INVALID_ARGUMENT", GRB.ERROR_INVALID_ARGUMENT, "An invalid value was provided for a routine argument");
    add ("UNKNOWN_ATTRIBUTE", GRB.ERROR_UNKNOWN_ATTRIBUTE, "Tried to query or set an unknown attribute");
    add ("DATA_NOT_AVAILABLE",
         GRB.ERROR_DATA_NOT_AVAILABLE,
         "Attempted to query or set an attribute that could not be accessed at that time");
    add ("INDEX_OUT_OF_RANGE",
         GRB.ERROR_INDEX_OUT_OF_RANGE,
         "Tried to query or set an attribute, but one or more of the provided indices (e.g., constraint index, variable index) was outside the range of valid values");
    add ("UNKNOWN_PARAMETER", GRB.ERROR_UNKNOWN_PARAMETER, "Tried to query or set an unknown parameter");
    add ("VALUE_OUT_OF_RANGE",
         GRB.ERROR_VALUE_OUT_OF_RANGE,
         "Tried to set a parameter to a value that is outside the parameter's valid range");
    add ("NO_LICENSE", GRB.ERROR_NO_LICENSE, "Failed to obtain a valid license");
    add ("SIZE_LIMIT_EXCEEDED",
         GRB.ERROR_SIZE_LIMIT_EXCEEDED,
         "Attempted to solve a model that is larger than the limit for a demo license");
    add ("CALLBACK", GRB.ERROR_CALLBACK, "Problem in callback");
    add ("FILE_READ", GRB.ERROR_FILE_READ, "Failed to read the requested file");
    add ("FILE_WRITE", GRB.ERROR_FILE_WRITE, "Failed to write the requested file");
    add ("NUMERIC", GRB.ERROR_NUMERIC, "Numerical error during requested operation");
    add ("IIS_NOT_INFEASIBLE",
         GRB.ERROR_IIS_NOT_INFEASIBLE,
         "Attempted to perform infeasibility analysis on a feasible model");
    add ("NOT_FOR_MIP", GRB.ERROR_NOT_FOR_MIP, "Requested operation not valid for a MIP model");
    add ("OPTIMIZATION_IN_PROGRESS",
         GRB.ERROR_OPTIMIZATION_IN_PROGRESS,
         "Tried to query or modify a model while optimization was in progress");
    add ("DUPLICATES", GRB.ERROR_DUPLICATES, "Constraint, variable, or SOS contained duplicated indices");
    add ("NODEFILE", GRB.ERROR_NODEFILE, "Error in reading or writing a node file during MIP optimization");
    add ("Q_NOT_PSD", GRB.ERROR_Q_NOT_PSD, "Q matrix in QP model is not positive semi-definite");
    add ("QCP_EQUALITY_CONSTRAINT",
         GRB.ERROR_QCP_EQUALITY_CONSTRAINT,
         "QCP equality constraint specified (only inequalities are supported)");
    add ("NETWORK", GRB.ERROR_NETWORK, "Problem communicating with the Gurobi Compute Server");
    add ("JOB_REJECTED",
         GRB.ERROR_JOB_REJECTED,
         "Gurobi Compute Server responded, but was unable to process the job (typically because the queuing time exceeded the user-specified timeout or because the queue has exceeded its maximum capacity)");
    add ("NOT_SUPPORTED",
         GRB.ERROR_NOT_SUPPORTED,
         "Indicates that a Gurobi feature is not supported under your usage environment (for example, some advanced features are not supported in a Compute Server environment)");
    add ("EXCEED_2B_NONZEROS",
         GRB.ERROR_EXCEED_2B_NONZEROS,
         "Indicates that the user has called a query routine on a model with more than 2 billion non-zero entries, and the result would exceed the maximum size that can be returned by that query routine. The solution is typically to move to the GRBX version of that query routine.");
    add ("INVALID_PIECEWISE_OBJ",
         GRB.ERROR_INVALID_PIECEWISE_OBJ,
         "Piecewise-linear objectives must have certain properties (as described in the documentation for the various setPWLObj methods). This error indicates that one of those properties was violated.");
    add ("NOT_IN_MODEL",
         GRB.ERROR_NOT_IN_MODEL,
         "Tried to use a constraint or variable that is not in the model, either because it was removed or because it has not yet been added");
    add ("FAILED_TO_CREATE_MODEL", GRB.ERROR_FAILED_TO_CREATE_MODEL, "Failed to create the requested model");
    add ("INTERNAL", GRB.ERROR_INTERNAL, "Internal Gurobi error");
  }

  private static void add (final String section,
                           final int errorCode,
                           final String description) {
    ERRORCODES.put (errorCode, new GurobiErrorCode (section, errorCode, description));
  }

  private static class GurobiErrorCode {

    private final String section;

    private final int    errorCode;

    private final String description;

    public GurobiErrorCode (final String section,
                            final int errorCode,
                            final String description) {
      this.section = section;
      this.errorCode = errorCode;
      this.description = description;
    }

    public String getSection () {
      return section;
    }

    public int getErrorCode () {
      return errorCode;
    }

    public String getDescription () {
      return description;
    }

  }
}
