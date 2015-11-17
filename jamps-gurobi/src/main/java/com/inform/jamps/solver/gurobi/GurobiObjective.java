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

import java.util.concurrent.atomic.AtomicLong;

import com.inform.jamps.modeling.Expression;
import com.inform.jamps.modeling.Objective;
import com.inform.jamps.modeling.ObjectiveSense;

public class GurobiObjective implements Objective {

  protected final static ObjectiveSense DEFAULT_SENSE     = ObjectiveSense.MINIMIZE;

  private final static AtomicLong       AUTO_NAME_COUNTER = new AtomicLong (0);

  private final GurobiExpression        expression;

  private final GurobiProgram           program;

  private final String                  name;

  private ObjectiveSense                sense;

  protected GurobiObjective (final GurobiProgram program) {
    this (program, DEFAULT_SENSE);
  }

  protected GurobiObjective (final GurobiProgram program,
                             final ObjectiveSense sense) {
    this (program, "obj" + AUTO_NAME_COUNTER.incrementAndGet (), sense);
  }

  protected GurobiObjective (final GurobiProgram program,
                             final String name,
                             final ObjectiveSense sense) {
    if (program == null) {
      throw new IllegalArgumentException ("Parameter program is mandatory and may not be null");
    }
    if (name == null) {
      throw new IllegalArgumentException ("Parameter name is mandatory and may not be null");
    }
    if (sense == null) {
      throw new IllegalArgumentException ("Parameter sense is mandatory and may not be null");
    }

    this.name = name;
    this.program = program;
    this.sense = sense;
    this.expression = new GurobiExpression (this);
  }

  @Override
  public String getName () {
    return name;
  }

  @Override
  public ObjectiveSense getObjectiveSense () {
    return sense;
  }

  @Override
  public void setObjectiveSense (final ObjectiveSense sense) {
    if (sense == null) {
      throw new IllegalArgumentException ("Parameter sense is mandatory and may not be null");
    }

    this.sense = sense;
  }

  @Override
  public Expression getExpression () {
    return expression;
  }

  protected GurobiProgram getProgram () {
    return program;
  }

  @Override
  public int compareTo (final Objective o) {
    if (!(o instanceof GurobiObjective)) {
      return -1;
    }

    final GurobiObjective grbObjective = ((GurobiObjective) o);
    int result = expression.compareTo (grbObjective.expression);
    if (result != 0) {
      return result;
    }

    result = sense.compareTo (grbObjective.sense);
    if (result != 0) {
      return result;
    }

    return name.compareTo (grbObjective.name);
  }

  @Override
  public final int hashCode () {
    final int prime = 31;
    int result = 1;
    result = prime * result + sense.hashCode ();
    result = prime * result + expression.hashCode ();
    result = prime * result + name.hashCode ();
    return result;
  }

  @Override
  public final boolean equals (final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof GurobiObjective)) {
      return false;
    }
    final GurobiObjective other = (GurobiObjective) obj;
    if (sense != other.sense) {
      return false;
    }
    if (!name.equals (other.name)) {
      return false;
    }
    if (!expression.equals (other.expression)) {
      return false;
    }
    return true;
  }

}
