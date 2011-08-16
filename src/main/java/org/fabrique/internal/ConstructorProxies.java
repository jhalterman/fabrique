package org.fabrique.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.core.DefaultNamingPolicy;
import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastClass.Generator;
import net.sf.cglib.reflect.FastConstructor;

import org.fabrique.intercept.IMethodInterceptor;

/**
 * Produces construction proxy instances.
 */
public class ConstructorProxies {
    static final NamingPolicy NAMING_POLICY = new DefaultNamingPolicy() {
        @Override
        protected String getTag() {
            return "ByFabrique";
        }
    };

    /** No-op interceptor */
    private static final MethodInterceptor NO_OP_METHOD_INTERCEPTOR = new MethodInterceptor() {
        public Object intercept(Object proxy, Method method, Object[] arguments,
                MethodProxy methodProxy) throws Throwable {
            return methodProxy.invokeSuper(proxy, arguments);
        }
    };

    /**
     * Default constructor implementation. Uses reflection to instantiate objects.
     */
    private static class DefaultConstructor<T> implements ConstructorProxy<T> {
        private Constructor<T> constructor;

        /**
         * Creates a new DefaultConstructor object.
         * 
         * @param constructor .
         */
        DefaultConstructor(Constructor<T> constructor) {
            this.constructor = constructor;
        }

        /**
         * {@inheritDoc}
         */
        public T newInstance(Object... args) throws InvocationTargetException {
            try {
                return constructor.newInstance(args);
            } catch (InstantiationException e) {
                throw new AssertionError(e);
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }
    }

    /**
     * A callback filter that maps methods to unique IDs. We define equals and hashCode using the
     * declaring class so that enhanced classes can be shared between injectors.
     */
    private static class IndicesCallbackFilter implements CallbackFilter {
        final Class<?> declaringClass;
        final Map<Method, Integer> indices;

        /**
         * Creates a new IndicesCallbackFilter object.
         * 
         * @param declaringClass Declaring class
         * @param methods Methods to filter
         */
        IndicesCallbackFilter(Class<?> declaringClass, List<Method> methods) {
            this.declaringClass = declaringClass;
            indices = new HashMap<Method, Integer>();

            for (int i = 0; i < methods.size(); i++) {
                Method _method = methods.get(i);
                indices.put(_method, i);
            }
        }

        /**
         * {@inheritDoc}
         */
        public int accept(Method method) {
            int index = indices.get(method);
            return index;
        }

        /**
         * {@inheritDoc}
         * 
         * Necessary for CGLIB caching.
         */
        @Override
        public boolean equals(Object object) {
            return object instanceof IndicesCallbackFilter
                    && (((IndicesCallbackFilter) object).declaringClass == declaringClass);
        }

        /**
         * {@inheritDoc}
         * 
         * Necessary for CGLIB caching.
         */
        @Override
        public int hashCode() {
            return declaringClass.hashCode();
        }
    }

    /**
     * Represents Invocation handlers for a specific method.
     */
    private static class InvocationHandlers {
        private List<IMethodInterceptor> interceptors;

        /**
         * Adds interceptors to the pair.
         * 
         * @param interceptors Interceptors to add
         */
        void addInterceptors(List<IMethodInterceptor> interceptors) {
            if (this.interceptors == null)
                this.interceptors = new ArrayList<IMethodInterceptor>();
            this.interceptors.addAll(interceptors);
        }

        /**
         * True if pair has handlers.
         */
        boolean hasHandlers() {
            return interceptors != null;
        }
    }

    /**
     * Proxy constructor implementation, capable of producing CGLIB proxied objects.
     */
    private static class ProxyConstructor<T> implements ConstructorProxy<T> {
        final Class<?> enhanced;
        final FastConstructor fastConstructor;
        final Callback[] callbacks;

        /**
         * Creates a new ProxyConstructor object.
         * 
         * @param constructor Reflect constructor
         * @param enhancer CGLIB enhancer
         * @param callbacks Callbacks for interceptors
         */
        ProxyConstructor(Enhancer enhancer, Constructor<T> constructor, Callback[] callbacks) {
            enhanced = enhancer.createClass();
            this.callbacks = callbacks;

            Generator generator = new Generator();
            generator.setType(enhanced);
            generator.setNamingPolicy(NAMING_POLICY);

            FastClass fastClass = generator.create();
            fastConstructor = fastClass.getConstructor(constructor == null ? null : constructor
                    .getParameterTypes());
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        public T newInstance(Object... arguments) throws InvocationTargetException {
            Enhancer.registerCallbacks(enhanced, callbacks);

            try {
                return (T) fastConstructor.newInstance(arguments);
            } finally {
                Enhancer.registerCallbacks(enhanced, null);
            }
        }
    }

    /**
     * Produces a constructor proxy for {@code constructor}.
     * 
     * @param <T> Type to produce
     * @param constructor Constructor to proxy
     * @return IConstructorProxy
     */
    static <T> ConstructorProxy<T> proxyFor(Constructor<T> constructor) {
        return ConstructorProxies.<T>proxyFor(constructor.getDeclaringClass(), constructor);
    }

    /**
     * Creates a construction proxy for {@code type}.
     * 
     * @param <T> Type to construct
     * @param type Type
     * @return IConstructionProxy for T
     */
    static <T> ConstructorProxy<T> proxyFor(Class<T> type) {
        return ConstructorProxies.<T>proxyFor(type, null);
    }

    /**
     * Creates a construction proxy for {@code type} and {@code pConstructor}.
     * 
     * @param <T> Type to construct
     * @param type Type
     * @param pConstructor Constructor
     * @return IConstructionProxy for T
     */
    private static <T> ConstructorProxy<T> proxyFor(Class<T> type, Constructor<T> pConstructor) {
        List<MethodAspect> applicableAspects = Aspects.matchesFor(type);

        if (applicableAspects.isEmpty())
            return new DefaultConstructor<T>(pConstructor);

        List<Method> methods = new ArrayList<Method>();
        Enhancer.getMethods(type, null, methods);

        Map<Method, InvocationHandlers> methodHandlers = new LinkedHashMap<Method, InvocationHandlers>();

        // Create method interceptor pairs
        for (Method method : methods)
            methodHandlers.put(method, new InvocationHandlers());

        boolean methodMatched = false;

        // Match method aspects against methods
        for (MethodAspect methodAspect : applicableAspects) {
            for (Map.Entry<Method, InvocationHandlers> entry : methodHandlers.entrySet()) {
                if (methodAspect.matches(entry.getKey())) {
                    entry.getValue().addInterceptors(methodAspect.interceptors());
                    methodMatched = true;
                }
            }
        }

        if (!methodMatched)
            return new DefaultConstructor<T>(pConstructor);

        Callback[] callbacks = new Callback[methods.size()];
        int i = -1;

        // Build the callbacks
        for (Map.Entry<Method, InvocationHandlers> entry : methodHandlers.entrySet()) {
            i++;

            InvocationHandlers handlers = entry.getValue();

            if (!handlers.hasHandlers()) {
                callbacks[i] = NO_OP_METHOD_INTERCEPTOR;
                continue;
            }

            callbacks[i] = new InvocationStack(entry.getKey(), handlers.interceptors);
        }

        @SuppressWarnings("unchecked")
        Class<? extends Callback>[] callbackTypes = new Class[methods.size()];
        Arrays.fill(callbackTypes, MethodInterceptor.class);

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(type);
        enhancer.setUseFactory(false);
        enhancer.setNamingPolicy(NAMING_POLICY);
        enhancer.setCallbackFilter(new IndicesCallbackFilter(type, methods));
        enhancer.setCallbackTypes(callbackTypes);
        return new ProxyConstructor<T>(enhancer, pConstructor, callbacks);
    }
}
