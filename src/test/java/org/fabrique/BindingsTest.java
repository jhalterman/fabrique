package org.fabrique;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabrique.AbstractModule;
import org.fabrique.ConfigurationException;
import org.fabrique.ObjectFactory;
import org.fabrique.Binding;
import org.fabrique.Provider;
import org.fabrique.Key;
import org.fabrique.Scopes;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests various bindings.
 */
public class BindingsTest {
  static boolean providerCalled;
  private static final String NAME = "Name";

  /** Test interface */
  interface ITest {
  }

  /** Test class */
  public static class TestClass implements ITest {
  }

  /** Test provider */
  public static class TestProvider implements Provider<ITest> {
    /**
     * {@inheritDoc}
     */
    public ITest get() {
      // Used to track eager singletons
      providerCalled = true;

      return new TestClass();
    }
  }

  /**
   * Performs setup.
   */
  @Before
  public void setup() {
    providerCalled = false;
    TestUtil.resetFactoryBindings();
  }

  /**
   * Verifies that {@link BindingBuilder#as(String)} does not allow null.
   */
  @Test(expected = ConfigurationException.class)
  public void testBindingAsForNull() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).as(null);
      }
    });
  }

  /**
   * Test that a type can be bound as a name to a target.
   */
  @Test
  public void testBindingAsNameTo() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).as(NAME).to(TestClass.class);
      }
    });
    assertBinding(ITest.class, NAME, TestClass.class, null, null);
  }

  /**
   * Test that a type can be bound as a name to a target instance.
   */
  @Test
  public void testBindingAsNameToInstance() {
    final ITest _instance = new TestClass();
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).as(NAME).toInstance(_instance);
      }
    });
    assertBinding(ITest.class, NAME, null, _instance, null);
  }

  /**
   * Test that a type can be bound as a name to a provider.
   */
  @Test
  public void testBindingAsNameToProvider() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).as(NAME).toProvider(TestProvider.class);
      }
    });
    assertBinding(ITest.class, NAME, null, null, null);
  }

  /**
   * Test that a type can be bound as a name to a provider instance.
   */
  @Test
  public void testBindingAsNameToProviderInstance() {
    final TestProvider _instance = new TestProvider();

    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).as(NAME).toProvider(_instance);
      }
    });
    assertBinding(ITest.class, NAME, null, null, _instance);
  }

  /**
   * Test that a type can be bound to a class.
   */
  @Test
  public void testBindingTo() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).to(TestClass.class);
      }
    });
    assertBinding(ITest.class, null, TestClass.class, null, null);
  }

  /**
   * Test that a type can be bound to a class as a singleton.
   */
  @Test
  public void testBindingToAsSingleton() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).to(TestClass.class).asSingleton();
      }
    });
    assertSingleton(ObjectFactory.getBinding(ITest.class));
  }

  /**
   * Verifies that {@link BindingBuilder#to(Class)} does not allow null.
   */
  @Test(expected = ConfigurationException.class)
  public void testBindingToForNull() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).to(null);
      }
    });
  }

  /**
   * Test that a type can be bound to an instance.
   */
  @Test
  public void testBindingToInstance() {
    final ITest _instance = new TestClass();

    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).toInstance(_instance);
      }
    });
    assertBinding(ITest.class, null, null, _instance, null);
  }

  /**
   * Verifies that {@link BindingBuilder#toInstance(Object)} does not allow null.
   */
  @Test(expected = ConfigurationException.class)
  public void testBindingToInstanceForNull() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).toInstance(null);
      }
    });
  }

  /**
   * Test that a type can be bound to a provider.
   */
  @Test
  public void testBindingToProvider() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).toProvider(TestProvider.class);
      }
    });
    assertBinding(ITest.class, null, null, null, null);
  }

  /**
   * Test that a type can be bound to a provider as an eager singleton.
   */
  @Test
  public void testBindingToProviderAsEagerSingleton() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).toProvider(TestProvider.class).asEagerSingleton();
      }
    });
    assertEagerSingleton(ObjectFactory.getBinding(ITest.class));
  }

  /**
   * Test that a type can be bound to a provider as a singleton.
   */
  @Test
  public void testBindingToProviderAsSingleton() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).toProvider(TestProvider.class).asSingleton();
      }
    });
    assertSingleton(ObjectFactory.getBinding(ITest.class));
  }

  /**
   * Verifies that {@link BindingBuilder#toProvider(Class)} does not allow null.
   */
  @SuppressWarnings("unchecked")
  @Test(expected = ConfigurationException.class)
  public void testBindingToProviderForNull() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).toProvider((Class) null);
      }
    });
  }

  /**
   * Test that a type can be bound to a provider instance.
   */
  @Test
  public void testBindingToProviderInstance() {
    final TestProvider _instance = new TestProvider();

    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).toProvider(_instance);
      }
    });

    assertBinding(ITest.class, null, null, null, _instance);
  }

  /**
   * Test that a type can be bound to a provider instance as a singleton.
   */
  @Test
  public void testBindingToProviderInstanceAsSingleton() {
    final TestProvider _instance = new TestProvider();

    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).toProvider(_instance).asSingleton();
      }
    });

    assertSingleton(ObjectFactory.getBinding(ITest.class));
  }

  /**
   * Verifies that {@link BindingBuilder#toProvider(Provider)} does not allow null.
   */
  @SuppressWarnings("unchecked")
  @Test(expected = ConfigurationException.class)
  public void testBindingToProviderInstanceForNull() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(ITest.class).toProvider((Provider) null);
      }
    });
  }

  /**
   * Tests that Factory.getBindings() returns bindings as expected.
   */
  @Test
  public void testGetBindings() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(List.class).to(ArrayList.class);
      }
    });

    Map<Key<?>, Binding<?>> _bindings = ObjectFactory.getBindings();
    assertNotNull(_bindings.get(Key.get(List.class)));

    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Map.class).to(HashMap.class);
      }
    });

    _bindings = ObjectFactory.getBindings();
    assertNotNull(_bindings.get(Key.get(Map.class)));
  }

  /**
   * Provides assertions for binding information.
   */
  @SuppressWarnings("unchecked")
  private void assertBinding(Class<?> pBoundType, String pName, Class pTarget,
      Object pTargetInstance, Provider pProviderInstance) {
    Binding<?> _binding = ObjectFactory.getNamedBinding(pBoundType, pName);

    assertNotNull("Binding was not located in the Binding container", _binding);
    assertSame("Binding key type was not as expected", ITest.class, _binding.getKey().getType());
    assertSame("Binding key name was not as expected", pName, _binding.getKey().getName());

    if (pTarget != null) {
      assertSame("Binding target was not as expected", pTarget,
          ObjectFactory.getInstance(_binding.getKey()).getClass());
    }

    if (pTargetInstance != null) {
      assertSame("Binding target instance was not as expected", pTargetInstance,
          ObjectFactory.getInstance(_binding.getKey()));
    }

    Provider _provider = ObjectFactory.getNamedProvider(pBoundType, pName);
    Object _object = _provider.get();
    assertTrue("Provider.get did not produce an instanceof the bound type.", _binding.getKey()
        .getType().isAssignableFrom(_object.getClass()));

    if (pProviderInstance == null) {
      assertTrue("Provider does not exist for binder", _provider instanceof Provider);
    } else {
      assertEquals("Binding provider was not as expected", pProviderInstance.get().getClass(),
          _provider.get().getClass());
    }
  }

  /**
   * Asserts that {@code pBinding} is scoped as an eager singleton.
   * 
   * @param pBinding Binding to check for eager singleton scope
   */
  private void assertEagerSingleton(Binding<?> pBinding) {
    assertTrue("TestClass should be eagerly instantiated", providerCalled);
    assertTrue("Binding is not in eager singleton scope",
        pBinding.getScope() == Scopes.EAGER_SINGLETON);
    assertTrue("Binding is in singleton scope instead of eager singleton scope",
        pBinding.getScope() != Scopes.SINGLETON);

    Object _instance1 = pBinding.getProvider().get();
    Object _intsance2 = pBinding.getProvider().get();
    assertEquals("Eager singleton scoped binding does not produce identical instances", _instance1,
        _intsance2);
  }

  /**
   * Asserts that {@code pBinding} is scoped as a singleton.
   * 
   * @param pBinding Binding to check for singleton scope
   */
  private void assertSingleton(Binding<?> pBinding) {
    assertTrue("Binding is not in singleton scope", pBinding.getScope() == Scopes.SINGLETON);

    Object _instance1 = pBinding.getProvider().get();
    Object _intsance2 = pBinding.getProvider().get();
    assertEquals("Singleton scoped binding does not produce identical instances", _instance1,
        _intsance2);
  }
}
