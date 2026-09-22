package TS_FRAMEWORK;

import java.lang.reflect.Proxy;
import java.util.Map;

import org.testng.IClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

/**
 * Builds the shapes TestNG really hands a reporter, so the summary's folding rules
 * can be exercised against them without running a suite to provoke one.
 * <p>
 * The interesting shape is an attempt that failed and is about to be retried: TestNG
 * files it as <em>skipped</em> with {@code wasRetried} set, not as a failure. Reading
 * that literally once made a test which failed twice and then passed report as a
 * clean pass.
 * <p>
 * Built as dynamic proxies rather than hand-written classes. {@link ITestResult} and
 * {@link ITestNGMethod} have upwards of sixty methods between them, of which the
 * summary calls seven; stubbing the rest by hand would be several hundred lines of
 * noise that breaks whenever TestNG adds a method.
 */
final class TestNgResults {

    private TestNgResults() {
    }

    /**
     * An attempt that failed and will be retried, exactly as TestNG records it.
     */
    static ITestResult retriedAway(String failureMessage) {
        return result(ITestResult.SKIP, true, new AssertionError(failureMessage), 1_000, 2_000);
    }

    /**
     * An attempt that succeeded.
     */
    static ITestResult passed() {
        return result(ITestResult.SUCCESS, false, null, 2_000, 3_000);
    }

    /**
     * A test genuinely skipped — a dependency failed, say — and never retried.
     */
    static ITestResult skipped() {
        return result(ITestResult.SKIP, false, null, 1_000, 1_000);
    }

    private static ITestResult result(int status, boolean retried, Throwable throwable,
                                      long startMillis, long endMillis) {
        Map<String, Object> answers = Map.of(
                "getStatus", status,
                "wasRetried", retried,
                "getStartMillis", startMillis,
                "getEndMillis", endMillis,
                "getParameters", new Object[0],
                "getMethod", method(),
                "getTestClass", testClass());
        return proxy(ITestResult.class, answers, throwable);
    }

    private static ITestNGMethod method() {
        return proxy(ITestNGMethod.class,
                Map.of("getMethodName", "method", "getDescription", "a scenario"), null);
    }

    private static IClass testClass() {
        return proxy(IClass.class, Map.of("getRealClass", TestNgResults.class), null);
    }

    /**
     * Answers the named methods; anything else returns a harmless default, so a
     * method the summary does not call cannot fail the stub.
     */
    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Map<String, Object> answers, Throwable throwable) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (target, invoked, arguments) -> {
                    if ("getThrowable".equals(invoked.getName())) {
                        return throwable;
                    }
                    if (answers.containsKey(invoked.getName())) {
                        return answers.get(invoked.getName());
                    }
                    return defaultFor(invoked.getReturnType());
                });
    }

    private static Object defaultFor(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return returnType.isArray() ? java.lang.reflect.Array.newInstance(returnType.getComponentType(), 0) : null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == void.class) {
            return null;
        }
        return 0;
    }
}
