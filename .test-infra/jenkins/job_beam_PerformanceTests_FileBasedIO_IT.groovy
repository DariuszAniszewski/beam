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

def testsParams = [
        [
                'beam_PerformanceTests_TextIOIT', // job name
                'Runs PerfKit tests for TextIOIT', // job description
                'org.apache.beam.sdk.io.text.TextIOIT', // it class
                'beam_performance.textioit_pkb_results', // BigQuery table for results
                null // extra Pipeline properties
        ],
        [

                'beam_PerformanceTests_Compressed_TextIOIT',
                'Runs PerfKit tests for TextIOIT with GZIP compression',
                'org.apache.beam.sdk.io.text.TextIOIT',
                'beam_performance.compressed_textioit_pkb_results',
                [
                        compressionType: 'GZIP'
                ]
        ],
        [
                'beam_PerformanceTests_AvroIOIT',
                'Runs PerfKit tests for AvroIOIT',
                'org.apache.beam.sdk.io.avro.AvroIOIT',
                'beam_performance.avroioit_pkb_results',
                null
        ],
        [
                'beam_PerformanceTests_TFRecordIOIT',
                'Runs PerfKit tests for beam_PerformanceTests_TFRecordIOIT',
                'org.apache.beam.sdk.io.tfrecord.TFRecordIOIT',
                'beam_performance.tfrecordioit_pkb_results',
                null
        ],
]

for (testParam in testsParams){
    def jobName = testParam[0]
    def jobDescription = testParam[1]
    def itClass = testParam[2]
    def bqTable = testParam[3]
    def extraPipelineArgs = testParam[4]

    // This job runs the file-based IOs performance tests on PerfKit Benchmarker.
    job(jobName) {
        description(jobDescription)

        // Set default Beam job properties.
        common_job_properties.setTopLevelMainJobProperties(delegate)

        // Allows triggering this build against pull requests.
    //    common_job_properties.enablePhraseTriggeringFromPullRequest(
    //            delegate,
    //            'Java FileBasedIOs Performance Test',
    //            'Run Java FileBasedIOs Performance Test')

        // Run job in postcommit every 6 hours, don't trigger every push, and
        // don't email individual committers.
    //    common_job_properties.setPostCommit(
    //            delegate,
    //            '0 */6 * * *',
    //            false,
    //            'commits@beam.apache.org',
    //            false)

        def pipelineArgs = [
                project: 'apache-beam-io-testing',
                tempRoot: 'gs://apache-beam-io-testing',
                numberOfRecords: '100000',
                filenamePrefix: 'gs://apache-beam-io-testing/filebased/${BUILD_ID}/TESTIOIT',
        ]
        if (extraPipelineArgs){
            pipelineArgs << extraPipelineArgs
        }

        def pipelineArgList = []
        pipelineArgs.each({
            key, value -> pipelineArgList.add("\"--$key=$value\"")
        })
        def pipelineArgsJoined = "[" + pipelineArgList.join(',') + "]"


        def argMap = [
            benchmarks: 'beam_integration_benchmark',
            beam_it_timeout: '1200',
            beam_it_profile: 'io-it',
            beam_prebuilt: 'true',
            beam_sdk: 'java',
            beam_it_module: 'sdks/java/io/file-based-io-tests',
            beam_it_class: itClass,
            beam_it_options: pipelineArgsJoined,
            beam_extra_mvn_properties: '["filesystem=gcs"]',
            bigquery_table: bqTable,
        ]
        common_job_properties.buildPerformanceTest(delegate, argMap)
    }
}