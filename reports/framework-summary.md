# Online Bookstore API - Test Execution Report

Executed at 2026-09-22 15:04:34

| Total | Passed | Flaky | Failed | Skipped | Pass rate | Elapsed | Cumulative |
|---|---|---|---|---|---|---|---|
| 28 | 28 | 0 | 0 | 0 | 100.0% | 0.7s | 1.1s |

Elapsed is wall-clock time; cumulative is the sum of test durations, which is larger because classes run in parallel.

| Status | Test case | Scenario | Attempts | Duration |
|---|---|---|---|---|
| ✅ PASS | `TC_FRAMEWORK_01_RequestIsolation.aContentTypeOverrideDoesNotLeak` | A request sent under an unusual content type does not change the next one | 1 | 697ms |
| ✅ PASS | `TC_FRAMEWORK_01_RequestIsolation.anOverriddenSpecificationCarriesTheRequestedType` | An overridden specification really does carry the requested content type | 1 | 4ms |
| ✅ PASS | `TC_FRAMEWORK_01_RequestIsolation.eachCallGetsItsOwnSpecification` | Each call receives its own request specification | 1 | 2ms |
| ✅ PASS | `TC_FRAMEWORK_02_RetryPolicy.aWrappedTransientFailureIsRecognised` | A transient failure nested inside another exception is still recognised | 1 | 29ms |
| ✅ PASS | `TC_FRAMEWORK_02_RetryPolicy.onlyTransientFailuresAreRetried` | Only failures that could plausibly succeed on a second attempt are retried <br>`['bad request', 'utils.common.UnexpectedStatusException: Expected status 200 but the API answered 400. Body was: {}', 'false']` | 1 | 3ms |
| ✅ PASS | `TC_FRAMEWORK_02_RetryPolicy.onlyTransientFailuresAreRetried` | Only failures that could plausibly succeed on a second attempt are retried <br>`['connection refused', 'java.net.ConnectException: Connection refused', 'true']` | 1 | 2ms |
| ✅ PASS | `TC_FRAMEWORK_02_RetryPolicy.onlyTransientFailuresAreRetried` | Only failures that could plausibly succeed on a second attempt are retried <br>`['gateway timeout', 'utils.common.UnexpectedStatusException: Expected status 200 but the API answered 504. Body was: {}', 'true']` | 1 | 1ms |
| ✅ PASS | `TC_FRAMEWORK_02_RetryPolicy.onlyTransientFailuresAreRetried` | Only failures that could plausibly succeed on a second attempt are retried <br>`['internal server error', 'utils.common.UnexpectedStatusException: Expected status 200 but the API answered 500. Body was: {}', 'false']` | 1 | 2ms |
| ✅ PASS | `TC_FRAMEWORK_02_RetryPolicy.onlyTransientFailuresAreRetried` | Only failures that could plausibly succeed on a second attempt are retried <br>`['invalid certificate', 'javax.net.ssl.SSLHandshakeException: PKIX path building failed', 'false']` | 1 | 1ms |
| ✅ PASS | `TC_FRAMEWORK_02_RetryPolicy.onlyTransientFailuresAreRetried` | Only failures that could plausibly succeed on a second attempt are retried <br>`['missing fixture', 'java.io.FileNotFoundException: contracts/openapi.json', 'false']` | 1 | 2ms |
| ✅ PASS | `TC_FRAMEWORK_02_RetryPolicy.onlyTransientFailuresAreRetried` | Only failures that could plausibly succeed on a second attempt are retried <br>`['not found', 'utils.common.UnexpectedStatusException: Expected status 200 but the API answered 404. Body was: {}', 'false']` | 1 | 1ms |
| ✅ PASS | `TC_FRAMEWORK_02_RetryPolicy.onlyTransientFailuresAreRetried` | Only failures that could plausibly succeed on a second attempt are retried <br>`['ordinary assertion failure', 'java.lang.AssertionError: A book must carry a title', 'false']` | 1 | 1ms |
| ✅ PASS | `TC_FRAMEWORK_02_RetryPolicy.onlyTransientFailuresAreRetried` | Only failures that could plausibly succeed on a second attempt are retried <br>`['read timed out', 'java.net.SocketTimeoutException: Read timed out', 'true']` | 1 | 1ms |
| ✅ PASS | `TC_FRAMEWORK_02_RetryPolicy.onlyTransientFailuresAreRetried` | Only failures that could plausibly succeed on a second attempt are retried <br>`['service unavailable', 'utils.common.UnexpectedStatusException: Expected status 200 but the API answered 503. Body was: {}', 'true']` | 1 | 1ms |
| ✅ PASS | `TC_FRAMEWORK_02_RetryPolicy.onlyTransientFailuresAreRetried` | Only failures that could plausibly succeed on a second attempt are retried <br>`['too many requests', 'utils.common.UnexpectedStatusException: Expected status 200 but the API answered 429. Body was: {}', 'true']` | 1 | 1ms |
| ✅ PASS | `TC_FRAMEWORK_02_RetryPolicy.onlyTransientFailuresAreRetried` | Only failures that could plausibly succeed on a second attempt are retried <br>`['unknown host', 'java.net.UnknownHostException: bookstore.invalid', 'true']` | 1 | 5ms |
| ✅ PASS | `TC_FRAMEWORK_02_RetryPolicy.onlyTransientFailuresAreRetried` | Only failures that could plausibly succeed on a second attempt are retried <br>`['unparsable payload', 'TS_FRAMEWORK.TC_FRAMEWORK_02_RetryPolicy$1: Cannot deserialize value of type OffsetDateTime', 'false']` | 1 | 1ms |
| ✅ PASS | `TC_FRAMEWORK_03_ExecutionContext.aDescriptionIsConsumedOnce` | A step description is delivered once and then cleared | 1 | 29ms |
| ✅ PASS | `TC_FRAMEWORK_03_ExecutionContext.anAbsentDescriptionIsEmpty` | Asking for a description that was never set yields nothing rather than failing | 1 | 2ms |
| ✅ PASS | `TC_FRAMEWORK_03_ExecutionContext.theContextIsShared` | The web service and its transport share one context | 1 | 221ms |
| ✅ PASS | `TC_FRAMEWORK_04_ExecutionSummary.aPassAfterAFailureIsFlaky` | A test that failed and then passed is reported as flaky, keeping the evidence | 1 | 27ms |
| ✅ PASS | `TC_FRAMEWORK_04_ExecutionSummary.aRetriedAttemptCountsAsAFailure` | An attempt TestNG marks as retried is treated as a failure, not a skip | 1 | 7ms |
| ✅ PASS | `TC_FRAMEWORK_04_ExecutionSummary.aSingleAttemptIsAPlainPass` | A clean pass stays a clean pass | 1 | 1ms |
| ✅ PASS | `TC_FRAMEWORK_04_ExecutionSummary.anUnretriedSkipStaysASkip` | A genuinely skipped test is still reported as skipped | 1 | 0ms |
| ✅ PASS | `TC_FRAMEWORK_04_ExecutionSummary.elapsedTimeIsWallClock` | Elapsed time is wall clock, not the sum of test durations | 1 | 8ms |
| ✅ PASS | `TC_FRAMEWORK_04_ExecutionSummary.flakyCasesAreCountedSeparately` | Flaky cases count as green in the pass rate but are reported separately | 1 | 1ms |
| ✅ PASS | `TC_FRAMEWORK_04_ExecutionSummary.foldingIsOrderIndependent` | Folding attempts gives the same answer whatever order they arrive in | 1 | 3ms |
| ✅ PASS | `TC_FRAMEWORK_04_ExecutionSummary.repeatedFailuresStayFailed` | A test that never recovered is reported as failed | 1 | 2ms |
