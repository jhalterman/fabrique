package org.fabrique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.fabrique.AbstractModule;
import org.fabrique.ConfigurationException;
import org.fabrique.ObjectFactory;
import org.fabrique.Provider;
import org.fabrique.ProvisionException;
import org.fabrique.Scopes;
import org.junit.Before;
import org.junit.Test;



/**
 * Tests the creation of various bindings without parameters.
 */
public class BindingTest {
	/** Test provider interface */
	interface TestListProvider extends Provider<List<?>> {
	}

	/**
	 * Performs setup.
	 */
	@Before
	public void setup() {
		TestUtil.resetFactoryBindings();
	}

	/**
	 * Tests that binding an abstract class is acceptable as long as the target is not abstract.
	 */
	@Test
	public void testBindAbstractClass() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(AbstractList.class).to(ArrayList.class);
			}
		});
	}

	/**
	 * Tests that an abstract class binding without an implementation is not allowed.
	 */
	@Test(expected = ConfigurationException.class)
	public void testBindAbstractWithoutImplementation() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(AbstractList.class);
			}
		});
	}

	/**
	 * Tests that a class can be bound to a class.
	 */
	@Test
	public void testBindClassToClass() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(AbstractList.class).to(ArrayList.class);
			}
		});

		assertNotNull(ObjectFactory.getInstance(AbstractList.class));
	}

	/**
	 * Tests that a class can be bound to a scope.
	 */
	@Test
	public void testBindClassToScope() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(ArrayList.class).in(Scopes.SINGLETON);
				bind(List.class).to(ArrayList.class);
			}
		});

		List _instance1 = ObjectFactory.getInstance(List.class);
		List _instance2 = ObjectFactory.getInstance(List.class);
		assertEquals("List instances should be equal since ArrayList is in Singleton scope",
				_instance1, _instance2);
	}

	/**
	 * Tests that an interface cannot be bound to another interface.
	 */
	@Test(expected = ConfigurationException.class)
	public void testBindInterfaceToInterface() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(Collection.class).to(List.class);
			}
		});
	}

	/**
	 * Tests that a binding without an implementation is not allowed.
	 */
	@Test(expected = ConfigurationException.class)
	public void testBindInterfaceWithoutImplementation() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(List.class).asEagerSingleton();
			}
		});
	}

	/**
	 * Tests that a binding interface without an implementation is not allowed.
	 */
	@Test(expected = ConfigurationException.class)
	public void testBindInterfaceWithoutImplementation2() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(List.class);
			}
		});
	}

	/**
	 * Tests that one binding without an implementation still disallows a duplicate binding.
	 */
	@Test(expected = ConfigurationException.class)
	public void testBindOneWithoutImplementation() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(Vector.class);
				bind(Vector.class).to(Stack.class);
			}
		});
	}

	/**
	 * Tests that an abstract class cannot be bound.
	 */
	@Test(expected = ConfigurationException.class)
	public void testBindToAbstractClass() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(List.class).to(AbstractList.class);
			}
		});
	}

	/**
	 * Tests that an abstract provider cannot be bound.
	 */
	@Test(expected = ConfigurationException.class)
	public void testBindToAbstractProvider() {
		/** Abstract provider */
		abstract class AbstractProvider implements Provider<List> {
		}
		;

		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(List.class).toProvider(AbstractProvider.class);
			}
		});
	}

	/**
	 * Tests that an interface cannot be bound to a provider interface.
	 */
	@Test(expected = ConfigurationException.class)
	public void testBindToProviderInterface() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(List.class).toProvider(TestListProvider.class);
			}
		});
	}

	/**
	 * Tests that a class can be bound to nothing.
	 */
	@Test
	public void testBindWithoutImplementation() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(ArrayList.class);
			}
		});

		assertTrue(ArrayList.class
				.isAssignableFrom(ObjectFactory.getInstance(ArrayList.class).getClass()));
	}

	/**
	 * Tests that circular dependencies are detected.
	 */
	@Test(expected = ProvisionException.class)
	public void testCircularDependency() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(Collection.class).to(ArrayList.class).forParams(Collection.class);
			}
		});

		ObjectFactory.getInstance(Collection.class);
	}

	/**
	 * Tests that an exception is thrown when attempting to the same key more than once.
	 */
	@Test(expected = ConfigurationException.class)
	public void testDuplicateBinding() {
		ObjectFactory.loadModules(new AbstractModule() {
			protected void configure() {
				bind(List.class).to(ArrayList.class);
				bind(List.class).to(Vector.class);
			}
		});
	}

	/**
	 * Tests that a type cannot be bound to itself.
	 */
	@Test(expected = ConfigurationException.class)
	public void testSelfBinding() {
		ObjectFactory.loadModules(new AbstractModule() {
			@Override
			public void configure() {
				bind(Thread.class).to(Thread.class);
			}
		});
	}
}
