/*
 * The same pipeline as .github/workflows/api-tests.yml, for a Jenkins controller.
 * Requires the Allure Jenkins plugin and a JDK 21 plus Maven tool installation.
 */
pipeline {
    agent any

    tools {
        jdk 'jdk-21'
        maven 'maven-3'
    }

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'local'],
                description: 'Environment profile to run against')
        choice(name: 'SUITE', choices: ['FULL_RUN', 'SMOKE_RUN', 'BOOKS_ONLY'],
                description: 'Suite to execute')
        booleanParam(name: 'RUN_DEPENDENCY_AUDIT', defaultValue: false,
                description: 'Run the OWASP dependency audit. Off by default: building the CVE ' +
                        'database takes minutes and has nothing to do with whether the API behaves. ' +
                        'The policy is a weekly audit, which belongs in a scheduled job of its own; ' +
                        'this parameter is for running it on demand.')
        booleanParam(name: 'INCLUDE_PROVIDER_CHARACTERISATION', defaultValue: false,
                description: 'Include the tests that pin current provider behaviour. Off for a ' +
                        'merge gate: those fail when FakeRestAPI changes, possibly for the better. ' +
                        'On for a nightly run, where that is exactly what you want to hear about.')
    }

    triggers {
        cron('H 6 * * *')
    }

    options {
        // A ceiling for the build; each stage below also has its own, so one slow
        // stage cannot quietly consume another's budget.
        timeout(time: 40, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }

    stages {
        stage('Static analysis') {
            options {
                timeout(time: 5, unit: 'MINUTES')
            }
            steps {
                sh './mvnw -B spotless:check checkstyle:check'
            }
        }

        // The framework's own tests: no network, about a second. Run first and
        // separately, so a failure here is unambiguously this repository's fault
        // rather than the shared sandbox having a bad moment.
        stage('Framework tests') {
            options {
                timeout(time: 10, unit: 'MINUTES')
            }
            steps {
                sh './mvnw -B clean test -Dsuite.xml=src/test/resources/test-suites/FRAMEWORK.xml'
            }
            post {
                // Published here, before the API stage runs. Both TestNG runs write
                // TEST-TestSuite.xml to the same path, so waiting until the end would
                // mean Jenkins only ever saw the API results.
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true,
                            keepProperties: true
                    archiveArtifacts artifacts: 'test-results/summary/**', allowEmptyArchive: true,
                            fingerprint: false
                    sh 'mkdir -p framework-results && cp -R target/surefire-reports test-results/summary ' +
                            'framework-results/ 2>/dev/null || true'
                    archiveArtifacts artifacts: 'framework-results/**', allowEmptyArchive: true
                }
            }
        }

        stage('API tests') {
            options {
                timeout(time: 20, unit: 'MINUTES')
            }
            steps {
                script {
                    def exclusions = params.INCLUDE_PROVIDER_CHARACTERISATION
                            ? ''
                            : '-DexcludedGroups=provider-behaviour'
                    // No clean: the framework lane's Allure results live in the same
                    // directory, and leaving them there is what lets the report at
                    // the end cover the whole build.
                    sh """
                        ./mvnw -B test \
                            -P${params.ENVIRONMENT} \
                            -Dsuite.xml=src/test/resources/test-suites/${params.SUITE}.xml \
                            ${exclusions}
                    """
                }
            }
        }

        // After the tests, never before. Building the CVE database is slow and has
        // nothing to do with whether the API behaves; running it first meant a slow
        // audit ate the test budget and a failed one skipped the tests entirely.
        stage('Dependency audit') {
            // Guarded. Without this it ran on every build - pull requests, manual
            // runs and nightlies alike - adding minutes to each for a check the
            // policy calls for weekly.
            when {
                expression { params.RUN_DEPENDENCY_AUDIT }
            }
            options {
                timeout(time: 30, unit: 'MINUTES')
            }
            steps {
                sh './mvnw -B org.owasp:dependency-check-maven:check -DskipTests'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/dependency-check-report.html',
                            allowEmptyArchive: true
                }
            }
        }
    }

    post {
        // A failed suite is exactly when the report is worth having, so reporting
        // is unconditional.
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            allure results: [[path: 'test-results/allure-results']]
            archiveArtifacts artifacts: 'test-results/summary/**', allowEmptyArchive: true
            publishHTML(target: [
                    reportDir            : 'test-results/summary',
                    reportFiles          : 'index.html',
                    reportName           : 'Execution Summary',
                    keepAll              : true,
                    alwaysLinkToLastBuild: true,
                    allowMissing         : true
            ])
        }
    }
}
