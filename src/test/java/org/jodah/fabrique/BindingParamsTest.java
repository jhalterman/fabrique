package org.jodah.fabrique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.jodah.fabrique.AbstractModule;
import org.jodah.fabrique.ConfigurationException;
import org.jodah.fabrique.Inject;
import org.jodah.fabrique.ObjectFactory;
import org.jodah.fabrique.Primitives;
import org.jodah.fabrique.Provider;
import org.jodah.fabrique.ProvisionException;
import org.jodah.fabrique.Scopes;
import org.jodah.fabrique.Scopes.SimpleScope;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the creation of various unnamed bindings with parameters.
 */
public class BindingParamsTest {
  /** Intelligent parameter matching test provider */
  public static class IntelligentStringProvider implements Provider<String> {
    /**
     * {@inheritDoc}
     */
    public String get() {
      return "test";
    }

    /** Creates a test string. */
    public String get(String pString) {
      return pString;
    }

    /** Creates a test string. */
    public String get(Integer pInteger) {
      return pInteger.toString();
    }

    /** Creates a test string. */
    public String get(String pString1, String pString2) {
      return pString1 + pString2;
    }

    /** Creates a test string. */
    public String get(String pString, Integer pInteger) {
      return pString + pInteger.toString();
    }
  }

  /** Intelligent parameter matching test class */
  public static class IntelligentTest {
    String string1 = "string1";
    String string2 = "string2";

    /** Creates a new IntelligentTest object. */
    @Inject
    public IntelligentTest() {
      string1 = "test";
    }

    /** Creates a new IntelligentTest object. */
    @Inject(optional = true)
    public IntelligentTest(String pString) {
      string1 = pString;
    }

    /** Creates a new IntelligentTest object. */
    @Inject(optional = true)
    public IntelligentTest(Integer pInteger) {
      string1 = pInteger.toString();
    }

    /** Creates a new IntelligentTest object. */
    @Inject(optional = true)
    public IntelligentTest(String pString1, String pString2) {
      string1 = pString1;
      string2 = pString2;
    }

    /** Creates a new IntelligentTest object. */
    @Inject(optional = true)
    public IntelligentTest(String pString, Integer pInteger) {
      string1 = pString;
      string2 = pInteger.toString();
    }
  }

  /** Object provider */
  public static class ObjectProvider implements Provider<Object> {
    /**
     * {@inheritDoc}
     */
    public Object get() {
      return "test";
    }

    /**
     * @see ObjectProvider#get()
     */
    public Object get(String pValue) {
      return new String(pValue);
    }

    /**
     * @see ObjectProvider#get()
     */
    public Object get(String pValue1, String pValue2) {
      return new String(pValue1 + pValue2);
    }
  }

  /** Collection class */
  @SuppressWarnings("serial")
  public static class TestCollectionClass<T> extends ArrayList<T> {
    /** Construct TestCollectionClass */
    public TestCollectionClass() {
    }

    /** Construct TestCollectionClass */
    public TestCollectionClass(List<T> pList) {
      super(pList);
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
   * Tests getInstance on a provider binding with explicit arguments partially provided.
   */
  @Test(expected = ProvisionException.class)
  public void testBindingWithPartialArgs() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).toProvider(ObjectProvider.class).forParams(String.class, String.class)
            .forOptionalParams(NO_PARAMS);
      }
    });

    ObjectFactory.getInstance(Object.class, "test");
  }

  /**
   * Tests that getInstance where a binding parameter is the same as the bound type results in a
   * provision exception if arguments are not explicitely provided.
   */
  @Test(expected = ProvisionException.class)
  public void testBindingWithSelfParams() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Collection.class).to(ArrayList.class).forParams(Collection.class);
      }
    });

    ObjectFactory.getInstance(Collection.class);
  }

  /**
   * Tests that getInstance where binding parameters refer to each other should result in a
   * provision exception if arguments are not explicitely provided.
   */
  @Test(expected = ProvisionException.class)
  public void testBindingsWithCircularParams() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Collection.class).to(TestCollectionClass.class).forParams(List.class);
        bind(List.class).to(ArrayList.class).forParams(Collection.class);
      }
    });

    ObjectFactory.getInstance(Collection.class);
  }

  /**
   * Tests that overriding the default provider get method prevents the method from being called.
   */
  @Test(expected = ProvisionException.class)
  public void testDefaultProviderParamsFail() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).toProvider(ObjectProvider.class).forParams(String.class);
      }
    });

    ObjectFactory.getDefaultInstance(Object.class);
  }

  /**
   * Tests that overriding the default provider get method requires that the method be setup as
   * optional in order to be callable.
   */
  @Test
  public void testDefaultProviderParamsSuccess() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).toProvider(ObjectProvider.class).forParams(String.class)
            .forOptionalParams(NO_PARAMS);
      }
    });

    ObjectFactory.getDefaultInstance(Object.class);
  }

  /**
   * Tests that overriding the default constructor for an object prevents the default constructor
   * from being called.
   */
  @Test(expected = ProvisionException.class)
  public void testDefaultTargetParamsFail() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).to(String.class).forParams(String.class);
      }
    });

    ObjectFactory.getDefaultInstance(Object.class);
  }

  /**
   * Tests that overriding the default provider get method requires that the method be setup as
   * optional in order to be callable.
   */
  @Test
  public void testDefaultTargetParamsSuccess() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).to(String.class).forParams(String.class).forOptionalParams(NO_PARAMS);
      }
    });

    ObjectFactory.getDefaultInstance(Object.class);
  }

  /**
   * Tests that duplicate parameters are not allowed.
   */
  @Test(expected = ConfigurationException.class)
  public void testDuplicateParams1() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).to(String.class).forParams(String.class).forOptionalParams(String.class);
      }
    });
  }

  /**
   * Tests that duplicate parameters are not allowed.
   */
  @Test(expected = ConfigurationException.class)
  public void testDuplicateParams2() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).to(String.class).forOptionalParams(String.class)
            .forOptionalParams(String.class);
      }
    });
  }

  /**
   * Tests that duplicate parameters are not allowed.
   */
  @Test(expected = ConfigurationException.class)
  public void testDuplicateParams3() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class)
            .to(String.class)
            .forParams(String.class)
            .forOptionalParams(
                new Class[][] { { StringBuffer.class }, { StringBuilder.class },
                    { StringBuffer.class } });
      }
    });
  }

  /**
   * Tests that the Factory throws an exception when an ambiguous provider get argument is passed.
   */
  @Test(expected = ProvisionException.class)
  public void testIntelligentProviderParamsFail1() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(String.class).toProvider(IntelligentStringProvider.class);
      }
    });

    ObjectFactory.getInstance(String.class, (Object[]) null);
  }

  /**
   * Tests that the Factory throws an exception when an ambiguous provider get argument is passed.
   */
  @Test(expected = ProvisionException.class)
  public void testIntelligentProviderParamsFail2() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(String.class).toProvider(IntelligentStringProvider.class);
      }
    });

    ObjectFactory.getInstance(String.class, "test", null);
  }

  /**
   * Tests that the Factory is capable of intelligently discerning which provider get method to call
   * when null arguments are given.
   */
  @Test
  public void testIntelligentProviderParamsSuccess() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(String.class).toProvider(IntelligentStringProvider.class);
      }
    });

    assertEquals(ObjectFactory.getInstance(String.class), "test");

    String _testStr = "asdf";
    assertEquals(ObjectFactory.getInstance(String.class, null, _testStr), null + _testStr);
  }

  /**
   * Tests that the Factory throws an exception when an ambiguous constructor argument is passed.
   */
  @Test(expected = ProvisionException.class)
  public void testIntelligentTargetParamsFail1() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(IntelligentTest.class);
      }
    });

    ObjectFactory.getInstance(IntelligentTest.class, (Object[]) null);
  }

  /**
   * Tests that the Factory throws an exception when an ambiguous constructor argument is passed.
   */
  @Test(expected = ProvisionException.class)
  public void testIntelligentTargetParamsFail2() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(IntelligentTest.class);
      }
    });

    ObjectFactory.getInstance(IntelligentTest.class, "test", null);
  }

  /**
   * Tests that the Factory is capable of intelligently discerning which constructor to call for a
   * target when null arguments are given.
   */
  @Test
  public void testIntelligentTargetParamsSuccess() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(IntelligentTest.class);
      }
    });

    IntelligentTest _test1 = ObjectFactory.getInstance(IntelligentTest.class);
    assertEquals(_test1.string1, "test");
    assertEquals(_test1.string2, "string2");

    String _testStr = "adsf";
    IntelligentTest _test2 = ObjectFactory.getInstance(IntelligentTest.class, null, _testStr);
    assertEquals(_test2.string1, null);
    assertEquals(_test2.string2, _testStr);
  }

  /**
   * Tests that binding to a provider for a method that does not exist throws an exception.
   */
  @Test(expected = ConfigurationException.class)
  public void testInvalidProviderBinding() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).toProvider(ObjectProvider.class).forParams(Object.class);
      }
    });
  }

  /**
   * Tests that binding to a target for a constructor that does not exist throws an exception.
   */
  @Test(expected = ConfigurationException.class)
  public void testInvalidTargetBinding() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(List.class).to(ArrayList.class).forParams(Set.class);
      }
    });
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

    assertEquals("Objects in same scope should be equal regardless of arguments",
        ObjectFactory.getInstance(Object.class, _testString1),
        ObjectFactory.getInstance(Object.class, _testString2));
    SimpleScope.reset();

    assertEquals("Object should match args since scope was reset", _testString2,
        ObjectFactory.getInstance(Object.class, _testString2));
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
    assertEquals(ObjectFactory.getInstance(Object.class, _testString), _testString);
  }

  /**
   * Tests getInstance on a provider binding with more than one set of params.
   */
  @Test
  public void testProviderBindingWithComplexParams() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).toProvider(ObjectProvider.class).forParams(NO_PARAMS)
            .forOptionalParams(new Class[][] { { String.class }, { String.class, String.class } });
      }
    });

    String _test = "test";
    assertEquals(ObjectFactory.getInstance(Object.class), _test);
    assertEquals(ObjectFactory.getInstance(Object.class, new Object[] {}), _test);
    assertEquals(ObjectFactory.getDefaultInstance(Object.class), _test);
    assertEquals(ObjectFactory.getInstance(Object.class, _test), _test);
    assertEquals(ObjectFactory.getInstance(Object.class, _test, _test), _test + _test);
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

    assertEquals(_testString, ObjectFactory.getInstance(Object.class));
  }

  /**
   * Tests that a scoped target binding with some parameters will produce objects as expected.
   */
  @Test
  public void testTargetBindingScoped() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).to(String.class).forParams(String.class).in(Scopes.SIMPLE);
      }
    });

    String _testString1 = "test1";
    String _testString2 = "test2";

    assertEquals("Objects in same scope should be equal regardless of arguments",
        ObjectFactory.getInstance(Object.class, _testString1),
        ObjectFactory.getInstance(Object.class, _testString2));
    SimpleScope.reset();

    assertEquals("Object should match args since scope was reset", _testString2,
        ObjectFactory.getInstance(Object.class, _testString2));
  }

  /**
   * Tests getInstance on a target binding with arguments explicitly provided.
   */
  @Test
  public void testTargetBindingWithArgs() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(List.class).to(ArrayList.class).forParams(Collection.class);
      }
    });

    String _test = "test";
    assertEquals(ObjectFactory.getInstance(List.class, Arrays.asList(_test)).get(0), _test);
  }

  /**
   * Tests getInstance on a target binding with more than one set of params.
   */
  @Test
  public void testTargetBindingWithComplexParams1() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(List.class).to(TestCollectionClass.class).forParams(List.class)
            .forOptionalParams(NO_PARAMS);
      }
    });

    List _emptyList = new ArrayList();
    assertEquals(ObjectFactory.getInstance(List.class, new Object[] {}), _emptyList);
    assertEquals(ObjectFactory.getDefaultInstance(List.class), _emptyList);

    String _test = "test";
    assertEquals(ObjectFactory.getInstance(List.class, Arrays.asList(_test)).get(0), _test);

    try {
      ObjectFactory.getInstance(List.class);
    } catch (ProvisionException e) {
      return;
    }

    fail("A ProvisionException was expected");
  }

  /**
   * Tests getInstance on a target binding with more than one set of params.
   */
  @Test
  public void testTargetBindingWithComplexParams2() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(List.class).to(TestCollectionClass.class).forParams(NO_PARAMS)
            .forOptionalParams(List.class);
      }
    });

    List _emptyList = new ArrayList();
    assertEquals(ObjectFactory.getInstance(List.class), _emptyList);
    assertEquals(ObjectFactory.getInstance(List.class, new Object[] {}), _emptyList);
    assertEquals(ObjectFactory.getDefaultInstance(List.class), _emptyList);

    String _test = "test";
    assertEquals(ObjectFactory.getInstance(List.class, Arrays.asList(_test)).get(0), _test);
  }

  /**
   * Tests getInstance on target binding with primitive args.
   */
  @Test
  public void testTargetBindingWithPrimitiveArgs() {
    final int _defaultCapacity = 12;

    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(List.class).to(Vector.class).forParams(Integer.TYPE);
        bind(Integer.TYPE).toInstance(_defaultCapacity);
      }
    });

    int _testCapacity = 5;
    List _testList = ObjectFactory.getInstance(List.class, Primitives.of(_testCapacity));
    assertEquals(((Vector) _testList).capacity(), _testCapacity);

    List _testList1 = ObjectFactory.getInstance(List.class);
    assertEquals(((Vector) _testList1).capacity(), _defaultCapacity);
  }

  /**
   * Tests that getInstance with no args on a binding that contains parameters will successfully
   * fulfill the parameters using the factory.
   */
  @Test
  public void testTargetBindingWithoutArgsFulfilled() {
    final String _testString = "test";

    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).to(String.class).forParams(String.class);
        bind(String.class).toInstance(_testString);
      }
    });

    assertEquals("Test strings do not match as expected", _testString,
        ObjectFactory.getInstance(Object.class));
  }

  /**
   * Tests that getInstance with no args on a binding that contains parameters will throw an
   * exception if the arguments cannot be fulfilled by the factory.
   */
  @Test(expected = ProvisionException.class)
  public void testTargetBindingWithoutArgsUnfulfilled() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Object.class).to(String.class).forParams(String.class);
      }
    });

    ObjectFactory.getInstance(Object.class);
  }
}
