package org.fabrique.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.fabrique.ConfigurationException;
import org.fabrique.Provider;

/**
 * Internally produces fully injected instances of {@code T}.
 * 
 * @param <T> Produced type
 */
public abstract class InternalFactory<T> {
  /** Represents a factory type */
  enum FactoryType {
    Provider, Target;
  }

  protected final Class<?> subject;
  protected final FactoryType factoryType;
  protected List<ConstructionInjector<?>> constructionInjectors;
  protected Set<Class<?>[]> optionalParams;
  protected Class<?>[] defaultParams;
  private List<MemberInjector> memberInjectors;

  /**
   * Creates a new InternalFactory object.
   * 
   * @param pOwner Class that injection occurs for
   */
  InternalFactory(Class<?> subject, FactoryType factoryType) {
    this.subject = subject;
    this.factoryType = factoryType;
  }

  /**
   * Performs field and method injection using member injectors.
   * 
   * @param context Injection context
   * @param object Object to perform injection against
   */
  protected void injectMembers(InjectionContext context, Object object) {
    for (MemberInjector injector : memberInjectors)
      injector.inject(context, object);
  }

  /**
   * Gets a fully injected instance of {@code T}. Called by providers that are already aware of the
   * appropriate construction injector to use.
   * 
   * @param pInjectionContext Injection context
   * @param pConstructionInjector Construction injector
   * @param args Construction args
   * @return T
   * @throws ProvisionException On failed provision or injection
   */
  abstract T get(InjectionContext pInjectionContext, ConstructionInjector<T> pConstructionInjector,
      Object[] args);

  /**
   * Gets a fully injected instance of {@code T} for args {@code args}.
   * 
   * @param pContext Injection Context
   * @param args Construction arguments
   * @return T
   * @throws ProvisionException On failed provision or injection
   */
  abstract T get(InjectionContext pContext, Object[] args);

  /**
   * Adds optional parameters for the factory {@code params}.
   * 
   * @param params Optional params
   * @throws ConfigurationException If {@code params} already exists
   */
  void addOptionalParams(Class<?>[][] params) {
    for (Class<?>[] paramSet : params)
      addOptionalParams(paramSet);
  }

  /**
   * Returns a provider for args {@code args}.
   * 
   * @param args Construction arguments
   * @return IProvider<T>
   * @throws ConfigurationException If provider 'get' method cannot be fulfilled with {@code args}
   */
  abstract Provider<T> getProvider(Object[] args);

  /**
   * Adds optional parameters for the factory {@code params}.
   * 
   * @param params Optional params
   * @throws ConfigurationException If {@code params} already exists
   */
  void addOptionalParams(Class<?>[] params) {
    if (Arrays.equals(defaultParams, params))
      throw new ConfigurationException(Errors.duplicateParams(subject, params));

    if (optionalParams == null)
      optionalParams = new HashSet<Class<?>[]>();

    for (Iterator<Class<?>[]> iterator = optionalParams.iterator(); iterator.hasNext();)
      if (Arrays.equals(iterator.next(), params))
        throw new ConfigurationException(Errors.duplicateParams(subject, params));

    optionalParams.add(params);
  }

  /**
   * Gets the subject class for the internal factory.
   * 
   * @return Class<?>
   */
  Class<?> getSubject() {
    return subject;
  }

  /**
   * Performs initial reflection to initialize the factory and create injectors.
   * 
   * @throws ConfigurationException On any failed configuration
   */
  void initialize() {
    memberInjectors = Injectors.memberInjectorsFor(subject);
  }

  /**
   * Performs initial injection for bound instances.
   * 
   * @throws InjectionException
   */
  void preInject() {
    return;
  }

  /**
   * Sets default parameters for the factory {@code params}.
   * 
   * @param params Default params
   */
  void setDefaultParams(Class<?>[] params) {
    defaultParams = params;
  }
}
