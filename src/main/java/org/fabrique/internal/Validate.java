package org.fabrique.internal;

import java.lang.reflect.Modifier;

import org.fabrique.ConfigurationException;

/**
 * Utility class for performing validations. Throws {@link ConfigurationException} on failed
 * validation.
 */
public final class Validate {
    /**
     * Creates a new Validate object.
     */
    private Validate() {
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     * 
     * @param expression a boolean expression
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *            string using {@link String#valueOf(Object)}
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression)
            throw new IllegalArgumentException(String.valueOf(errorMessage));
    }

    /**
     * Validates that {@code pObject} is not null and that none of its elements are null.
     * 
     * @param objects Objects to validate
     * @param msg Error message
     * @throws ConfigurationException on failed validation
     */
    public static void noNullElements(Object[] objects, String msg) {
        notNull(objects, msg);

        for (Object object : objects)
            if (object == null)
                throw new ConfigurationException(msg);
    }

    /**
     * Validates that {@code objects} is not null and that none of its elements are null.
     * 
     * @param objects Objects to validate
     * @param msg Error message
     * @throws ConfigurationException on failed validation
     */
    public static void noNullElements(Object[][] objects, String msg) {
        notNull(objects, msg);

        for (Object[] outerObject : objects) {
            notNull(outerObject, msg);
            for (Object object : outerObject)
                if (object == null)
                    throw new ConfigurationException(msg);
        }
    }

    /**
     * Validates that {@code object} is not null.
     * 
     * @param object Object to validate
     * @param msg Error message
     * @throws ConfigurationException on failed validation
     */
    public static void notNull(Object object, String msg) {
        if (object == null)
            throw new ConfigurationException(msg);
    }

    /**
     * Validates that a type is not null, an interface or an abstract class.
     * 
     * @param type Type
     * @param pTypeString String representation of the type
     */
    public static void validateType(Class<?> type, String pTypeString) {
        Validate.notNull(type, pTypeString + " cannot be null");
        if (type.isInterface())
            throw new ConfigurationException(pTypeString + " cannot be an interface");
        if (Modifier.isAbstract(type.getModifiers()))
            throw new ConfigurationException(pTypeString + " cannot be abstract");

    }
}
