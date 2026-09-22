#!/usr/bin/env bash
#
# Runs the Online Bookstore suites and generates the reports.
#
# Two lanes, in the order the pipeline runs them:
#
#   1. the framework's own tests - no network, about a second. A gate: if the code
#      that builds and sends requests is broken, the API results below it are not
#      measuring what they claim to.
#   2. the API suite against the chosen environment.
#
# Usage:
#   ./run-tests.sh                         # FULL_RUN against dev
#   ./run-tests.sh SMOKE_RUN               # a named suite against dev
#   ./run-tests.sh BOOKS_ONLY local        # a named suite against a named environment
#   ./run-tests.sh FULL_RUN dev --skip-framework
#
# Anything this script does can be done directly with the Maven Wrapper; see the
# README. On Windows use `mvnw.cmd clean test -Pdev` instead.

set -u
set -o pipefail

if [[ $# -gt 3 ]]; then
    echo "Too many arguments. Usage: ./run-tests.sh [SUITE] [ENVIRONMENT] [--skip-framework]" >&2
    exit 2
fi

SUITE="${1:-FULL_RUN}"
ENVIRONMENT="${2:-dev}"
SKIP_FRAMEWORK="${3:-}"

if [[ -n "${SKIP_FRAMEWORK}" && "${SKIP_FRAMEWORK}" != "--skip-framework" ]]; then
    echo "Unknown option: ${SKIP_FRAMEWORK}" >&2
    echo "Usage: ./run-tests.sh [SUITE] [ENVIRONMENT] [--skip-framework]" >&2
    exit 2
fi
SUITE_FILE="src/test/resources/test-suites/${SUITE}.xml"
FRAMEWORK_SUITE="src/test/resources/test-suites/FRAMEWORK.xml"
ENVIRONMENT_FILE="src/main/resources/config/${ENVIRONMENT}.properties"
LOGS_DIR="./logs"

print_header() {
    echo
    echo "========================================================"
    echo "$1"
    echo "========================================================"
}

list_available() {
    echo "Available suites:" >&2
    ls -1 src/test/resources/test-suites/*.xml | sed 's|.*/||; s|\.xml$||; s|^|  |' >&2
    echo "Available environments:" >&2
    ls -1 src/main/resources/config/*.properties | sed 's|.*/||; s|\.properties$||; s|^|  |' >&2
}

if [[ ! -f "${SUITE_FILE}" ]]; then
    echo "No such suite: ${SUITE}" >&2
    list_available
    exit 2
fi

# Checked here rather than left to Maven, which answers an unknown profile with a
# warning and then silently runs against the default one.
if [[ ! -f "${ENVIRONMENT_FILE}" ]]; then
    echo "No such environment: ${ENVIRONMENT}" >&2
    list_available
    exit 2
fi

mkdir -p "${LOGS_DIR}"

# Cleaned once, here, rather than before each lane. Cleaning between them wiped the
# framework's Allure results and surefire XML, so the report generated at the end
# covered only the API run and the framework evidence was gone. Both lanes now write
# into the same results directory and the report covers the whole run.
print_header 'Preparing a clean workspace'
./mvnw -B clean > "${LOGS_DIR}/clean.log" 2>&1 || {
    echo "Could not clean the workspace - see ${LOGS_DIR}/clean.log" >&2
    exit 1
}

if [[ "${SKIP_FRAMEWORK}" != "--skip-framework" ]]; then
    print_header 'Framework tests (no network)'
    ./mvnw -B test -Dsuite.xml="${FRAMEWORK_SUITE}" 2>&1 | tee "${LOGS_DIR}/framework.log"
    FRAMEWORK_EXIT_CODE=${PIPESTATUS[0]}

    # Both TestNG runs write TEST-TestSuite.xml and the summary to the same paths,
    # so the API lane overwrites them. The Allure results do not collide and are left
    # where they are, which is why the report at the end covers both lanes.
    rm -rf "${LOGS_DIR}/framework-summary" "${LOGS_DIR}/framework-surefire"
    cp -R test-results/summary "${LOGS_DIR}/framework-summary" 2>/dev/null || true
    cp -R target/surefire-reports "${LOGS_DIR}/framework-surefire" 2>/dev/null || true

    if [[ "${FRAMEWORK_EXIT_CODE}" -ne 0 ]]; then
        print_header 'Stopping: the framework tests failed'
        echo "  The code that builds and sends requests is broken, so any API result"
        echo "  gathered now would be measuring the wrong thing."
        echo "  Report: $(pwd)/${LOGS_DIR}/framework-summary/index.html"
        exit "${FRAMEWORK_EXIT_CODE}"
    fi
fi

print_header "Running ${SUITE} against ${ENVIRONMENT}"
./mvnw -B test -P"${ENVIRONMENT}" -Dsuite.xml="${SUITE_FILE}" 2>&1 | tee "${LOGS_DIR}/tests.log"
TEST_EXIT_CODE=${PIPESTATUS[0]}

# Reporting is unconditional: a failed run is exactly when the report is wanted.
print_header 'Generating the Allure report'
REPORT_EXIT_CODE=0
if ./mvnw -B io.qameta.allure:allure-maven:report > "${LOGS_DIR}/report.log" 2>&1; then
    ALLURE_REPORT="$(pwd)/allure-report/index.html"
else
    ALLURE_REPORT="NOT GENERATED - see ${LOGS_DIR}/report.log"
    REPORT_EXIT_CODE=1
fi

print_header 'Reports'
if [[ -f test-results/summary/index.html ]]; then
    echo "  API summary       : $(pwd)/test-results/summary/index.html"
else
    echo "  API summary       : NOT GENERATED - the suite did not reach the reporting stage"
fi
if [[ -f "${LOGS_DIR}/framework-summary/index.html" ]]; then
    echo "  Framework summary : $(pwd)/${LOGS_DIR}/framework-summary/index.html"
    echo "  Framework JUnit   : $(pwd)/${LOGS_DIR}/framework-surefire/"
fi
echo "  Allure report     : ${ALLURE_REPORT}"
echo "                      (covers both lanes - results accumulate across them)"
echo "  Console log       : $(pwd)/${LOGS_DIR}/tests.log"

# A run that produced no report has not finished its job, even with every test
# green, so the failure is surfaced rather than swallowed. Test failures take
# precedence, because that is the more important thing to know about.
if [[ "${TEST_EXIT_CODE}" -ne 0 ]]; then
    exit "${TEST_EXIT_CODE}"
fi
exit "${REPORT_EXIT_CODE}"
