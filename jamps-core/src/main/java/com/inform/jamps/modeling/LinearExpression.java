package com.inform.jamps.modeling;

import java.util.List;

public interface LinearExpression extends Expression {

  List<LinearTerm> getLinearTerms ();

  double getConstant ();

  double getCoefficient (Variable variable);

  LinearExpression addTerm (double coefficient,
                            Variable variable);

  LinearExpression addTerm (double constant);

  LinearExpression addTerms (LinearExpression expr);

  LinearExpression removeTerm (Variable variable);

  LinearExpression removeConstant ();
}
