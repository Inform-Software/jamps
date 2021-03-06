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

import java.io.File;

import com.inform.jamps.solver.SolverParameters;

import gurobi.GRBEnv;
import gurobi.GRBException;

public class GurobiSolverParameters implements SolverParameters {

  private File    outputDirectory;

  private boolean writeLPFile;

  private boolean writeMPSFile;

  private boolean useNamesForModelFileOutput = true;

  private boolean writeIISFile;

  private boolean writeSolutionFile;

  private boolean writeParameterFile;

  private boolean useCompressionForFileOuput;

  protected GurobiSolverParameters () {
    super ();
  }

  protected GRBEnv getNativeEnvironment () {
    try {
      return new GRBEnv ();
    } catch (GRBException e) {
      throw new IllegalStateException ("Unable to create native environment", e);
    }
  }

  public File getOutputDirectory () {
    return outputDirectory;
  }

  public void setOutputDirectory (final File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public boolean isWriteLPFile () {
    return writeLPFile;
  }

  public void setWriteLPFile (final boolean writeLPFile) {
    this.writeLPFile = writeLPFile;
  }

  public boolean isWriteMPSFile () {
    return writeMPSFile;
  }

  public void setWriteMPSFile (final boolean writeMPSFile) {
    this.writeMPSFile = writeMPSFile;
  }

  public boolean isUseNamesForModelFileOutput () {
    return useNamesForModelFileOutput;
  }

  public void setUseNamesForModelFileOutput (final boolean useNamesForModelFileOutput) {
    this.useNamesForModelFileOutput = useNamesForModelFileOutput;
  }

  public boolean isWriteIISFile () {
    return writeIISFile;
  }

  public void setWriteIISFile (final boolean writeIISFile) {
    this.writeIISFile = writeIISFile;
  }

  public boolean isWriteSolutionFile () {
    return writeSolutionFile;
  }

  public void setWriteSolutionFile (final boolean writeSolutionFile) {
    this.writeSolutionFile = writeSolutionFile;
  }

  public boolean isWriteParameterFile () {
    return writeParameterFile;
  }

  public void setWriteParameterFile (final boolean writeParameterFile) {
    this.writeParameterFile = writeParameterFile;
  }

  public boolean isUseCompressionForFileOuput () {
    return useCompressionForFileOuput;
  }

  public void setUseCompressionForFileOuput (final boolean useCompressionForFileOuput) {
    this.useCompressionForFileOuput = useCompressionForFileOuput;
  }

}
