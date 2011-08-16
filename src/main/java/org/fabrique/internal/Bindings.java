package org.fabrique.internal;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.fabrique.ConfigurationException;
import org.fabrique.Binder;
import org.fabrique.Binding;
import org.fabrique.Module;
import org.fabrique.Key;
import org.fabrique.Scopes;
import org.fabrique.builder.NamedBindingBuilder;
import org.fabrique.builder.TargetBindingBuilder;
import org.fabrique.intercept.IMethodInterceptor;
import org.fabrique.matcher.Matcher;

/**
 * Utility methods for creating, loading and initializing bindings.
 * 
 */
public final class Bindings {
    List<BindingImpl<?>> bindings = new ArrayList<BindingImpl<?>>();

    /**
     * Creates a new Bindings object.
     */
    private Bindings() {
    }

    /**
     * Binder implementation. Creates binding builders and installs modules.
     */
    private static class BinderImpl implements Binder {
        private final List<BindingImpl<?>> bindings = new ArrayList<BindingImpl<?>>();
        private final List<MethodAspect> methodAspects = new ArrayList<MethodAspect>();
        private final Set<Class<?>> modules = new HashSet<Class<?>>();

        /**
         * {@inheritDoc}
         */
        public <T> NamedBindingBuilder<T> bind(Class<T> type) {
            Validate.notNull(type, "Type");
            return new BindingBuilder<T>(bindings, type);
        }

        /**
         * {@inheritDoc}
         */
        public <T> TargetBindingBuilder<T> bind(Key<T> key) {
            Validate.notNull(key, "Key");
            return new BindingBuilder<T>(bindings, key);
        }

        /**
         * {@inheritDoc}
         */
        public void bindInterceptors(Matcher<? super Class<?>> classMatcher,
                Matcher<? super Method> methodMatcher, IMethodInterceptor... interceptors) {
            Validate.notNull(classMatcher, "Class matcher");
            Validate.notNull(classMatcher, "Method matcher");
            Validate.noNullElements(interceptors, "Interceptors");
            methodAspects.add(new MethodAspect(classMatcher, methodMatcher, interceptors));
        }

        /**
         * {@inheritDoc}
         */
        public void install(Module module) {
            if (modules.add(module.getClass()))
                module.configure(this);
        }

        /**
         * Gets the method aspects for the binder.
         * 
         * @return List of MethodAspects
         */
        List<MethodAspect> getMethodAspects() {
            return methodAspects;
        }

        /**
         * Gets the bindings for the binder.
         * 
         * @return List<BindingImpl<?>>
         */
        List<BindingImpl<?>> getBindings() {
            return bindings;
        }
    }

    /**
     * Creates an uninitialized binding for {@code key} where the target type is the same as the
     * bound type.
     * 
     * @param <T> Bound type
     * @param key Key to create binding for
     * @return Binding for T
     */
    public static <T> BindingImpl<T> create(Key<T> key) {
        Validate.validateType(key.getType(), "Type");
        return new BindingImpl<T>(key, new TargetFactory<T>(key.getType()));
    }

    /**
     * Loads and initializes bindings into {@code bindingLoader} for {@code modules}.
     * 
     * @param bindingLoader Binding storage
     * @param modules Modules to load bindings from
     * @throws ConfigurationException If initialization or pre-injection fails
     */
    public static void loadBindings(BindingLoader bindingLoader, Module[] modules) {
        BinderImpl binder = new BinderImpl();

        for (Module module : modules)
            binder.install(module);

        // Load method aspects
        for (MethodAspect methodAspect : binder.getMethodAspects())
            AspectStore.addMethodAspect(methodAspect);

        // Initialize and load bindings
        for (BindingImpl<?> binding : binder.getBindings()) {
            initialize(binding);
            bindingLoader.loadBinding(binding);
        }

        Iterator<BindingImpl<?>> iterator = binder.getBindings().listIterator();

        // Pre-inject bindings
        while (iterator.hasNext()) {
            BindingImpl<?> _binding = iterator.next();

            try {
                preInject(_binding);
            } catch (Exception e) {
                iterator.remove();
                throw new ConfigurationException(e);
            }
        }
    }

    /**
     * Initializes {@code binding} and validates that it is bound to a valid type.
     * 
     * @param binding Binding to initialize
     * @throws ConfigurationException If binding is uninitialized and invalid
     */
    public static <T> void initialize(Binding<T> binding) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        InternalFactory<T> internalFactory = ((BindingImpl) binding).getInternalFactory();
        Key<?> key = binding.getKey();

        if (internalFactory.getSubject().equals(key.getType()))
            Validate.validateType(key.getType(), "Untargetted bound type");

        internalFactory.initialize();
    }

    /**
     * Pre-injects {@code binding} and loads eager singletons.
     * 
     * @param binding Binding to initialize
     */
    private static <T> void preInject(BindingImpl<T> binding) {
        binding.getInternalFactory().preInject();

        if (Scopes.EAGER_SINGLETON.equals(binding.getScope())) {
            try {
                binding.getProvider().get();
            } catch (Exception e) {
                throw new ConfigurationException("Failed to eagerly instantiate binding " + binding);
            }
        }
    }
}
