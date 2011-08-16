package org.fabrique;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;



/**
 * Tests {@link com.ObjectFactory.common.binder.Factory}.
 *
 * @see {@link BindingsTest} for more specific binding tests.
 */
public class ObjectFactoryTest {
  /** Bad List provider */
  public static class BadListProvider implements Provider<List> {
    /**
     * Throws a runtime exception intended to trigger a provision exception.
     */
    public List get() {
      throw new RuntimeException();
    }
  }

  /** List provider */
  public static class ListProvider implements Provider<List> {
    /**
     * {@inheritDoc}
     */
    public List get() {
      return new ArrayList();
    }
  }

  /**
   * Performs setup.
   */
  @Before
  public void setup() {
    TestUtil.resetFactoryBindings();
  }

  /**
   * Tests {@link ObjectFactory#findBindingsByType(Class)}.
   */
  @Test
  public void testFindBindingsByType() {
    ObjectFactory.loadModules(new AbstractModule() {
        protected void configure() {
          bind(Set.class).to(HashSet.class);
          bind(List.class).to(ArrayList.class);
          bind(Collection.class).to(ArrayList.class);
          bind(List.class).as("THREADSAFE").to(Vector.class);
          bind(List.class).as("SLOW").to(LinkedList.class);
        }
      });

    List<Key> _expectedKeys = new ArrayList<Key>(3);
    _expectedKeys.add(Key.get(List.class));
    _expectedKeys.add(Key.get(List.class, "THREADSAFE"));
    _expectedKeys.add(Key.get(List.class, "SLOW"));

    List<Binding<List>> _bindings = ObjectFactory.findBindingsByType(List.class);

    for (int i = 0; i < _bindings.size(); i++) {
      if (!_bindings.get(i).getKey().equals(_expectedKeys.get(i))) {
        fail();
      }
    }
  }

  /**
   * Verifies that {@link ObjectFactory#findBindingsByType(Class)} does not allow nulls.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testFindBindingsByTypeWithNull() {
    ObjectFactory.findBindingsByType(null);
  }

  /**
   * Tests {@link ObjectFactory#loadBinding(Binding)}.
   */
  @Test
  public void testGetBindings() {
    ObjectFactory.loadModules(new AbstractModule() {
        protected void configure() {
          bind(List.class).to(ArrayList.class);
        }
      });

    Map<Key<?>, Binding<?>> _bindings = ObjectFactory.getBindings();
    int _bindingsSize = _bindings.size();

    ObjectFactory.loadModules(new AbstractModule() {
        protected void configure() {
          bind(List.class).as("THREADSAFE").to(Vector.class);
        }
      });

    assertNotSame("Binding was not added to getBindings map", _bindingsSize, _bindings.size());
  }

  /**
   * Tests {@link ObjectFactory#getInstance(Class)} with a bound class.
   */
  @Test
  public void testGetInstance() {
    ObjectFactory.loadModules(new AbstractModule() {
        protected void configure() {
          bind(List.class).to(ArrayList.class);
        }
      });
    assertNotNull("The factory should have returned a list instance",
      ObjectFactory.getInstance(List.class));
  }
  
  /**
   * Verifies that {@link ObjectFactory#getInstances(Class)} returns a list of instances ordered by the 
   * binding creation order.
   */
  @Test
  public void testGetInstances() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Collection.class).to(ArrayList.class);
        bind(Collection.class).as("1").to(Vector.class);
        bind(Collection.class).as("2").to(Stack.class);
        bind(Collection.class).as("3").to(LinkedList.class);
        bind(Collection.class).as("4").to(HashSet.class);
      }
    });
    
    List<Collection> _instances = ObjectFactory.getInstances(Collection.class);
    assertTrue(_instances.get(0) instanceof ArrayList);
    assertTrue(_instances.get(1) instanceof Vector);
    assertTrue(_instances.get(2) instanceof Stack);
    assertTrue(_instances.get(3) instanceof LinkedList);
    assertTrue(_instances.get(4) instanceof HashSet);
  }
  
  /**
   * Verifies that the Factory throws IllegalArgumentException when attempting to get instances 
   * for null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testGetInstancesForNull() {
    ObjectFactory.getInstances(null);
  }
  
  /**
   * Verifies that the Factory throws ConfigurationException when attempting to get instances 
   * for an unbound type.
   */
  @Test(expected = ConfigurationException.class)
  public void testGetInstancesForUnboundType() {
    ObjectFactory.getInstances(Collection.class);
  }
  
  /**
   * Verifies that {@link ObjectFactory#getInstance(Class)} does not allow null.
   */
  @SuppressWarnings("unchecked")
  @Test(expected = ConfigurationException.class)
  public void testGetInstanceForClassAsNull() {
    ObjectFactory.getInstance((Class) null);
  }

  /**
   * Verifies that a null value given to {@link ObjectFactory#getInstance(Key)} does not allow null.
   */
  @SuppressWarnings("unchecked")
  @Test(expected = ConfigurationException.class)
  public void testGetInstanceForKeyAsNull() {
    ObjectFactory.getInstance((Key) null);
  }

  /**
   * Verifies that {@link ObjectFactory#getInstance(Key)} throws an exception for an unbound key.
   */
  @Test(expected = ConfigurationException.class)
  public void testGetInstanceForUnboundKey() {
    ObjectFactory.getInstance(Key.get(List.class));
  }

  /**
   * Tests {@link ObjectFactory#getInstance(Class)} with a bound provider.
   */
  @Test
  public void testGetInstanceFromProvider() {
    ObjectFactory.loadModules(new AbstractModule() {
        protected void configure() {
          bind(List.class).toProvider(ListProvider.class);
        }
      });
    assertNotNull("The factory should have returned a list instance",
      ObjectFactory.getInstance(List.class));
  }

  /**
   * Tests that getInstance with args throws an exception if the binding has no parameters.
   */
  @Test(expected = ProvisionException.class)
  public void testGetInstanceWithArgsNoParams() {
    ObjectFactory.loadModules(new AbstractModule() {
        protected void configure() {
          bind(List.class).to(ArrayList.class);
        }
      });

    ObjectFactory.getInstance(List.class, 1);
  }

  /**
   * Verifies that {@link ObjectFactory#getInstance(Class, String)} does not allow null.
   */
  @Test(expected = ConfigurationException.class)
  public void testGetNamedInstanceAsNull() {
    ObjectFactory.getNamedInstance(null, "NAME");
  }

  /**
   * Verifies that a null value given to {@link ObjectFactory#getProvider(Class)} does not allow null.
   */
  @SuppressWarnings("unchecked")
  @Test(expected = ConfigurationException.class)
  public void testGetProviderForClassAsNull() {
    ObjectFactory.getProvider((Class) null);
  }

  /**
   * Verifies that a null value given to {@link ObjectFactory#getProvider(Key)} does not allow null.
   */
  @SuppressWarnings("unchecked")
  @Test(expected = ConfigurationException.class)
  public void testGetProviderForKeyAsNull() {
    ObjectFactory.getProvider((Key) null);
  }

  /**
   * Verifies that {@link ObjectFactory#getProvider(Key)} throws an exception for an unbound key.
   */
  @Test(expected = ConfigurationException.class)
  public void testGetProviderForUnboundKey() {
    ObjectFactory.getProvider(Key.get(List.class));
  }

  /**
   * Verifies that {@link ObjectFactory#loadModules(Module...)} does not allow nulls.
   */
  @Test(expected = ConfigurationException.class)
  public void testLoadModulesAsNull() {
    ObjectFactory.loadModules((Module[]) null);
  }

  /**
   * Verifies that a ProvisionException is thrown when an exception occurs during provisioning.
   */
  @Test(expected = ProvisionException.class)
  public void testProviderException() {
    ObjectFactory.loadModules(new AbstractModule() {
        protected void configure() {
          bind(List.class).toProvider(BadListProvider.class);
        }
      });

    ObjectFactory.getInstance(List.class);
  }
}
