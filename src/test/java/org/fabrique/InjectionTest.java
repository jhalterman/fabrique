package org.fabrique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.fabrique.AbstractModule;
import org.fabrique.ConfigurationException;
import org.fabrique.ObjectFactory;
import org.fabrique.Provider;
import org.fabrique.Inject;
import org.fabrique.Primitives;
import org.fabrique.ProvisionException;
import org.junit.Before;
import org.junit.Test;



/**
 * Tests binder factory dependency injection.
 */
public class InjectionTest {
	/** Tests child class constructor injection */
	public static class ChildClassTest extends ConstructorTest {
	}

	/** Tests child class field injection */
	public static class ChildFieldTest extends FieldTest {
	}

	/** Invalid @Inject annotated class */
	public static class ConstructorFailedTest {
		/** Creates a new ConstructorFailedTest object. */
		@Inject
		ConstructorFailedTest() {
		}

		/** Creates a new ConstructorFailedTest object. */
		@Inject
		ConstructorFailedTest(String pTestString) {
		}
	}

	/** Tests constructor injection */
	public static class ConstructorTest {
		Integer testInt;
		List<?> testList;
		String optionalString;
		String testString;
		int testPrimitiveInt;

		/** Creates a new ConstructorTest object. */
		ConstructorTest() {
		}

		/** Creates a new ConstructorTest object. */
		@Inject
		ConstructorTest(String pTestString, List<?> pTestList) {
			testString = pTestString;
			testList = pTestList;
		}

		/** Creates a new ConstructorTest object. */
		@Inject(optional = true)
		ConstructorTest(String pTestString) {
			optionalString = pTestString;
		}

		/** Creates a new ConstructorTest object. */
		@Inject(optional = true)
		ConstructorTest(Integer pTestInt) {
			testInt = pTestInt;
		}

		/** Creates a new ConstructorTest object. */
		@Inject(optional = true)
		ConstructorTest(int pTestInt) {
			testPrimitiveInt = pTestInt;
		}
	}

	/** Tests field injection */
	public static class FieldTest {
		@Inject(optional = true)
		Integer testInt;
		@Inject
		String testString1;
		@Inject(optional = true)
		String testString2;
	}

	/** Tests private injection */
	public static class PrivateConstructorTest {
		int testInt;

		/** Public constructor  */
		private PrivateConstructorTest(int pTestInt) {
			testInt = pTestInt;
		}
	}

	/** Tests child class method injection */
	static class ChildMethodTest extends MethodTest {
	}

	/** Tests method injection */
	static class MethodTest {
		Integer testInt;
		List<?> optionalList;
		List<?> testList;
		String optionalString;
		String testString;

		/** Required Setter */
		@Inject
		public void set(String pTestString, List<?> pTestList) {
			testString = pTestString;
			testList = pTestList;
		}

		/** Optional Setter */
		@Inject(optional = true)
		public void setInt(Integer pTestInt) {
			testInt = pTestInt;
		}

		/** Optional Setter */
		@Inject(optional = true)
		public void setOptional(String pTestString, List<?> pTestList) {
			optionalString = pTestString;
			optionalList = pTestList;
		}
	}

	/** Tests provider injection */
	static class StringProvider implements Provider<String> {
		Integer testInt;
		@Inject
		String testString;

		/** Creates a new StringProvider object. */
		StringProvider() {
		}

		/** Creates a new StringProvider object.  */
		@Inject
		StringProvider(Integer pInt) {
			testInt = pInt;
		}

		/**
		 * {@inheritDoc}
		 */
		public String get() {
			return testString + testInt;
		}

		/** Provider get */
		String get(String pValue) {
			return testString + pValue;
		}

		/** Injected Setter */
		@Inject
		void setString(String pString) {
			testString += pString;
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
	 * Tests injection for a class where the default constructor is private.
	 */
	@Test
	public void testPrivateConstructorInjection() {
		final int _testInt = 3;

		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(Integer.TYPE).toInstance(_testInt);
				bind(PrivateConstructorTest.class).forParams(Integer.TYPE);
			}
		});

		PrivateConstructorTest _test = ObjectFactory.getInstance(PrivateConstructorTest.class);
		assertEquals(_testInt, _test.testInt);
	}

	/**
	 * Tests that child class construction injection injection works as expected.
	 */
	@Test
	public void testChildClassConstructorInjection() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(ChildClassTest.class);
			}
		});

		ChildClassTest _test = ObjectFactory.getInstance(ChildClassTest.class);
		assertNull(_test.testString);
		assertNull(_test.testList);
	}

	/**
	 * Tests that field injection works as expected for required injection points.
	 */
	@Test
	public void testChildClassTargetFieldInjection() {
		final String _testString = "test";

		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(ChildFieldTest.class);
				bind(String.class).toInstance(_testString);
			}
		});

		ChildFieldTest _test = ObjectFactory.getInstance(ChildFieldTest.class);
		assertEquals(_test.testString1, _testString);
		assertEquals(_test.testString2, _testString);
		assertNull(_test.testInt);
	}

	/**
	 * Tests that method injection works as expected for required methods.
	 */
	@Test
	public void testChildClassTargetMethodInjection() {
		final String _testString = "test";

		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(ChildMethodTest.class);
				bind(String.class).toInstance(_testString);
				bind(List.class).to(ArrayList.class);
			}
		});

		ChildMethodTest _test = ObjectFactory.getInstance(ChildMethodTest.class);
		assertEquals(_test.testString, _testString);
		assertEquals(_test.optionalString, _testString);
		assertNotNull(_test.testList);
		assertNotNull(_test.optionalList);
		assertNull(_test.testInt);
	}

	/**
	 * Tests that provider injection works as expected.
	 */
	@Test
	public void testProviderInjection() {
		final String _testString = "test";
		final Integer _testInt = 5;

		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(String.class).toInstance(_testString);
				bind(Integer.class).toInstance(_testInt);
				bind(Object.class).toProvider(StringProvider.class);
			}
		});

		assertEquals(_testString + _testString + _testInt, ObjectFactory.getInstance(Object.class));
	}

	/**
	 * Tests that provider injection works as expected.
	 */
	@Test
	public void testProviderInstanceInjection() {
		final String _testString = "test";
		final Integer _testInt = 5;
		final StringProvider _provider = new StringProvider();

		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(String.class).toInstance(_testString);
				bind(Integer.class).toInstance(_testInt);
				bind(Object.class).toProvider(_provider);
			}
		});

		assertEquals(_testString + _testString + "abc", ObjectFactory.getInstance(Object.class, "abc"));
		assertEquals(_testString + _testString, _provider.testString);
		assertNull(_provider.testInt);

		String _newTestString = "abc";
		Integer _newTestInt = 3;

		_provider.testString = _newTestString;
		_provider.testInt = _newTestInt;

		// Assert that injection does not occur more than once for instance
		ObjectFactory.getInstance(Object.class, "abc");
		assertEquals(_newTestString, _provider.testString);
		assertEquals(_newTestInt, _provider.testInt);
	}

	/**
	 * Tests that duplicate non-optional @Inject annotated constructors results in an exception.
	 */
	@Test(expected = ConfigurationException.class)
	public void testTargetConstructorInjectionDuplicateConstructors() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(ConstructorFailedTest.class);
				bind(String.class).toInstance("test");
			}
		});
	}

	/**
	 * Tests that injection fails if dependencies cannot be created.
	 */
	@Test(expected = ProvisionException.class)
	public void testTargetConstructorInjectionFailed() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(ConstructorTest.class);
			}
		});

		ObjectFactory.getInstance(ConstructorTest.class);
	}

	/**
	 * Test injection into optional constructors using explicitly provided arguments.
	 */
	@Test
	public void testTargetConstructorInjectionOptional() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(ConstructorTest.class);
			}
		});

		String _testString = "test";
		ConstructorTest _test = ObjectFactory.getInstance(ConstructorTest.class, _testString);
		assertNull(_test.testString);
		assertEquals(_testString, _test.optionalString);
		assertNull(_test.testList);
		assertNull(_test.testInt);

		int _testPrimitiveInt = 3;
		ConstructorTest _test1 = ObjectFactory.getInstance(ConstructorTest.class,
				Primitives.of(_testPrimitiveInt));
		assertNull(_test1.testString);
		assertNull(_test1.optionalString);
		assertNull(_test1.testList);
		assertEquals(_testPrimitiveInt, _test1.testPrimitiveInt);
		assertNull(_test1.testInt);

		Integer _testInt = new Integer(5);
		ConstructorTest _test2 = ObjectFactory.getInstance(ConstructorTest.class, _testInt);
		assertNull(_test2.testString);
		assertNull(_test2.optionalString);
		assertNull(_test2.testList);
		assertEquals(0, _test2.testPrimitiveInt);
		assertEquals(_testInt, _test2.testInt);
	}

	/**
	 * Test injection into a default constructor.
	 */
	@Test
	public void testTargetConstructorInjectionRequired() {
		final String defaultString = "test";

		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(ConstructorTest.class);
				bind(String.class).toInstance(defaultString);
				bind(List.class).to(ArrayList.class);
			}
		});

		ConstructorTest _test = ObjectFactory.getInstance(ConstructorTest.class);

		assertEquals(_test.testString, defaultString);
		assertNotNull(_test.testList);
		assertNull(_test.optionalString);
		assertNull(_test.testInt);

		String _testString = "test";
		List _testList = new ArrayList();
		_test = ObjectFactory.getInstance(ConstructorTest.class, _testString, _testList);
		assertEquals(_testString, _test.testString);
		assertEquals(_testList, _test.testList);
	}

	/**
	 * Tests that field injection works as expected for required injection points.
	 */
	@Test
	public void testTargetFieldInjection() {
		final String _testString = "test";

		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(FieldTest.class);
				bind(String.class).toInstance(_testString);
			}
		});

		FieldTest _test = ObjectFactory.getInstance(FieldTest.class);
		assertEquals(_test.testString1, _testString);
		assertEquals(_test.testString2, _testString);
		assertNull(_test.testInt);
	}

	/**
	 * Tests that field injection works as expected for a target instance.
	 */
	@Test
	public void testTargetInstanceFieldInjection() {
		final String _testString = "test";

		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(FieldTest.class).toInstance(new FieldTest());
				bind(String.class).toInstance(_testString);
			}
		});

		FieldTest _test = ObjectFactory.getInstance(FieldTest.class);
		assertEquals(_test.testString1, _testString);
		assertEquals(_test.testString2, _testString);
		assertNull(_test.testInt);

		String _testString1 = "abc";
		Integer _testInt = 5;

		_test.testString1 = _testString1;
		_test.testInt = _testInt;

		// Assert that injection does not occur more than once for instance
		_test = ObjectFactory.getInstance(FieldTest.class);
		assertEquals(_testString1, _test.testString1);
		assertEquals(_testString, _test.testString2);
		assertEquals(_testInt, _test.testInt);
	}

	/**
	 * Tests that method injection works as expected for a target instance.
	 */
	@Test
	public void testTargetInstanceMethodInjection() {
		final String _testString = "test";

		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(MethodTest.class).toInstance(new MethodTest());
				bind(String.class).toInstance(_testString);
				bind(List.class).to(ArrayList.class);
			}
		});

		MethodTest _test = ObjectFactory.getInstance(MethodTest.class);
		assertEquals(_test.testString, _testString);
		assertEquals(_test.optionalString, _testString);
		assertTrue(_test.testList instanceof ArrayList);
		assertTrue(_test.optionalList instanceof ArrayList);
		assertNull(_test.testInt);

		String _newTestString = "Abc";
		Integer _testInt = 4;
		List _testList = new Vector();

		_test.testString = _newTestString;
		_test.testInt = _testInt;
		_test.testList = _testList;

		// Assert that injection does not occur more than once for instance
		_test = ObjectFactory.getInstance(MethodTest.class);
		assertEquals(_newTestString, _test.testString);
		assertEquals(_testString, _test.optionalString);
		assertTrue(_test.testList instanceof Vector);
		assertTrue(_test.optionalList instanceof ArrayList);
		assertEquals(_testInt, _test.testInt);
	}

	/**
	 * Tests that method injection works as expected for required methods.
	 */
	@Test
	public void testTargetMethodInjection() {
		final String _testString = "test";

		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(MethodTest.class);
				bind(String.class).toInstance(_testString);
				bind(List.class).to(ArrayList.class);
			}
		});

		MethodTest _test = ObjectFactory.getInstance(MethodTest.class);
		assertEquals(_test.testString, _testString);
		assertEquals(_test.optionalString, _testString);
		assertNotNull(_test.testList);
		assertNotNull(_test.optionalList);
		assertNull(_test.testInt);
	}

	/**
	 * Tests that method injection fails if a dependency cannot be created.
	 */
	@Test(expected = ProvisionException.class)
	public void testTargetMethodInjectionFailed() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(MethodTest.class);
			}
		});

		ObjectFactory.getInstance(MethodTest.class);
	}
}
