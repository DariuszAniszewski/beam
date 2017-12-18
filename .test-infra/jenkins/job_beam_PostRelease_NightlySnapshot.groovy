/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import common_job_properties

// This verifies the nightly snapshot build.
// From https://repository.apache.org/content/groups/snapshots/org/apache/beam.
job('beam_PostRelease_NightlySnapshot') {
  description('Runs post release verification of the nightly snapshot.')

  // Execute concurrent builds if necessary.
  concurrentBuild()

  // Set common parameters.
  common_job_properties.setTopLevelMainJobProperties(delegate)

  parameters {
    stringParam('snapshot_version',
                '2.3.0-SNAPSHOT',
                'Version of the repository snapshot to install')
    stringParam('snapshot_url',
                'https://repository.apache.org/content/repositories/snapshots',
                'Repository URL to install from')
  }

  wrappers {

  // This is a post-commit job that runs once per day, not for every push.
  common_job_properties.setPostCommit(
      delegate,
      '0 11 * * *',
      false,
      'dev@beam.apache.org')

  steps {
    // Run a quickstart from https://beam.apache.org/get-started/quickstart-java/
    shell('cd ' + common_job_properties.checkoutDir + '/release && groovy quickstart-java-direct.groovy ')
  }
}