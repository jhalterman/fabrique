package org.fabrique;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.fabrique.AbstractModule;
import org.fabrique.ObjectFactory;
import org.fabrique.Provider;
import org.fabrique.ProvisionException;
import org.fabrique.Scopes;
import org.fabrique.BindingParamsTest.ObjectProvider;
import org.fabrique.Scopes.SimpleScope;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the usage of various providers.
 */
public class ProviderTest {
  /** Provides strings */
  public static class StringProvider implements Provider<String> {
    /**
     * {@inheritDoc}
     */
    public String get() {
      return "abc";
    }

    /**
     * {@inheritDoc}
     */
    public String get(String pValue) {
      return pValue;
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
   * Tests that getProvider where a binding parameter is the same as the bound type results in a
   * provision exception if arguments are not explicitely provided.
   */
  @Test(expected = ProvisionException.class)
  public void testBindingWithSelfParams() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Collection.class).to(ArrayList.class).forParams(Collection.class);
      }
    });

    ObjectFactory.getProvider(Collection.class).get();
  }

  /**
   * Tests that the provider for some object works as expected.
   */
  @Test
  public void testProvider() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(String.class).toProvider(StringProvider.class).forParams(String.class);
      }
    });

    String _testString = "test";
    assertEquals(_testString, ObjectFactory.getProvider(String.class, _testString).get());
  }

  /**
   * Tests that a scoped provider binding with some parameters will produce objects as expected.
   */
  @Test
  public void testProviderBindingScoped() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).toProvider(ObjectProvider.class).forParams(String.class)
            .in(Scopes.SIMPLE);
      }
    });

    String _testString1 = "test1";
    String _testString2 = "test2";

    assertEquals("Objects in same scope should be equal regardless of arguments", ObjectFactory
        .getProvider(Object.class, _testString1).get(),
        ObjectFactory.getProvider(Object.class, _testString2).get());
    SimpleScope.reset();

    assertEquals("Object should match args since scope was reset", _testString2, ObjectFactory
        .getProvider(Object.class, _testString2).get());
  }

  /**
   * Tests getInstance on a provider binding with arguments explicitly provided.
   */
  @Test
  public void testProviderBindingWithArgs() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).toProvider(ObjectProvider.class).forParams(String.class);
      }
    });

    String _testString = "test";
    assertEquals(ObjectFactory.getProvider(Object.class, _testString).get(), _testString);
  }

  /**
   * Tests that getInstance for a provider binding with an overloaded method can be fulfilled by the
   * factory.
   */
  @Test
  public void testProviderBindingWithoutArgsFulfilled() {
    final String _testString = "test";

    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).toProvider(ObjectProvider.class).forParams(String.class);
        bind(String.class).toInstance(_testString);
      }
    });

    assertEquals(_testString, ObjectFactory.getProvider(Object.class).get());
  }

  /**
   * Tests that the provider instance for some object works as expected.
   */
  @Test
  public void testProviderInstance() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(String.class).toProvider(new StringProvider()).forParams(String.class);
      }
    });

    String _testString = "test";
    assertEquals(_testString, ObjectFactory.getProvider(String.class, _testString).get());
  }

  /**
   * Tests that a scoped provider instance binding with some parameters will produce objects as
   * expected.
   */
  @Test
  public void testProviderInstanceBindingScoped() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).toProvider(new ObjectProvider()).forParams(String.class)
            .in(Scopes.SIMPLE);
      }
    });

    String _testString1 = "test1";
    String _testString2 = "test2";

    assertEquals("Objects in same scope should be equal regardless of arguments", ObjectFactory
        .getProvider(Object.class, _testString1).get(),
        ObjectFactory.getProvider(Object.class, _testString2).get());
    SimpleScope.reset();

    assertEquals("Object should match args since scope was reset", _testString2, ObjectFactory
        .getProvider(Object.class, _testString2).get());
  }

  /**
   * Tests getInstance on a provider binding with arguments explicitly provided.
   */
  @Test
  public void testProviderInstanceBindingWithArgs() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).toProvider(new ObjectProvider()).forParams(String.class);
      }
    });

    String _testString = "test";
    assertEquals(ObjectFactory.getProvider(Object.class, _testString).get(), _testString);
  }

  /**
   * Tests that getInstance for a provider instance binding with an overloaded method can be
   * fulfilled by the factory.
   */
  @Test
  public void testProviderInstanceBindingWithoutArgsFulfilled() {
    final String _testString = "test";

    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).toProvider(new ObjectProvider()).forParams(String.class);
        bind(String.class).toInstance(_testString);
      }
    });

    assertEquals(_testString, ObjectFactory.getProvider(Object.class).get());
  }
}
