# Fabrique 0.1.0

A lightweight configurable factory and dependency injection container with optional AOP support. Initially based on Guice's [Injector API](http://google-guice.googlecode.com/svn/trunk/javadoc/com/google/inject/Injector.html) and [Binder EDSL](http://google-guice.googlecode.com/svn/trunk/javadoc/com/google/inject/Binder.html).

## Motivation

When I first evaluated [Guice](http://code.google.com/p/google-guice) in 2008 I was an instant fan. But there were some key features whose absence was a barrier to adoption for my project. These were:

* The ability to provide explicit construction arguments
* The desire for a single globally accessible injector
* The ability to integrate with closed services

While the last item was [addressed](http://google-guice.googlecode.com/svn/trunk/javadoc/com/google/inject/binder/LinkedBindingBuilder.html#toConstructor%28java.lang.reflect.Constructor%3CS%3E%29) with Guice 2, the former items are inherent to Guice's design and likely to remain. So as a side project I created Fabrique, a lighter weight implementation of Guice's injector API and Binder EDSL geared towards addressing the above items.

### Explicit Construction Arguments

Fabrique allows for explicit arguments to be passed through the ObjectFactory to the underlying constructor or Provider `get` method whose definition matches the given argument types. This allows for arguments that are not known to the dependency injection container to be used for object construction while still providing the benefits of dependency injection.

### Global Accessibility

Fabrique uses a single globally accessible dynamic factory in place of Guice's individual injectors. This design allows for straightforward service resolution and additionally allows for modules to be loaded separately at runtime.

### Closed Service Integration

Integrating with closed services involves the ability to define injection points on constructors that are not free to be annotated with `@Inject`. Fabrique provides this capability, allowing primary and optional injection points to be defined externally via the Binder EDSL.

## Usage

To configure Fabrique for use, Module instances which contain binding definitions are loaded into the ObjectFactory:

    public class ServiceModule extends AbstractModule {
      protected void configure() {
        bind(Service.class).to(ServiceImpl.class);
        bind(AnotherService.class).toInstance(serviceInstance);
      }
    }
    
    ObjectFactory.loadModules(new ServiceModule());
    
Instances can then be retrieved:

    Service service = ObjectFactory.getInstance(Service.class);
    AnotherService anotherService = ObjectFactory.getInstance(AnotherService.class);

### Named Bindings

Different implementations of the same type can be bound to a given name via binding definitions:

    bind(Service.class).as(LAZY).to(LazyServiceImpl.class);
    bind(Service.class).as(TRANSACTIONAL).to(TransactionalServiceImpl.class);

Named instances can then be retrieved:

    Service lazyService = ObjectFactory.getNamedInstance(Service.class, LAZY);
    Service transactionalService = ObjectFactory.getNamedInstance(Service.class, TRANSACTIONAL);

### Providers

Providers allow for separate types to exercise full control over object provisioning:

    bind(String.class).toProvider(new Provider<String>() {
        public void get() { return "abc"; }
    });
    
    assert ObjectFactory.getInstance(String.class).equals("abc");

### Explicit Arguments

Explicit construction arguments can be passed through the ObjectFactory to the corresponding constructor or provider `get` method:

    class StringProvider implements Provider<String> {
        public void get(String s) { return s + "bar"; }
    }

    bind(List.class).to(ArrayList.class);
    bind(String.class).toProvider(StringProvider.class);
    
    assert ObjectFactory.getInstance(List.class, 3).size() == 3;
    assert ObjectFactory.getInstance(String.class, "foo").equals("foobar");

### Closed Service Integration

Injection points for constructors on closed services can be identified by their parameter types:

     bind(DAO.class).to(DAOImpl.class)
        .forParams(UnitOfWork.class, Session.class);
     
Additional optional injection points can be similarly identified:

    bind(Service.class).to(ServiceImpl.class)
        .forParams(Connection.class)
        .forOptionalParams(Session.class, Connection.class)
        .forOptionalParams(UnitOfWork.class, Session.class);

### Scopes

Fabrique can manage the lifecycle of objects based on a configured scope:

    bind(Service.class).to(ServiceImpl.class).in(Scopes.SINGLETON);
    assert ObjectFactory.getInstance(Service.class) == ObjectFactory.getInstance(Service.class);

## Design Notes

While the desire to provide a single globally accessible injector API, such as was initially used by [StructureMap](http://structuremap.sourceforge.net), enhances usability for some use cases, it is clearly not suitable for use cases where multiple injectors are beneficial. The single injector approach poses a challenge for long running test sessions, where bindings loaded into the injector from one test method may interfere with those for other methods. This requires resetting the injector prior to invoking a test method.

## License

Fabrique is released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).