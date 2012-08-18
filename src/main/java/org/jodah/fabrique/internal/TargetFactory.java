package org.jodah.fabrique.internal;

/**
 * Produces fully injected instances of a target type.
 * 
 * @param <T> Type to produce
 */
public class TargetFactory<T> extends ConstructionFactory<T> {
  /**
   * Creates a new TargetFactory object.
   * 
   * @param target Target
   */
  TargetFactory(Class<?> target) {
    super(target, FactoryType.Target);
  }

  /**
   * {@inheritDoc}
   */
  T get(InjectionContext context, ConstructionInjector<T> constructionInjector, Object[] args) {
    T object = constructionInjector.construct(context, null, args);
    injectMembers(context, object);
    return object;
  }
}
