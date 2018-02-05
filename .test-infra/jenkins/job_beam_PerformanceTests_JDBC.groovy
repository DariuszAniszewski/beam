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

// This job runs the Beam performance tests on PerfKit Benchmarker.
job('beam_PerformanceTests_JDBC'){
    // Set default Beam job properties.
    common_job_properties.setTopLevelMainJobProperties(delegate)

    def pipelineArgs = [
            tempRoot: 'gs://apache-beam-io-testing',
            project: 'apache-beam-io-testing',
            postgresPort: '5432',
            numberOfRecords: '5000000'
    ]

    def pipelineArgList = []
    pipelineArgs.each({
        key, value -> pipelineArgList.add("\"--$key=$value\"")
    })
    def pipelineArgsJoined = "[" + pipelineArgList.join(',') + "]"

    def argMap = [
            beam_it_timeout: '1200',
            benchmarks: 'beam_integration_benchmark',
            beam_it_profile: 'io-it',
            beam_prebuilt: 'true',
            beam_sdk: 'java',
            beam_it_module: 'sdks/java/io/jdbc',
            beam_it_class: 'org.apache.beam.sdk.io.jdbc.JdbcIOIT',
            beam_it_options: pipelineArgsJoined,
            beam_kubernetes_scripts: makePathAbsolute('.test-infra/kubernetes/postgres/postgres.yml')
                    + ',' + makePathAbsolute('.test-infra/kubernetes/postgres/postgres-service-for-local-dev.yml'),
            beam_options_config_file: makePathAbsolute('.test-infra/kubernetes/postgres/pkb-config-local.yml'),
            bigquery_table: 'beam_performance.JdbcIOIT_pkb_results',
            container_cluster_cloud: 'GCP',
            machine_type: 'n1-standard-1',
            cloud: 'GCP',
            image: 'debian',
    ]

    common_job_properties.buildPerformanceTest(delegate, argMap)
}

static def makePathAbsolute(String path) {
    return '"$WORKSPACE/src/' + path + '"'
}