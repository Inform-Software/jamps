package com.inform.jamps.solver.gurobi;

import org.apache.commons.math3.util.Precision;

import com.inform.jamps.modeling.VariableType;

import gurobi.GRB;

public final class GurobiUtils {

  private GurobiUtils () {
    // Do not instantiate
  }

  public static char convertToNativeType (final VariableType type) {
    switch (type) {
      case CONTINUOUS:
        return GRB.CONTINUOUS;
      case BINARY:
        return GRB.BINARY;
      case INTEGER:
        return GRB.INTEGER;
      case SEMI_CONTINUOUS:
        return GRB.SEMICONT;
      case SEMI_INTEGER:
        return GRB.SEMIINT;
      default:
        throw new IllegalArgumentException ("Variables of type " + type.name () + " are not supported");
    }
  }

  public static VariableType convertFromNativeType (final char nativeType) {
    switch (nativeType) {
      case GRB.CONTINUOUS:
        return VariableType.CONTINUOUS;
      case GRB.BINARY:
        return VariableType.BINARY;
      case GRB.INTEGER:
        return VariableType.INTEGER;
      case GRB.SEMICONT:
        return VariableType.SEMI_CONTINUOUS;
      case GRB.SEMIINT:
        return VariableType.SEMI_INTEGER;
      default:
        throw new IllegalArgumentException ("Variables of native type '" + nativeType + "' are not supported");
    }
  }

  public static double convertInfinity (final double gurobiValue) {
    if (Precision.equals (gurobiValue, GRB.INFINITY)) {
      return Double.POSITIVE_INFINITY;
    } else if (Precision.equals (gurobiValue, -GRB.INFINITY)) {
      return Double.NEGATIVE_INFINITY;
    } else {
      return gurobiValue;
    }
  }
}
