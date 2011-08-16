package org.fabrique.internal;

import java.lang.reflect.Method;

import java.util.Arrays;

import org.fabrique.Key;

/**
 * Produces error messages.
 */
public final class Errors {
  /**
   * Creates a new Errors object.
   */
  private Errors() {
  }

  /** Ambiguous arguments given */
  public static String ambiguousArgs(Class<?> type) {
    return "Cannot construct " + type + " with ambiguous arguments";
  }

  /** Binding already exists */
  public static String bindingExists(Key<?> key) {
    return "A binding already exists for " + key;
  }

  /** Duplicate params configured for some type */
  public static String duplicateParams(Class<?> type, Class<?>[] params) {
    return "The parameter set " + Arrays.toString(params) + " was configured more than once for "
        + type;
  }

  /** Inject for provider get method */
  public static String injectProviderGet(Method method) {
    return "@Inject is not allowed for provider 'get' method: " + method;
  }

  /** No default constructor */
  public static String noConstructor(Class<?> type, Object[] args) {
    return noConstructor(type, paramsFor(args));
  }

  /** No default constructor */
  public static String noConstructor(Class<?> type, Class<?>[] params) {
    String _params = params != null ? Arrays.toString(params) : "";
    return "Constructor does not exist or is not configured: " + type.getName() + "(" + _params
        + ")";
  }

  /** No default provider method */
  public static String noProviderMethod(Class<?> type, Object[] args) {
    return noProviderMethod(type, paramsFor(args));
  }

  /** No default provider method */
  public static String noProviderMethod(Class<?> type, Class<?>[] params) {
    String _params = params != null ? Arrays.toString(params) : "";
    return "Provider method does not exist or is not configured: " + type.getName() + ".get("
        + _params + ")";
  }

  /** Unresolved primitive */
  public static String unresolvedPrimitive(Class<?> type) {
    return "Could not resolve primitive type for " + type;
  }

  /** Converts args to class params */
  private static Class<?>[] paramsFor(Object[] args) {
    if (args == null)
      return null;

    Class<?>[] params = new Class<?>[args.length];
    for (int i = 0; i < args.length; i++)
      params[i] = args[i].getClass();

    return params;
  }
}
