package org.fabrique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fabrique.internal.Bindings;
import org.fabrique.internal.Errors;
import org.fabrique.internal.BindingLoader;
import org.fabrique.internal.InjectionContext;
import org.fabrique.internal.Validate;
import org.fabrique.util.MultiMap;

/**
 * A dynamic factory and dependency injection container with AOP interceptor support, capable of
 * producing fully injected instances based on configured bindings.
 * 
 * <p>
 * See {@link Binder} for information on creating bindings.
 * <p>
 * See {@link Inject} for information on defining injection points.
 */
public final class ObjectFactory {
  private static final Map<Key<?>, Binding<?>> bindings = new LinkedHashMap<Key<?>, Binding<?>>();
  private static final Map<Key<?>, Binding<?>> jitBindings = new HashMap<Key<?>, Binding<?>>();
  private static Map<Key<?>, Binding<?>> bindingsImmutable;
  private static MultiMap<Class<?>, Binding<?>> bindingsMultimap;
  private static final Object[] NULL_ARG = new Object[] { null };

  /** Matches a no argument constructor or method */
  public static final Object[] NO_ARGS = new Object[] {};

  /**
   * Binding loader implementation.
   */
  static BindingLoader bindingLoader = new BindingLoader() {
    /**
     * {@inheritDoc}
     */
    public void removeBinding(Binding<?> binding) {
      bindings.remove(binding);
    }

    /**
     * {@inheritDoc}
     */
    public void loadBinding(Binding<?> binding) throws ConfigurationException {
      Validate.notNull(binding, "Binding cannot be null");

      if (bindings.get(binding.getKey()) != null)
        throw new ConfigurationException(Errors.bindingExists(binding.getKey()));

      bindings.put(binding.getKey(), binding);

      if (bindingsMultimap != null)
        bindingsMultimap.put(binding.getKey().getType(), binding);
    }
  };

  /**
   * Private to prevent instantiation.
   */
  private ObjectFactory() {
  }

  /**
   * Finds all bindings for {@code type}.
   * 
   * @param <T> Bound type
   * @param type Type to find bindings for
   * @return List<Binding<T>>
   * @throws IllegalArgumentException if {@code type} is null
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> List<Binding<T>> findBindingsByType(Class<T> type) {
    if (type == null)
      throw new IllegalArgumentException("Type cannot be null");

    if (bindingsMultimap == null)
      indexBindings();

    return Collections.<Binding<T>>unmodifiableList((List) bindingsMultimap.get(type));
  }

  /**
   * Gets the binding for {@code type}.
   * 
   * @param <T> Bound type
   * @param type Type to get binding for
   * @return Binding<T>
   * @throws ConfigurationException if the factory cannot find the binding for {@code type}
   */
  public static <T> Binding<T> getBinding(Class<T> type) {
    return getBinding(Key.get(type));
  }

  /**
   * Gets a binding for {@code key}.
   * 
   * @param <T> Bound type
   * @param key Key to retrieve binding for
   * @return Binding<T>
   * @throws ConfigurationException if the factory cannot find the binding for {@code key}
   */
  @SuppressWarnings("unchecked")
  public static <T> Binding<T> getBinding(Key<T> key) {
    if (key == null)
      throw new ConfigurationException("Binding key cannot be null");

    Binding<T> binding = (Binding<T>) bindings.get(key);
    if (binding != null)
      return binding;

    binding = getJustInTimeBinding(key);
    if (binding == null)
      throw new ConfigurationException("Binding does not exist for " + key);

    return binding;
  }

  /**
   * Gets an immutable map of all bindings.
   * 
   * @return Map
   */
  public static Map<Key<?>, Binding<?>> getBindings() {
    if (bindingsImmutable == null)
      bindingsImmutable = Collections.unmodifiableMap(bindings);
    return bindingsImmutable;
  }

  /**
   * Shortcut method that gets an instance of the type bound for {@code type} using the default
   * constructor or get method for the binding's target or provider.
   * 
   * <p>
   * Shortcut for getInstance(type, Factory.NO_ARGS);
   * 
   * @param <T> Bound type
   * @param type Type to retrieve instance of
   * @return T
   * @throws ConfigurationException if the factory cannot find the binding for {@code type}
   * @throws ProvisionException if there was a runtime failure while providing an instance
   */
  public static <T> T getDefaultInstance(Class<T> type) {
    return getInstanceInternal(Key.get(type), NO_ARGS);
  }

  /**
   * Shortcut method that gets a named instance of the type bound for {@code pType} using the
   * default constructor or get method for the binding's target or provider.
   * 
   * <p>
   * Shortcut for getNamedInstance(pType, pName, Factory.NO_ARGS);
   * 
   * @param <T> Bound type
   * @param type Type to get instance of
   * @param name Name of binding to get instance of
   * @return T
   * @throws ConfigurationException if the factory cannot find the binding for {@code pType}
   * @throws ProvisionException if there was a runtime failure while providing an instance
   */
  public static <T> T getDefaultNamedInstance(Class<T> type, Object name) {
    return getInstanceInternal(Key.get(type, name), NO_ARGS);
  }

  /**
   * Gets an instance of the bound type for {@code key}.
   * 
   * @param <T> Bound type
   * @param key Key to retrieve instance for
   * @return T
   * @throws ConfigurationException If the factory cannot find the binding for {@code key}
   * @throws ProvisionException If there was a runtime failure while providing an instance
   */
  public static <T> T getInstance(Key<T> key) {
    return getInstanceInternal(key, (Object[]) null);
  }

  /**
   * Gets an instance of the bound type for {@code key} with construction arguments {@code args} .
   * Accepts null values for {@code args}.
   * 
   * <p>
   * The arguments will be passed to either the constructor of the target associated with the
   * binding, or to the get method of the provider associated with the binding.
   * 
   * @param <T> Bound type
   * @param key Key to retrieve instance for
   * @param args Construction arguments for producing an object bound by {@code key}
   * @return T
   * @throws ConfigurationException if the factory cannot find the binding for {@code key}
   * @throws ProvisionException if there was a runtime failure while providing an instance
   */
  public static <T> T getInstance(Key<T> key, Object... args) {
    return getInstanceInternal(key, args == null ? NULL_ARG : args);
  }

  /**
   * Gets all instances for the {@code pType} ordered by the binding creation order.
   * 
   * @param <T> Bound type
   * @param type to retrieve instances for
   * @return List of {@code pType} instances
   * @throws ConfigurationException if the factory cannot find bindings for {@code pType}
   * @throws ProvisionException if there was a runtime failure while providing instances
   */
  public static <T> List<T> getInstances(Class<T> type) {
    if (type == null)
      throw new IllegalArgumentException("Type cannot be null");

    if (bindingsMultimap == null)
      indexBindings();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    List<Binding<T>> bindings = (List) bindingsMultimap.get(type);
    if (bindings == null)
      throw new ConfigurationException("Bindings do not exist for " + type);

    List<T> _results = new ArrayList<T>(bindings.size());
    for (Binding<T> _binding : bindings)
      _results.add(_binding.getProvider().get());

    return _results;
  }

  /**
   * Gets an instance of the type bound for {@code pType}.
   * 
   * @param <T> Bound type
   * @param type Type to retrieve instance of
   * @return T
   * @throws ConfigurationException if the factory cannot find the binding for {@code pType}
   * @throws ProvisionException if there was a runtime failure while providing an instance
   */
  public static <T> T getInstance(Class<T> type) {
    return getInstanceInternal(Key.get(type), (Object[]) null);
  }

  /**
   * Gets an instance of the type bound for {@code pType} with construction arguments {@code args} .
   * Accepts null values for {@code args}.
   * 
   * <p>
   * The arguments will be passed to either the constructor of the target associated with the
   * binding, or to the get method of the provider associated with the binding.
   * 
   * @param <T> Bound type
   * @param type Type to retrieve instance of
   * @param args Construction arguments for producing {@code pType}
   * @return T
   * @throws ConfigurationException if the factory cannot find the binding for {@code pType}
   * @throws ProvisionException if there was a runtime failure while providing an instance
   */
  public static <T> T getInstance(Class<T> type, Object... args) {
    return getInstanceInternal(Key.get(type), args == null ? NULL_ARG : args);
  }

  /**
   * Gets a named binding for {@code pType} and {@code pName}.
   * 
   * @param <T> Bound type
   * @param type Type to get binding for
   * @param name Name
   * @return IBinding<T>
   * @throws ConfigurationException if the factory cannot find the binding for {@code pType} and
   *           {@code pName}.
   */
  public static <T> Binding<T> getNamedBinding(Class<T> type, Object name) {
    return getBinding(Key.get(type, name));
  }

  /**
   * Gets an instance of the type bound for {@code pType} as {@code pName}.
   * 
   * @param <T> Bound type
   * @param type Type to get instance of
   * @param name Name of binding to get instance of
   * @return T
   * @throws ConfigurationException if the factory cannot find the binding for {@code pType}
   * @throws ProvisionException if there was a runtime failure while providing an instance
   */
  public static <T> T getNamedInstance(Class<T> type, Object name) {
    return getInstanceInternal(Key.get(type, name), (Object[]) null);
  }

  /**
   * Gets an instance of the type bound for {@code pType} as {@code pName} with construction
   * arguments {@code args}. Accepts null values for {@code args}.
   * 
   * @param <T> Bound type
   * @param type Type to get instance of
   * @param name Name of binding to get instance of
   * @param args Construction arguments for producing {@code pType}
   * @return T
   * @throws ConfigurationException if the factory cannot find the binding for {@code pType}
   * @throws ProvisionException if there was a runtime failure while providing an instance
   */
  public static <T> T getNamedInstance(Class<T> type, Object name, Object... args) {
    return getInstanceInternal(Key.get(type, name), args == null ? NULL_ARG : args);
  }

  /**
   * Gets a provider for the bound type {@code pType} as name {@code pName}.
   * 
   * @param <T> Bound type
   * @param type Type to get provider for
   * @param name Name of binding to get provider for
   * @return IProvider<?extends T>
   * @throws ConfigurationException if the factory cannot find the binding for {@code pType} and
   *           {@code pName}.
   */
  public static <T> Provider<? extends T> getNamedProvider(Class<T> type, Object name) {
    return getBinding(Key.get(type, name)).getProvider();
  }

  /**
   * Gets a provider for the bound type {@code pType} as name {@code pName} with construction
   * arguments {@code args}. Accepts null values for {@code args}.
   * 
   * @param <T> Bound type
   * @param type Type to get provider for
   * @param name Name of binding to get provider for
   * @param args Construction arguments for creating a provider for {@code pType}
   * @return IProvider<?extends T>
   * @throws ConfigurationException if the factory cannot find the binding for {@code pType} and
   *           {@code pName}.
   */
  public static <T> Provider<? extends T> getNamedProvider(Class<T> type, Object name,
      Object... args) {
    return getBinding(Key.get(type, name)).getProvider(args == null ? NULL_ARG : args);
  }

  /**
   * Gets a provider for the bound type {@code pType}.
   * 
   * @param <T> Bound type
   * @param type Type to get provider for
   * @return IProvider<T>
   * @throws ConfigurationException if the factory cannot find the binding for {@code pType}
   */
  public static <T> Provider<T> getProvider(Class<T> type) {
    return getBinding(Key.get(type)).getProvider();
  }

  /**
   * Gets a provider for the bound type {@code pType} with construction arguments {@code args}.
   * Accepts null values for {@code args}.
   * 
   * @param <T> Bound type
   * @param type Type to get provider for
   * @param args Construction arguments for creating a provider for {@code pType}
   * @return IProvider<T>
   * @throws ConfigurationException if the factory cannot find the binding for {@code pType}
   */
  public static <T> Provider<T> getProvider(Class<T> type, Object... args) {
    return getBinding(Key.get(type)).getProvider(args == null ? NULL_ARG : args);
  }

  /**
   * Gets a provider for the bound type for {@code key} with construction arguments {@code args} .
   * Accepts null values for {@code args}.
   * 
   * @param <T> Bound type
   * @param key Key to get provider for
   * @param args Construction arguments for creating a provider for {@code pType}
   * @return IProvider<T>
   * @throws ConfigurationException if the factory cannot find the binding for {@code key}
   */
  public static <T> Provider<T> getProvider(Key<T> key, Object... args) {
    return getBinding(key).getProvider(args == null ? NULL_ARG : args);
  }

  /**
   * Gets a provider for the bound type for {@code key}.
   * 
   * @param <T> Bound type
   * @param key Key to get provider for
   * @return IProvider<T>
   * @throws ConfigurationException if the factory cannot find the binding for {@code key}
   */
  public static <T> Provider<T> getProvider(Key<T> key) {
    return getBinding(key).getProvider();
  }

  /**
   * Loads {@code modules} into the factory.
   * 
   * <p>
   * This method is not threadsafe and must be externally synchronized.
   * 
   * @param pModules Modules to load
   */
  public static void loadModules(Module... modules) {
    Validate.noNullElements(modules, "Modules cannot be null");
    Bindings.loadBindings(bindingLoader, modules);
  }

  /**
   * Gets an instance of the bound type for {@code key} with construction arguments {@code args} .
   */
  private static <T> T getInstanceInternal(Key<T> key, Object... args) {
    Binding<T> binding = getBinding(key);

    try {
      return binding.get(new InjectionContext(), args);
    } catch (Exception e) {
      throw new ProvisionException(key, e);
    }
  }

  /**
   * Gets and loads a just in time binding for {@code key}. Write operations are guarded by
   * {@code key.getType()}.
   * 
   * @param <T> Bound type
   * @param key Key
   * @return IBinding
   */
  @SuppressWarnings("unchecked")
  private static <T> Binding<T> getJustInTimeBinding(Key<T> key) {
    Binding<T> binding = null;

    synchronized (key.getType()) {
      binding = (Binding<T>) jitBindings.get(key);

      if (binding != null)
        return binding;
      binding = Bindings.create(key);
      jitBindings.put(key, binding);

      boolean success = false;

      try {
        Bindings.initialize(binding);
        success = true;
      } finally {
        if (!success)
          jitBindings.remove(key);
      }
    }

    return binding;
  }

  /**
   * Indexes bindings into the multimap. Additional loaded bindings will be maintained in the
   * multimap.
   */
  private static void indexBindings() {
    bindingsMultimap = new MultiMap<Class<?>, Binding<?>>();
    for (Binding<?> _binding : bindings.values())
      bindingsMultimap.put(_binding.getKey().getType(), _binding);
  }

  /**
   * Clears all bindings within the Factory. Intended for testing purposes only.
   */
  @SuppressWarnings("unused")
  private static void clearBindings() {
    bindings.clear();
    jitBindings.clear();
    if (bindingsMultimap != null)
      bindingsMultimap.clear();
  }
}
