package org.fabrique.internal;

import java.lang.annotation.Annotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fabrique.BindingAnnotation;
import org.fabrique.ConfigurationException;
import org.fabrique.Inject;
import org.fabrique.InjectionException;
import org.fabrique.Key;
import org.fabrique.Named;
import org.fabrique.Primitives.Primitive;
import org.fabrique.internal.InternalFactory.FactoryType;

/**
 * Utility methods for creating and working with injectors.
 */
public class Injectors {
    private static final Key<?>[] NO_DEPENDENCIES = new Key<?>[0];
    private static final Map<Class<?>, List<MemberInjector>> memberInjectors = new HashMap<Class<?>, List<MemberInjector>>();
    private static final Map<Class<?>, List<ConstructionInjector<?>>> constructorInjectors = new HashMap<Class<?>, List<ConstructionInjector<?>>>();
    private static final Map<Class<?>, List<ConstructionInjector<?>>> providerMethodInjectors = new HashMap<Class<?>, List<ConstructionInjector<?>>>();
    private static String PROVIDER_GET_METHOD_NAME = "get";

    /**
     * Produces construction injectors for {@code M}.
     * 
     * @param M member type
     */
    private interface ConstructionFactory<M extends Member> {
        /**
         * Produces constructor injectors for {@code type} and {@code params}.
         */
        ConstructionFactory<Constructor<?>> CONSTRUCTORS = new ConstructionFactory<Constructor<?>>() {
            /**
             * Gets a constructor injector for {@code type} and {@code params}.
             * 
             * @param type Type to obtain injector for
             * @param params Constructor params
             * @return ConstructionInjector<T>
             */
            public <T> ConstructionInjector<T> injectorFor(Class<T> type, Class<?>[] params) {
                try {
                    Constructor<T> constructor = type.getDeclaredConstructor(params);
                    return new ConstructorInjectorImpl<T>(constructor, dependenciesFor(constructor));
                } catch (NoSuchMethodException e) {
                    throw new ConfigurationException(Errors.noConstructor(type, params), e);
                }
            }

            /**
             * Gets the default constructor injector for {@code type}.
             * 
             * @param type Type to obtain injector for
             * @return ConstructionInjector<T>
             */
            @SuppressWarnings("unchecked")
            public <T> ConstructionInjector<T> defaultInjectorFor(Class<T> type) {
                Constructor<?>[] constructors = type.getDeclaredConstructors();
                Constructor<T> defaultConstructor = null;
                Constructor<T> noArgConstructor = null;

                for (int i = 0; i < constructors.length; i++) {
                    Constructor<T> constructor = (Constructor<T>) constructors[i];
                    Inject inject = constructor.getAnnotation(Inject.class);

                    if (inject != null && !inject.optional()) {
                        if (defaultConstructor != null)
                            throw new ConfigurationException(
                                    "Multiple non-optional constructors in " + type);
                        defaultConstructor = constructor;
                    } else if (constructor.getParameterTypes().length == 0)
                        noArgConstructor = constructor;
                }

                if (defaultConstructor != null)
                    return new ConstructorInjectorImpl<T>(defaultConstructor,
                            dependenciesFor(defaultConstructor));
                else if (noArgConstructor != null)
                    return new ConstructorInjectorImpl<T>(noArgConstructor, NO_DEPENDENCIES);
                return null;
            }

            /**
             * Gets the optional constructor injectors for {@code type}.
             * 
             * @param type Type to obtain injectors for
             * @return ConstructionInjector<T>
             */
            @SuppressWarnings("unchecked")
            public <T> List<ConstructionInjector<?>> optionalInjectorsFor(Class<T> type) {
                Constructor<?>[] constructors = type.getDeclaredConstructors();
                List<ConstructionInjector<?>> injectors = new ArrayList<ConstructionInjector<?>>(
                        constructors.length);

                for (int i = 0; i < constructors.length; i++) {
                    Constructor<T> constructor = (Constructor<T>) constructors[i];
                    Inject inject = constructor.getAnnotation(Inject.class);
                    if (inject != null && inject.optional())
                        injectors.add(new ConstructorInjectorImpl<T>(constructor,
                                dependenciesFor(constructor)));

                }

                return injectors;
            }

            /**
             * Gets cached injectors for {@code type}.
             * 
             * @param type Injector type
             * @return List<ConstructionInjector<?>>
             */
            public List<ConstructionInjector<?>> cachedInjectorsFor(Class<?> type) {
                return constructorInjectors.get(type);
            }

            /**
             * Caches {@code injectors} for {@code type}.
             * 
             * @param type Type
             * @param injectors Injectors
             */
            public void cacheInjectors(Class<?> type, List<ConstructionInjector<?>> injectors) {
                constructorInjectors.put(type, injectors);
            }
        };

        /**
         * Produces provider method injectors for {@code type} and {@code params}.
         */
        ConstructionFactory<Method> PROVIDER_METHODS = new ConstructionFactory<Method>() {
            /**
             * Gets a provider method injector for {@code type} and {@code params}.
             * 
             * @param type Type to obtain injector for
             * @param params Provider method params
             * @return ConstructionInjector<T>
             */
            public <T> ConstructionInjector<T> injectorFor(Class<T> type, Class<?>[] params) {
                try {
                    Method method = type.getDeclaredMethod(PROVIDER_GET_METHOD_NAME, params);
                    if (method.getAnnotation(Inject.class) != null)
                        throw new ConfigurationException(Errors.injectProviderGet(method));
                    return new ProviderMethodInjector<T>(method, dependenciesFor(method));
                } catch (NoSuchMethodException e) {
                    throw new ConfigurationException(Errors.noProviderMethod(type, params), e);
                }
            }

            /**
             * Gets the default provider method injector for {@code type}.
             * 
             * @param type Type to obtain injector for
             * @return ConstructionInjector<T>
             */
            public <T> ConstructionInjector<T> defaultInjectorFor(Class<T> type) {
                Method[] methods = type.getDeclaredMethods();
                Method defaultMethod = null;

                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    if (method.getName().equals(PROVIDER_GET_METHOD_NAME)) {
                        if (method.getAnnotation(Inject.class) != null)
                            throw new ConfigurationException(Errors.injectProviderGet(method));
                        if (method.getParameterTypes().length == 0)
                            defaultMethod = method;
                    }
                }

                if (defaultMethod != null) {
                    return new ProviderMethodInjector<T>(defaultMethod, NO_DEPENDENCIES);
                }

                throw new ConfigurationException(Errors.noProviderMethod(type, null));
            }

            /**
             * Gets the optional provider method injectors for {@code type}.
             * 
             * @param type Type to obtain injectors for
             * @return ConstructionInjector<T>
             */
            public <T> List<ConstructionInjector<?>> optionalInjectorsFor(Class<T> type) {
                Method[] methods = type.getDeclaredMethods();
                List<ConstructionInjector<?>> injectors = new ArrayList<ConstructionInjector<?>>(
                        methods.length);

                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];

                    if (method.getName().equals(PROVIDER_GET_METHOD_NAME)
                            && (method.getParameterTypes().length > 0)) {
                        injectors
                                .add(new ProviderMethodInjector<T>(method, dependenciesFor(method)));
                    }
                }

                return injectors;
            }

            /**
             * Gets cached injectors for {@code type}.
             * 
             * @param type Injector type
             * @return List<ConstructionInjector<?>>
             */
            public List<ConstructionInjector<?>> cachedInjectorsFor(Class<?> type) {
                return providerMethodInjectors.get(type);
            }

            /**
             * Caches {@code injectors} for {@code type}.
             * 
             * @param type Type
             * @param injectors Injectors
             */
            public void cacheInjectors(Class<?> type, List<ConstructionInjector<?>> injectors) {
                providerMethodInjectors.put(type, injectors);
            }
        };

        void cacheInjectors(Class<?> type, List<ConstructionInjector<?>> injectors);

        List<ConstructionInjector<?>> cachedInjectorsFor(Class<?> type);

        <T> ConstructionInjector<T> defaultInjectorFor(Class<T> type);

        <T> ConstructionInjector<T> injectorFor(Class<T> type, Class<?>[] params);

        <T> List<ConstructionInjector<?>> optionalInjectorsFor(Class<T> type);
    }

    /**
     * Produces members and member injectors.
     * 
     * @param M Member type
     */
    private interface MemberFactory<M extends Member> {
        MemberFactory<Field> FIELDS = new MemberFactory<Field>() {
            public Field[] membersFor(Class<?> type) {
                return type.getDeclaredFields();
            }

            public MemberInjector injectorFor(Field member, boolean optional) {
                return new FieldInjector(member, dependencyFor(member), optional);
            }
        };

        MemberFactory<Method> METHODS = new MemberFactory<Method>() {
            public Method[] membersFor(Class<?> type) {
                return type.getDeclaredMethods();
            }

            public MemberInjector injectorFor(Method member, boolean optional) {
                return new MethodInjector(member, dependenciesFor(member), optional);
            }
        };

        /**
         * Creates a member injector for {@code member}.
         * 
         * @param member Member to create injector
         * @param optional Whether the member is optional
         * @return IMemberInjector
         */
        MemberInjector injectorFor(M member, boolean optional);

        /**
         * Gets members for {@code type}.
         * 
         * @param type Type to retrieve members for
         * @return M[]
         */
        M[] membersFor(Class<?> type);
    }

    /**
     * Returns a list of {@link ConstructionInjector} instances for
     * 
     * @param <T> Constructed type
     * @param <M> Member type
     * @param type Type to retrieve injectors for
     * @param pConstructionFactory Construction injector factory
     * @param defaultParams Default construction params
     * @param optionalParams Optional construction params
     * @return List<ConstructionInjector<?>>
     */
    static <T, M extends Member> List<ConstructionInjector<?>> constructionInjectorsFor(
            Class<T> type, FactoryType factoryType, Class<?>[] defaultParams,
            Class<?>[][] optionalParams) {
        ConstructionFactory<?> constructionFactory = factoryType.equals(FactoryType.Target) ? ConstructionFactory.CONSTRUCTORS
                : ConstructionFactory.PROVIDER_METHODS;
        ConstructionInjector<?> defaultInjector = null;
        List<ConstructionInjector<?>> injectors = null;

        if (defaultParams == null && optionalParams == null) {
            injectors = constructionFactory.cachedInjectorsFor(type);
            if (injectors != null)
                return injectors;
        }

        defaultInjector = defaultParams == null ? constructionFactory.defaultInjectorFor(type)
                : constructionFactory.injectorFor(type, defaultParams);
        if (defaultInjector == null)
            throw new ConfigurationException(Errors.noConstructor(type, null));

        if (optionalParams == null) {
            injectors = constructionFactory.optionalInjectorsFor(type);
            if (factoryType.equals(FactoryType.Provider))
                injectors.remove(defaultInjector);
        } else
            injectors = optionalInjectorsFor(type, constructionFactory, optionalParams);

        injectors.add(0, defaultInjector);
        if (defaultParams == null && optionalParams == null)
            constructionFactory.cacheInjectors(type, injectors);

        return injectors;
    }

    /**
     * Gets the construction injector from the given {@code injectors} for {@code args}. Supports
     * ambiguous argument matching where one or more {@code args} may be null.
     * 
     * @param injectors Injectors to match against
     * @param args Args to get params index for
     * @return ConstructionInjector<T>
     * @throws InjectionException If any {@code args} is null
     */
    @SuppressWarnings("unchecked")
    static <T> ConstructionInjector<T> injectorFor(Class<?> type,
            List<ConstructionInjector<?>> injectors, Object[] args) {
        if (args == null)
            return (ConstructionInjector<T>) injectors.get(0);

        Class<?>[] params = new Class<?>[args.length];
        int i = 0;
        boolean matchAmbiguous = false;

        for (; i < args.length; i++)
            if (args[i] == null)
                matchAmbiguous = true;
            else if (args[i] instanceof Primitive)
                params[i] = ((Primitive) args[i]).getType();
            else
                params[i] = args[i].getClass();

        ConstructionInjector<T> result = null;
        int j = 0;
        int matches = 0;

        for (i = 0; i < injectors.size(); i++) {
            ConstructionInjector<T> injector = (ConstructionInjector<T>) injectors.get(i);
            Key<?>[] dependencies = injector.getDependencies();

            if (dependencies.length == params.length) {
                for (j = 0; j < dependencies.length; j++)
                    if (params[j] != null && !dependencies[j].getType().equals(params[j])
                            && !dependencies[j].getType().isAssignableFrom(params[j]))
                        break;

                if (j == dependencies.length) {
                    if (matchAmbiguous) {
                        matches++;
                        if (matches > 1)
                            throw new InjectionException(Errors.ambiguousArgs(type));
                        result = injector;
                    } else
                        return injector;

                }
            }
        }

        return result;
    }

    /**
     * Builds a list of {@link MemberInjector} objects for {@code type} and all super types. Field
     * injectors are first, followed by method injectors, with injectors for super types coming
     * first. This allows member injection to take place in a specific order later on.
     * 
     * <p>
     * Note: Cached results are not defensively copied, and should not be modified.
     * 
     * @param type Type to obtain injectors for.
     * @return List<IMemberInjector>
     */
    static List<MemberInjector> memberInjectorsFor(Class<?> type) {
        List<MemberInjector> injectors = memberInjectors.get(type);

        if (injectors == null) {
            injectors = new ArrayList<MemberInjector>();
            addMemberInjectors(type, MemberFactory.FIELDS, injectors);
            addMemberInjectors(type, MemberFactory.METHODS, injectors);
            memberInjectors.put(type, injectors);
        }

        return injectors;
    }

    /**
     * Recursively builds a list of {@link MemberInjector} objects for {@code type} and all super
     * types.
     * 
     * @param type Type to obtain injectors for
     * @param memberFactory Produces member injectors for {@code type}
     * @param injectors Storage for produced injectors
     */
    private static <M extends Member> void addMemberInjectors(Class<?> type,
            MemberFactory<M> memberFactory, Collection<MemberInjector> injectors) {
        Class<?> superType = type.getSuperclass();
        if (superType != Object.class)
            addMemberInjectors(superType, memberFactory, injectors);

        for (M member : memberFactory.membersFor(type)) {
            /** Skip static members */
            Modifier.isStatic(member.getModifiers());
            Inject inject = ((AnnotatedElement) member).getAnnotation(Inject.class);
            if (inject != null) {
                try {
                    injectors.add(memberFactory.injectorFor(member, inject.optional()));
                } catch (ConfigurationException e) {
                    /** Handle missing binding */
                    if (!inject.optional())
                        throw new ConfigurationException(
                                "No binding exists for required dependency " + type, e);
                }
            }
        }
    }

    /**
     * Gets the binding annotation type for {@code} searching {@code annotations}. If the annotation
     * is {@code @Named}, then the value String is returned.
     * 
     * @param member Member to get annotation for
     * @param annotations Annotations to search
     * @return Object
     * @throws ConfigurationException If duplicate binding annotations exist for {@code member}
     */
    private static Object bindingAnnotationsFor(Member member, Annotation[] annotations) {
        Annotation bindingAnnotation = null;

        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(BindingAnnotation.class)) {
                if (bindingAnnotation == null)
                    bindingAnnotation = annotation;
                else
                    throw new ConfigurationException("Duplicate binding annotations for " + member);
            }
        }

        return (bindingAnnotation instanceof Named) ? ((Named) bindingAnnotation).value()
                : ((bindingAnnotation == null) ? null : bindingAnnotation.annotationType());
    }

    /**
     * Obtains non-primitive dependenciy keys for {@code member}.
     * 
     * @param member Member to retrieve dependencies for
     * @return Key<?>[]
     */
    private static Key<?>[] dependenciesFor(Member member) {
        Class<?>[] parameters = null;
        Annotation[][] parameterAnnotations = null;

        if (member instanceof Constructor) {
            parameters = ((Constructor<?>) member).getParameterTypes();
            parameterAnnotations = ((Constructor<?>) member).getParameterAnnotations();
        } else if (member instanceof Method) {
            parameters = ((Method) member).getParameterTypes();
            parameterAnnotations = ((Method) member).getParameterAnnotations();
        }

        if (parameters.length == 0)
            return NO_DEPENDENCIES;

        Key<?>[] dependencies = new Key[parameters.length];
        int i = 0;
        Iterator<Annotation[]> annotationsIterator = Arrays.asList(parameterAnnotations).iterator();

        for (Class<?> parameter : parameters) {
            Annotation[] parameterAnnotation = annotationsIterator.next();
            dependencies[i++] = Key.get(parameter,
                    bindingAnnotationsFor(member, parameterAnnotation));
        }

        return dependencies;
    }

    /**
     * Obtains a non-primitive dependency key for {@code field}.
     * 
     * @param field Field
     * @return Key<?>
     */
    private static Key<?> dependencyFor(Field field) {
        return Key.get(field.getType(), bindingAnnotationsFor(field, field.getAnnotations()));
    }

    /**
     * Obtains explicit override optional injectors for {@code type} and {@code params}.
     * 
     * @param <T> Injector type
     * @param type Type to obtain injectors for
     * @param params Constructor params
     * @return List<ConstructionInjector<?>>
     * @throws ConfigurationException If specific constructor does not exist
     */
    private static <T, M extends Member> List<ConstructionInjector<?>> optionalInjectorsFor(
            Class<T> type, ConstructionFactory<M> constructionFactory, Class<?>[][] params) {
        List<ConstructionInjector<?>> injectors = new ArrayList<ConstructionInjector<?>>(
                params.length + 1);

        for (Class<?>[] paramsSet : params)
            injectors.add(constructionFactory.injectorFor(type, paramsSet));
        return injectors;
    }
}
