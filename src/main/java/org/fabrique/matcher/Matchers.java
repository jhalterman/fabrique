package org.fabrique.matcher;

import java.io.Serializable;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import java.util.Arrays;

import org.fabrique.internal.Validate;

/**
 * Utility for creating matcher implementations.
 * 
 * @author Bob Lee (crazybob@google.com)
 */
public class Matchers {
    private static final Matcher<Object> ANY = new Any();

    /**
     * Creates a new Matchers object.
     */
    private Matchers() {
    }

    /**
     * Annotated with matcher.
     */
    private static class AnnotatedWith extends AbstractMatcher<AnnotatedElement> implements
            Serializable {
        private static final long serialVersionUID = 0;
        private final Annotation annotation;

        /**
         * Creates a new AnnotatedWith object.
         * 
         * @param annotation Annotation
         */
        public AnnotatedWith(Annotation annotation) {
            Validate.notNull(annotation, "Annotation cannot be null");
            this.annotation = annotation;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof AnnotatedWith
                    && ((AnnotatedWith) other).annotation.equals(annotation);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 37 * annotation.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        public boolean matches(AnnotatedElement element) {
            Annotation fromElement = element.getAnnotation(annotation.annotationType());

            return (fromElement != null) && annotation.equals(fromElement);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "annotatedWith(" + annotation + ")";
        }
    }

    /**
     * Annotated with matcher.
     */
    private static class AnnotatedWithType extends AbstractMatcher<AnnotatedElement> implements
            Serializable {
        private static final long serialVersionUID = 0;
        private final Class<? extends Annotation> annotationType;

        /**
         * Creates a new AnnotatedWithType object.
         * 
         * @param annotationType Type to create matcher for
         */
        public AnnotatedWithType(Class<? extends Annotation> annotationType) {
            Validate.notNull(annotationType, "Annotation type cannot be null");
            this.annotationType = annotationType;
            checkForRuntimeRetention(annotationType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof AnnotatedWithType
                    && ((AnnotatedWithType) other).annotationType.equals(annotationType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 37 * annotationType.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        public boolean matches(AnnotatedElement element) {
            return element.getAnnotation(annotationType) != null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "annotatedWith(" + annotationType.getSimpleName() + ".class)";
        }
    }

    /**
     * Any matcher
     */
    private static class Any extends AbstractMatcher<Object> implements Serializable {
        private static final long serialVersionUID = 0;

        /**
         * {@inheritDoc}
         */
        public boolean matches(Object object) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public Object readResolve() {
            return any();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "any()";
        }
    }

    /**
     * Identical to matcher
     */
    private static class IdenticalTo extends AbstractMatcher<Object> implements Serializable {
        private static final long serialVersionUID = 0;
        private final Object value;

        /**
         * Creates a new IdenticalTo object.
         * 
         * @param value .
         */
        public IdenticalTo(Object value) {
            Validate.notNull(value, "Value");
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof IdenticalTo && (((IdenticalTo) other).value == value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 37 * System.identityHashCode(value);
        }

        /**
         * {@inheritDoc}
         */
        public boolean matches(Object other) {
            return value == other;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "identicalTo(" + value + ")";
        }
    }

    /**
     * In package matcher.
     */
    private static class InPackage extends AbstractMatcher<Class> implements Serializable {
        private static final long serialVersionUID = 0;
        private final transient Package targetPackage;
        private final String packageName;

        /**
         * Creates a new InPackage object.
         * 
         * @param targetPackage .
         */
        public InPackage(Package targetPackage) {
            Validate.notNull(targetPackage, "Target package cannot be null");
            this.targetPackage = targetPackage;
            packageName = targetPackage.getName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof InPackage
                    && ((InPackage) other).targetPackage.equals(targetPackage);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 37 * targetPackage.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        public boolean matches(Class c) {
            return c.getPackage().equals(targetPackage);
        }

        /**
         * {@inheritDoc}
         */
        public Object readResolve() {
            return inPackage(Package.getPackage(packageName));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "inPackage(" + targetPackage.getName() + ")";
        }
    }

    /**
     * In sub-package matcher.
     */
    private static class InSubpackage extends AbstractMatcher<Class> implements Serializable {
        private static final long serialVersionUID = 0;
        private final String targetPackageName;

        /**
         * Creates a new InSubpackage object.
         * 
         * @param targetPackageName .
         */
        public InSubpackage(String targetPackageName) {
            this.targetPackageName = targetPackageName;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof InSubpackage
                    && ((InSubpackage) other).targetPackageName.equals(targetPackageName);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 37 * targetPackageName.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        public boolean matches(Class c) {
            String classPackageName = c.getPackage().getName();

            return classPackageName.equals(targetPackageName)
                    || classPackageName.startsWith(targetPackageName + ".");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "inSubpackage(" + targetPackageName + ")";
        }
    }

    /**
     * Method matcher.
     */
    private static class IsMethod extends AbstractMatcher<Method> implements Serializable {
        private static final long serialVersionUID = 0;
        private static final Class<?>[] NO_PARAMS = new Class<?>[] {};
        private final String methodName;
        private final Class<?>[] params;

        /**
         * Creates a new MethodMatcher object.
         * 
         * @param methodName Method name
         */
        public IsMethod(String methodName) {
            Validate.notNull(methodName, "Method name cannot be null");
            this.methodName = methodName;
            params = NO_PARAMS;
        }

        /**
         * Creates a new MethodMatcher object.
         * 
         * @param methodName Method name
         * @param params Method params
         */
        public IsMethod(String methodName, Class<?>[] params) {
            Validate.notNull(methodName, "Method name cannot be null");
            Validate.noNullElements(params, "Method params cannot be null");
            this.methodName = methodName;
            this.params = params;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof IsMethod && ((IsMethod) other).methodName.equals(methodName)
                    && Arrays.equals(((IsMethod) other).params, params);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 37 * methodName.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(Method method) {
            return method.getName().equals(methodName)
                    && Arrays.equals(params, method.getParameterTypes());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "method(" + methodName + ")";
        }
    }

    /**
     * Not matcher.
     */
    private static class Not<T> extends AbstractMatcher<T> implements Serializable {
        private static final long serialVersionUID = 0;
        final Matcher<? super T> delegate;

        /**
         * Creates a new Not object.
         * 
         * @param delegate .
         */
        private Not(Matcher<? super T> delegate) {
            Validate.notNull(delegate, "Delegate cannot be null");
            this.delegate = delegate;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof Not && ((Not) other).delegate.equals(delegate);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return -delegate.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        public boolean matches(T t) {
            return !delegate.matches(t);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "not(" + delegate + ")";
        }
    }

    /**
     * Only matcher
     */
    private static class Only extends AbstractMatcher<Object> implements Serializable {
        private static final long serialVersionUID = 0;
        private final Object value;

        /**
         * Creates a new Only object.
         * 
         * @param value .
         */
        public Only(Object value) {
            Validate.notNull(value, "Value cannot be null");
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof Only && ((Only) other).value.equals(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 37 * value.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        public boolean matches(Object other) {
            return value.equals(other);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "only(" + value + ")";
        }
    }

    /**
     * Returns matcher.
     */
    private static class Returns extends AbstractMatcher<Method> implements Serializable {
        private static final long serialVersionUID = 0;
        private final Matcher<? super Class<?>> returnType;

        /**
         * Creates a new Returns object.
         * 
         * @param returnType .
         */
        public Returns(Matcher<? super Class<?>> returnType) {
            Validate.notNull(returnType, "Return type cannot be null");
            this.returnType = returnType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof Returns && ((Returns) other).returnType.equals(returnType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 37 * returnType.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        public boolean matches(Method method) {
            return returnType.matches(method.getReturnType());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "returns(" + returnType + ")";
        }
    }

    /**
     * Subclass matcher.
     */
    private static class SubclassesOf extends AbstractMatcher<Class> implements Serializable {
        private static final long serialVersionUID = 0;
        private final Class<?> superclass;

        /**
         * Creates a new SubclassesOf object.
         * 
         * @param superclass Superclass
         */
        public SubclassesOf(Class<?> superclass) {
            Validate.notNull(superclass, "Superclass cannot be null");
            this.superclass = superclass;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof SubclassesOf
                    && ((SubclassesOf) other).superclass.equals(superclass);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 37 * superclass.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        public boolean matches(Class subclass) {
            return superclass.isAssignableFrom(subclass);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "subclassesOf(" + superclass.getSimpleName() + ".class)";
        }
    }

    /**
     * Returns a matcher which matches elements (methods, classes, etc.) with a given annotation.
     */
    public static Matcher<AnnotatedElement> annotatedWith(
            final Class<? extends Annotation> annotationType) {
        return new AnnotatedWithType(annotationType);
    }

    /**
     * Returns a matcher which matches elements (methods, classes, etc.) with a given annotation.
     */
    public static Matcher<AnnotatedElement> annotatedWith(final Annotation annotation) {
        return new AnnotatedWith(annotation);
    }

    /**
     * Returns a matcher which matches any input.
     */
    public static Matcher<Object> any() {
        return ANY;
    }

    /**
     * Returns a matcher which matches only the given object.
     */
    public static Matcher<Object> identicalTo(final Object value) {
        return new IdenticalTo(value);
    }

    /**
     * Returns a matcher which matches classes in the given package. Packages are specific to their
     * classloader, so classes with the same package name may not have the same package at runtime.
     */
    public static Matcher<Class> inPackage(final Package targetPackage) {
        return new InPackage(targetPackage);
    }

    /**
     * Returns a matcher which matches classes in the given package and its subpackages. Unlike
     * {@link #inPackage(Package) inPackage()}, this matches classes from any classloader.
     */
    public static Matcher<Class> inSubpackage(final String targetPackageName) {
        return new InSubpackage(targetPackageName);
    }

    /**
     * Returns a matcher which matches any method matching with the given name.
     */
    public static Matcher<Method> isMethod(String pMethodName) {
        return new IsMethod(pMethodName);
    }

    /**
     * Returns a matcher which matches any method matching the given name and parameters.
     */
    public static Matcher<Method> isMethod(String pMethodName, Class<?>[] pParams) {
        return new IsMethod(pMethodName, pParams);
    }

    /**
     * Inverts the given matcher.
     */
    public static <T> Matcher<T> not(final Matcher<? super T> p) {
        return new Not<T>(p);
    }

    /**
     * Returns a matcher which matches objects equal to the given object.
     */
    public static Matcher<Object> only(Object pValue) {
        return new Only(pValue);
    }

    /**
     * Returns a matcher which matches methods with matching return types.
     */
    public static Matcher<Method> returns(final Matcher<? super Class<?>> pReturnType) {
        return new Returns(pReturnType);
    }

    /**
     * Returns a matcher which matches subclasses of the given type (as well as the given type).
     */
    public static Matcher<Class> subclassesOf(final Class<?> pSuperclass) {
        return new SubclassesOf(pSuperclass);
    }

    /**
     * Checks {@code pAnnotationType} to see if its retention is set to runtime.
     * 
     * @param pAnnotationType Type to check
     */
    private static void checkForRuntimeRetention(Class<? extends Annotation> pAnnotationType) {
        Retention retention = pAnnotationType.getAnnotation(Retention.class);
        Validate.checkArgument(retention != null && retention.value() == RetentionPolicy.RUNTIME,
                "Annotation " + pAnnotationType.getSimpleName() + " is missing RUNTIME retention");
    }
}
