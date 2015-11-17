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

package com.inform.jamps.solver;

import static org.hamcrest.collection.IsIn.isOneOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Before;

import com.inform.jamps.modeling.Constraint;
import com.inform.jamps.modeling.Objective;
import com.inform.jamps.modeling.ObjectiveSense;
import com.inform.jamps.modeling.Operator;
import com.inform.jamps.modeling.Program;
import com.inform.jamps.modeling.Variable;
import com.inform.jamps.modeling.VariableType;

public class AbstractSolvingIT {

  private final List<Variable>  variables  = new ArrayList<Variable> ();

  private final List<Objective> objectives = new ArrayList<Objective> ();

  @Before
  public void cleanup () {
    variables.clear ();
    objectives.clear ();
  }

  protected Program createKnapsackMIP (final MathProgrammingSolverFactory<? extends MathProgrammingSolver, ? extends SolverParameters> solverFactory) {
    final int[] itemSizes = new int[] {10, 10, 15, 20, 20, 24, 24, 50};
    final int[] itemProfits = new int[] {10, 10, 15, 20, 20, 24, 24, 50};
    final int itemCount = itemSizes.length;
    final int knapsackSize = 100;

    final Program mip = solverFactory.createProgram ("Knapsack");
    final Objective objective = mip.addObjective ("ItemProfit", ObjectiveSense.MAXIMIZE);
    final Constraint constraint = mip.addConstraint ("KnapsackCapacity", Operator.LESS_EQUALS);
    constraint.getRhs ().addTerm (knapsackSize);

    Variable[] variables = new Variable[itemCount];
    for (int i = 0; i < itemCount; i++) {
      variables[i] = mip.addVariable ("item" + i, VariableType.BINARY);

      objective.getExpression ().addTerm (itemProfits[i], variables[i]);

      constraint.getLhs ().addTerm (itemSizes[i], variables[i]);
    }

    this.variables.addAll (Arrays.asList (variables));
    this.objectives.add (objective);

    return mip;
  }

  protected void verifyKnapsackExecutionResult (final ExecutionResult<Program> result) {
    final Solution bestSolution = result.getBestSolution ();

    for (Variable var: variables) {
      if (bestSolution.getBinaryValue (var)) {
        assertThat ("Wrong item selected", var.getName (), isOneOf ("item0", "item3", "item4", "item7"));
      } else {
        assertThat ("Missed to select item", var.getName (), isOneOf ("item1", "item2", "item5", "item6"));
      }
    }

    assertEquals ("Expecting different objective value", 100.0, bestSolution.getObjectiveValue (), 0.0001);
  }

  protected Program createLargeKnapsackMIP (final MathProgrammingSolverFactory<? extends MathProgrammingSolver, ? extends SolverParameters> solverFactory) {
    final int itemCount = 1000;
    final int knapsackSize = 1000;
    final int[] itemSizes = new int[itemCount];
    final int[] itemProfits = new int[itemCount];

    final Random rand = new Random (0);
    for (int i = 0; i < itemCount; i++) {
      itemSizes[i] = rand.nextInt (30) + 20;
      itemProfits[i] = rand.nextInt (10) + 5;
    }

    final Program mip = solverFactory.createProgram ("Knapsack");
    final Objective objective = mip.addObjective ("ItemProfit", ObjectiveSense.MAXIMIZE);
    final Constraint constraint = mip.addConstraint ("KnapsackCapacity", Operator.LESS_EQUALS);
    constraint.getRhs ().addTerm (knapsackSize);

    Variable[] variables = new Variable[itemCount];
    for (int i = 0; i < itemCount; i++) {
      variables[i] = mip.addVariable ("item" + i, VariableType.BINARY);

      objective.getExpression ().addTerm (itemProfits[i], variables[i]);

      constraint.getLhs ().addTerm (itemSizes[i], variables[i]);
    }

    this.variables.addAll (Arrays.asList (variables));
    this.objectives.add (objective);

    return mip;
  }

  protected void verifyLargeKnapsackExecutionResult (final ExecutionResult<Program> result) {
    final Solution bestSolution = result.getBestSolution ();
    assertEquals ("Expecting different objective value", 591.0, bestSolution.getObjectiveValue (), 0.0001);
    assertEquals ("Expecting different objective gap", 0.0, bestSolution.getRelativeOptimalityGap (), 0.0001);
  }

  protected Program createDietMIP (final MathProgrammingSolverFactory<? extends MathProgrammingSolver, ? extends SolverParameters> solverFactory) {
    final String[] food = new String[] {"Bread", "Milk", "Cheese", "Potato", "Fish", "Yogurt"};
    final double[] proteins = new double[] {4.0, 8.0, 7.0, 1.3, 8.0, 9.2};
    final double[] fat = new double[] {1.0, 5.0, 9.0, 0.1, 7.0, 1.0};
    final double[] carbohydrates = new double[] {15.0, 11.7, 0.4, 22.6, 0.0, 17.0};
    final double[] calories = new double[] {90, 120, 106, 97, 130, 180};
    final double[] costs = new double[] {2.0, 3.5, 8.0, 1.5, 11.0, 1.0};
    final int foodCount = food.length;

    final double minCalories = 300;
    final double maxProteins = 10;
    final double minCarbohydrates = 10;
    final double minFat = 8;
    final double minFish = 0.5;
    final double maxMilk = 1.0;

    final Program mip = solverFactory.createProgram ("Diet");
    final Objective objective = mip.addObjective ("FoodCosts", ObjectiveSense.MINIMIZE);

    final Constraint constrMinCalories = mip.addConstraint ("MinCalories", Operator.GREATER_EQUALS);
    constrMinCalories.getRhs ().addTerm (minCalories);

    final Constraint constrMinCarbohydrates = mip.addConstraint ("MinCarbohydrates", Operator.GREATER_EQUALS);
    constrMinCarbohydrates.getRhs ().addTerm (minCarbohydrates);

    final Constraint constrMinFat = mip.addConstraint ("MinFat", Operator.GREATER_EQUALS);
    constrMinFat.getRhs ().addTerm (minFat);

    final Constraint constrMaxProteins = mip.addConstraint ("MaxProteins", Operator.LESS_EQUALS);
    constrMaxProteins.getRhs ().addTerm (maxProteins);

    Variable[] variables = new Variable[foodCount];
    for (int i = 0; i < foodCount; i++) {
      double lowerBound = 0.0;
      double upperBound = Double.POSITIVE_INFINITY;

      if ("Fish".equalsIgnoreCase (food[i])) {
        lowerBound = minFish;
      }

      if ("Milk".equalsIgnoreCase (food[i])) {
        upperBound = maxMilk;
      }

      variables[i] = mip.addVariable (food[i], VariableType.CONTINUOUS, lowerBound, upperBound);

      objective.getExpression ().addTerm (costs[i], variables[i]);

      constrMinCalories.getLhs ().addTerm (calories[i], variables[i]);
      constrMinCarbohydrates.getLhs ().addTerm (carbohydrates[i], variables[i]);
      constrMinFat.getLhs ().addTerm (fat[i], variables[i]);
      constrMaxProteins.getLhs ().addTerm (proteins[i], variables[i]);
    }

    this.variables.addAll (Arrays.asList (variables));
    this.objectives.add (objective);

    return mip;
  }

  protected void verifyDietExecutionResult (final ExecutionResult<Program> result) {
    final Solution bestSolution = result.getBestSolution ();
    final double bread = bestSolution.getVariableValue (variables.get (0));
    final double milk = bestSolution.getVariableValue (variables.get (1));
    final double cheese = bestSolution.getVariableValue (variables.get (2));
    final double potato = bestSolution.getVariableValue (variables.get (3));
    final double fish = bestSolution.getVariableValue (variables.get (4));
    final double yogurt = bestSolution.getVariableValue (variables.get (5));

    assertEquals ("Expecting different value of Bread", 0.0, bread, 0.0001);
    assertEquals ("Expecting different value of Milk", 0.0535991, milk, 0.0001);
    assertEquals ("Expecting different value of Cheese", 0.449499, cheese, 0.0001);
    assertEquals ("Expecting different value of Potato", 1.865168, potato, 0.0001);
    assertEquals ("Expecting different value of Fish", 0.5, fish, 0.0001);
    assertEquals ("Expecting different value of Yogurt", 0.0, yogurt, 0.0001);

    assertEquals ("Expecting different objective value", 12.081337881, bestSolution.getObjectiveValue (), 0.0001);
    assertEquals ("Expecting different objective gap", 0.0, bestSolution.getRelativeOptimalityGap (), 0.0001);
  }

  protected Program createTransportMIP (final MathProgrammingSolverFactory<? extends MathProgrammingSolver, ? extends SolverParameters> solverFactory) {
    final String[] warehouses = new String[] {"A", "B"};
    final int[] supply = new int[] {5, 10};
    final int warehousesCount = warehouses.length;

    final String[] shops = new String[] {"S1", "S2", "S3"};
    final int[] demand = new int[] {8, 5, 2};
    final int shopsCount = shops.length;

    final int[][] transportCosts = new int[][] {{1, 2, 4}, {3, 2, 1}};

    final Program mip = solverFactory.createProgram ("TransportingGoods");
    final Objective objective = mip.addObjective ("TransportCosts", ObjectiveSense.MINIMIZE);

    Variable[][] variables = new Variable[warehousesCount][shopsCount];
    for (int i = 0; i < warehousesCount; i++) {
      for (int j = 0; j < shopsCount; j++) {
        variables[i][j] = mip.addVariable ("x_" + warehouses[i] + "_" + shops[j],
                                           VariableType.INTEGER,
                                           0.0,
                                           Double.POSITIVE_INFINITY);

        objective.getExpression ().addTerm (transportCosts[i][j], variables[i][j]);
      }
    }

    for (int i = 0; i < warehousesCount; i++) {
      final Constraint constraint = mip.addConstraint ("MeetSuppyl" + warehouses[i], Operator.LESS_EQUALS);
      constraint.getRhs ().addTerm (supply[i]);

      for (int j = 0; j < shopsCount; j++) {
        constraint.getLhs ().addTerm (1.0, variables[i][j]);
      }
    }

    for (int j = 0; j < shopsCount; j++) {
      final Constraint constraint = mip.addConstraint ("ServeDemand" + shops[j], Operator.EQUALS);
      constraint.getRhs ().addTerm (demand[j]);

      for (int i = 0; i < warehousesCount; i++) {
        constraint.getLhs ().addTerm (1.0, variables[i][j]);
      }
    }

    for (int i = 0; i < warehousesCount; i++) {
      this.variables.addAll (Arrays.asList (variables[i]));
    }

    return mip;
  }

  protected void verifyTransportExecutionResult (final ExecutionResult<Program> result) {
    final Solution bestSolution = result.getBestSolution ();
    final double x_A_S1 = bestSolution.getVariableValue (variables.get (0));
    final double x_A_S2 = bestSolution.getVariableValue (variables.get (1));
    final double x_A_S3 = bestSolution.getVariableValue (variables.get (2));
    final double x_B_S1 = bestSolution.getVariableValue (variables.get (3));
    final double x_B_S2 = bestSolution.getVariableValue (variables.get (4));
    final double x_B_S3 = bestSolution.getVariableValue (variables.get (5));

    assertEquals ("Expecting different value of x_A_S1", 5.0, x_A_S1, 0.0001);
    assertEquals ("Expecting different value of x_A_S2", 0.0, x_A_S2, 0.0001);
    assertEquals ("Expecting different value of x_A_S3", 0.0, x_A_S3, 0.0001);
    assertEquals ("Expecting different value of x_B_S1", 3.0, x_B_S1, 0.0001);
    assertEquals ("Expecting different value of x_B_S2", 5.0, x_B_S2, 0.0001);
    assertEquals ("Expecting different value of x_B_S3", 2.0, x_B_S3, 0.0001);

    assertEquals ("Expecting different objective value", 26, bestSolution.getObjectiveValue (), 0.0001);
    assertEquals ("Expecting different objective gap", 0.0, bestSolution.getRelativeOptimalityGap (), 0.0001);
  }

  protected static boolean isClassAvailable (String fqn) {
    try {
      Class.forName (fqn);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
