package org.jodah.fabrique.internal;

import java.lang.reflect.Field;

import org.jodah.fabrique.InjectionException;
import org.jodah.fabrique.Key;

/**
 * Performs dependency injection for a single field.
 */
public class FieldInjector extends AbstractDependencyInjector implements MemberInjector {
  private final Field field;

  /**
   * Creates a new FieldInjector object.
   * 
   * @param field Field to inject
   * @param dependency .
   * @param optional .
   */
  FieldInjector(Field field, Key<?> dependency, boolean optional) {
    super(dependency, optional);
    this.field = field;
    field.setAccessible(true);
  }

  /**
   * {@inheritDoc}
   */
  public void inject(InjectionContext context, Object object) {
    try {
      field.set(object, injectDependencies(context)[0]);
    } catch (Exception e) {
      if (!optional)
        throw new InjectionException("Field injection failed for " + field, e);
    }
  }
}
