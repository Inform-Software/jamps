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

package com.inform.jamps.modeling;

public enum VariableType {

  /**
   * Variable can get any real value between specified lower and upper bound
   */
  CONTINUOUS,

  /**
   * Variable can get values of 0 or 1
   */
  BINARY,

  /**
   * Variable can get any integer value between specified lower and upper bound
   */
  INTEGER,

  /**
   * Same as CONTINUOUS but value 0 is always allowed, even if 0 is not in bound range
   */
  SEMI_CONTINUOUS,

  /**
   * Same as INTEGER but value 0 is always allowed, even if 0 is not in bound range
   */
  SEMI_INTEGER
}
