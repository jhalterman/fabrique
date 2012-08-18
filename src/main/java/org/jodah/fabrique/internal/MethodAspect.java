package org.jodah.fabrique.internal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.jodah.fabrique.intercept.IMethodInterceptor;
import org.jodah.fabrique.matcher.Matcher;

/**
 * Encapsulates an aspect that is method specific and ties a matcher to a method interceptor.
 */
public final class MethodAspect {
  private final List<IMethodInterceptor> interceptors;
  private final Matcher<? super Class<?>> classMatcher;
  private final Matcher<? super Method> methodMatcher;

  /**
   * Cosntructs a new MethodAspect.
   * 
   * @param classMatcher matches classes the interceptor should apply to. For example:
   *          {@code only(Runnable.class)}.
   * @param methodMatcher matches methods the interceptor should apply to. For example:
   *          {@code annotatedWith(Transactional.class)}.
   * @param pInterceptors to apply
   */
  MethodAspect(Matcher<? super Class<?>> classMatcher, Matcher<? super Method> methodMatcher,
      List<IMethodInterceptor> pInterceptors) {
    this.classMatcher = classMatcher;
    this.methodMatcher = methodMatcher;
    this.interceptors = pInterceptors;
  }

  /**
   * Creates a new MethodAspect object.
   * 
   * @param classMatcher .
   * @param methodMatcher .
   */
  MethodAspect(Matcher<? super Class<?>> classMatcher, Matcher<? super Method> methodMatcher,
      IMethodInterceptor... interceptors) {
    this(classMatcher, methodMatcher, Arrays.asList(interceptors));
  }

  /**
   * Creates a new MethodAspect object.
   * 
   * @param classMatcher .
   */
  MethodAspect(Matcher<? super Class<?>> classMatcher, IMethodInterceptor... interceptors) {
    this(classMatcher, null, Arrays.asList(interceptors));
  }

  /**
   * Gets the aspect's interceptors.
   * 
   * @return List<MethodInterceptor>
   */
  List<IMethodInterceptor> interceptors() {
    return interceptors;
  }

  /**
   * Determines if {@code type} matches the method aspect.
   * 
   * @param type .
   * 
   * @return true if matches
   */
  boolean matches(Class<?> type) {
    return classMatcher.matches(type);
  }

  /**
   * Determines if {@code method} matches the method aspect.
   * 
   * @param method .
   * 
   * @return true if matches
   */
  boolean matches(Method method) {
    return methodMatcher.matches(method);
  }
}
