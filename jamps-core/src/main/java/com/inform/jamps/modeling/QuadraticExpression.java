package com.inform.jamps.modeling;

import java.util.List;

public interface QuadraticExpression extends Expression {

  List<LinearTerm> getLinearTerms ();

  List<QuadraticTerm> getQuadraticTerms ();

  double getConstant ();

  double getCoefficient (Variable variable);

  double getCoefficient (Variable var1,
                         Variable var2);

  QuadraticExpression addTerm (double coefficient,
                               Variable variable);

  Expression addTerm (double coefficient,
                      Variable var1,
                      Variable var2);

  QuadraticExpression addTerm (double constant);

  QuadraticExpression addTerms (QuadraticExpression expr);

  QuadraticExpression removeTerm (Variable variable);

  QuadraticExpression removeTerm (Variable var1,
                                  Variable var2);

  QuadraticExpression removeConstant ();

}
