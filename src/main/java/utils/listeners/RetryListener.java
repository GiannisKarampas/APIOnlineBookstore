package utils.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import utils.common.Retry;

/**
 * Attaches {@link Retry} to every test, so no test method has to remember to ask for
 * it. Whether a given failure is actually retried is {@link Retry}'s decision, not
 * this listener's.
 */
public class RetryListener implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation testannotation, Class testClass, Constructor testConstructor, Method testMethod) {
        testannotation.setRetryAnalyzer(Retry.class);
    }
}
