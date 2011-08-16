package org.fabrique;

import java.lang.reflect.Method;

import org.fabrique.builder.NamedBindingBuilder;
import org.fabrique.builder.TargetBindingBuilder;
import org.fabrique.intercept.IMethodInterceptor;
import org.fabrique.matcher.Matcher;

/**
 * Allows for the binding of interfaces to target objects or providers.
 * 
 * <h3>Binder EDSL (Embedded Domain Specific Language)</h3>
 * 
 * <h4>Creating Bindings</h4>
 * 
 * <p>
 * Simple bindings are created by binding an interface to a target implementation class or instance.
 * 
 * <pre>
 * bind(Service.class).to(ServiceImpl.class);
 * // or
 * bind(Service.class).toInstance(new ServiceImpl());</pre>
 * 
 * <p>
 * Usage of the factory is straightforward. For the binding configured above, an instance can be
 * retrieved from the ObjectFactory via:
 * 
 * <pre>
 * Service service = ObjectFactory.getInstance(Service.class);</pre>
 * 
 * <p>
 * The Binder EDSL utilizes type-checking to ensure that the target extends the bound type. In the
 * example above {@code ServiceImpl} must implement {@code Service}, else a compile error will
 * occur.
 * 
 * <h4>Named Bindings</h4>
 * 
 * <p>
 * Bindings can be given arbitrary names which can later be used to retrieve instances of the bound
 * object. This allows multiple bindings to be created for the same interface where each binding
 * represents a specific implementation.
 * 
 * <pre>
 * bind(Service.class).as("TRANSACTIONAL").to(TransactionalServiceImpl.class);
 * bind(Service.class).as("LAZY").to(LazyServiceImpl.class);</pre>
 * 
 * <p>
 * Retrieving an instance from a named binding is similarly straightforward:
 * 
 * <pre>
 * Service service = ObjectFactory.getNamedInstance(Service.class, "LAZY");</pre>
 * 
 * <h4>Providers</h4>
 * 
 * <p>
 * As an alternative to having the {@link ObjectObjectFactory} create instances of bound types for
 * you, you can bind your own provider to a type, allowing full control over instantiation. By
 * specifying a provider, the {@link ObjectObjectFactory} will call {@link Provider#get()} to
 * produce an instances of the bound type.
 * 
 * <pre>
 * bind(Service.class).toProvider(ServiceProvider.class);
 * // or
 * bind(Service.class).toProvider(new ServiceProvider());</pre>
 * 
 * <p>
 * In the example above {@code ServiceProvider} must implement {@code Provider<Service>}. This is
 * enforced via compiler type-checking.
 * 
 * <h4>Parameters</h4>
 * 
 * <p>
 * By default, the ObjectFactory utilizes the default constructor or the no argument provider 'get'
 * method to construct objects. Binding parameters allow you to specify that a different constructor
 * or provider 'get' method containing parameters be utilized to construct an object. This allows
 * providers in particular to make a logical decision as to which implementation they will produce,
 * based on the given arguments.
 * 
 * <p>
 * Parameters must correspond to a constructor when binding to a target or to an overloaded 'get'
 * method when binding to a {@link Provider}.
 * 
 * <pre>
 * bind(DAO.class).to(DAOImpl.class).withParams(DBConnection.class);
 * bind(Service.class).toProvider(ServiceProvider.class).withParams(Dependency.class);</pre>
 * 
 * <p>
 * The example above creates two bindings which by <i>default</i> will use the
 * {@code DAOImpl(DBConnection)} constructor and the {@code ServiceProvider.get(Dependency)} method
 * to construct instances of DAO and Service, respectively. When doing so, arguments can be
 * explicitly provided or fulfilled automatically by the {@link ObjectObjectFactory} via other
 * bindings.
 * 
 * <pre>
 * // ObjectFactory retrieval
 * ObjectFactory.getInstance(DAO.class, connection);
 * ObjectFactory.getInstance(Service.class);</pre>
 * 
 * <p>
 * In this example, the DAO ObjectFactory retrieval uses an explicit DBConnection argument to
 * retrieve an DAO instance. The Service factory retrieval has its arguments fulfilled internally by
 * the factory, assuming that a binding exists elsewhere for Dependency. If an argument is not given
 * and cannot be fulfilled internally by the ObjectFactory, an exception will be thrown.
 * 
 * <h4>Optional Parameters</h4>
 * 
 * <p>
 * In the case that the bound target has multiple constructors or the provider has multiple
 * overloaded 'get' methods, <i>optional</i> parameter sets can also be defined for a binding. These
 * optional parameters will only be used when the user explicitly passes arguments to the
 * ObjectFactory.
 * 
 * <pre>
 * bind(IRouter.class).to(RouterImpl.class).withParams(IInvTxn.class).withOptionalParams(IInvTxn.class, String.class);
 * bind(IComputerSystem.class).to(ComputerSystemImpl.class).withOptionalParams(IInvTxn.class).withOptionalParams(IInvTxn.class, String.class);
 * bind(IInvBaseObject.class).to(InvBaseObject.class).withOptionalParams(new Class[][] {
 *   { IOWBean.class, String.class },
 *   { IInvBaseObject.class },
 * });</pre>
 * 
 * <p>
 * The example above shows the different ways that optional parameters can be defined. They can be
 * given with or without having to define default parameters, they can be chained together, or they
 * can be given in a two-dimensional array.
 * 
 * <p>
 * Note: When defining default parameters for your binding, the default constructor or provider
 * 'get' method is replaced. In order to make the default constructor or provider 'get' method
 * accessible, it must be defined via optional parameters.
 * 
 * <pre>
 * bind(Router.class).to(RouterImpl.class).withParams(Service.class).withOptionalParams(NO_PARAMS);</pre>
 * 
 * <p>
 * The example above shows how the default constructor or provider 'get' method can be configured
 * via an optional parameter set. Invoking the default constructor or provider 'get' method can be
 * performed via a shortcut method: {@link ObjectObjectFactory#getDefaultInstance(Class)}.
 * 
 * <h4>Scopes</h4>
 * 
 * <p>
 * Scopes allow the ObjectFactory to create object instances based on the scope of some application
 * state. Example scopes might be thread, session, request, transactional, rule or singleton. For
 * example: for a type bound in thread scope, the ObjectFactory would return the same object
 * instance when being called within the context of the same thread.
 * 
 * <pre>
 * bind(Service.class).to(ServiceImpl.class).in(Scopes.SINGLETON);
 * bind(DBConnection.class).to(DBConnection.class).in(Scopes.RULE);
 * bind(ComputerSystem.class).to(ComputerSystemImpl).forParams(IOWBOMClient.class).in(Scopes.THREAD);
 * bind(Service.class).to(ServiceImpl.class).in(Scopes.EAGER_SINGLETON);</pre>
 * 
 * <p>
 * Shortcut methods exist for singleton and eager singleton scopes:
 * 
 * <pre>
 * bind(Service.class).to(ServiceImpl.class).asSingleton();
 * bind(Service.class).to(ServiceImpl.class).asEagerSingleton();</pre>
 * 
 * <p>
 * Eager singleton scope will eagerly instantiate an instance of the bound type immediately after
 * the binding is loaded into the ObjectFactory.
 * 
 * <h4>Interceptors</h4>
 * 
 * Interceptors allow you to intercept method invocations.
 * 
 * <pre>
 * bindInterceptors(Matchers.any(), Matchers.isMethod("toString"), new ToStringInterceptor());
 * bindInterceptors(Matchers.annotatedWith(Trace.class), Matchers.any(), new TraceInterceptor());
 * bindInterceptors(Matchers.any(), new AccessorInterceptor());
 * </pre>
 * 
 * <h4>Binding Keys</h4>
 * 
 * <p>
 * Bindings are identified by a {@link Key} where the key consists of the type being bound and the
 * optional name. Multiple bindings for the same type are allowed, but not for the same key. A
 * binding {@link Key} can be utilized to perform various operations throughout the binder API.
 * 
 * <pre>
 * bind(List.class).to(ArrayList.class);
 * bind(List.class).as("THREADSAFE").to(Vector.class);
 * // same as
 * bind(Key.get(List.class)).to(ArrayList.class);
 * bind(Key.get(List.class, "THREADSAFE")).to(Vector.class);</pre>
 * 
 * <h4>Possible Uses</h4>
 * 
 * <p>
 * Any combination of the described methods can be used to bind an interface to a target type or
 * instance, a provider type or instance, with or without a name, with or without explicit default
 * or optional parameters, and with or without a specific scope.
 * 
 * <pre>
 * bind(Service.class).to(TestService.class).withParams(IOWBOMClient.class).asSingleton();
 * bind(Service.class).as("TRANSACTIONAL").toProvider(TransactionalServiceProvider.class).in(Scopes.TRANSACTIONAL);</pre>
 */
public interface Binder {
  /**
   * Creates a named binding builder for {@code type}.
   * 
   * @param <T> Bound type
   * @param type Type to bind
   * @return NamedBindingBuilder<T>
   * @throws ConfigurationException if {@code type} is a primitive class
   */
  <T> NamedBindingBuilder<T> bind(Class<T> type);

  /**
   * Creates a targeted binding builder for {@code key}.
   * 
   * @param <T> Bound type
   * @param key Key to bind
   * @return TargetBindingBuilder<T>
   * @throws ConfigurationException if {@code key} represents a primitive class
   */
  <T> TargetBindingBuilder<T> bind(Key<T> key);

  /**
   * Binds one or more method interceptors to methods matched by class and method matchers. A method
   * is eligible for interception if:
   * 
   * <ul>
   * <li>The ObjectFactory created the instance the method is on</li>
   * <li>Neither the enclosing type nor the method is final</li>
   * <li>And the method is package-private, protected, or public</li>
   * </ul>
   * 
   * @param classMatcher matches classes the interceptor should apply to. For example:
   *          {@code only(Runnable.class)}.
   * @param methodMatcher matches methods the interceptor should apply to. For example:
   *          {@code annotatedWith(Transactional.class)}.
   * @param interceptors to bind
   */
  void bindInterceptors(Matcher<? super Class<?>> classMatcher,
      Matcher<? super Method> methodMatcher, IMethodInterceptor... interceptors);

  /**
   * Installs additional modules or sub-modules into the factory for the current binder instance.
   * 
   * @param module Module to install
   */
  void install(Module module);
}
