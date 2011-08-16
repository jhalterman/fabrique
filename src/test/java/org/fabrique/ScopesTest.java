package org.fabrique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.fabrique.Scopes.PoolScope;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link com.dorado.common.binder.Scopes}.
 */
public class ScopesTest {
    private boolean singletonLoaded;

    /** Test interface */
    interface ITest {
    }

    /** Test class */
    public static class TestClass implements ITest {
    }

    /** Test class provider */
    class TestProvider implements Provider<ITest> {
        /**
         * {@inheritDoc}
         */
        public ITest get() {
            singletonLoaded = true;
            return new TestClass();
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
     * Tests that a singleton is eagerly loaded.
     */
    @Test
    public void testEagerSingleton() {
        ObjectFactory.loadModules(new AbstractModule() {
            protected void configure() {
                bind(ITest.class).toProvider(new TestProvider()).asEagerSingleton();
            }
        });

        assertTrue("TestClass should be eagerly instantiated", singletonLoaded);
    }

    /**
     * Tests the pool scope.
     */
    @Test
    public void testPool() {
        ObjectFactory.loadModules(new AbstractModule() {
            protected void configure() {
                bind(String.class).forParams(String.class).in(new PoolScope(2));
            }
        });

        String _string1 = ObjectFactory.getInstance(String.class, "test1");
        String _string2 = ObjectFactory.getInstance(String.class, "test2");
        PoolScope.release(Key.get(String.class), _string1);

        String _string3 = ObjectFactory.getInstance(String.class, "test3");
        PoolScope.release(Key.get(String.class), _string2);

        String _string4 = ObjectFactory.getInstance(String.class, "test4");
        assertEquals(_string1, _string3);
        assertEquals(_string2, _string4);
        PoolScope.release(Key.get(String.class), _string4);

        String _string5 = ObjectFactory.getInstance(String.class, "test5");
        assertEquals(_string2, _string5);
        assertEquals(_string4, _string5);
    }

    /**
     * Tests that singleton instances are properly produced by a singleton scoped provider.
     */
    @Test
    public void testSingleton() {
        ObjectFactory.loadModules(new AbstractModule() {
            protected void configure() {
                bind(ITest.class).to(TestClass.class).asSingleton();
            }
        });

        Object _instance1 = ObjectFactory.getInstance(ITest.class);
        Object _instance2 = ObjectFactory.getInstance(ITest.class);
        assertEquals("Singleton scoped binding does not produce identical instances", _instance1,
                _instance2);
    }

    /**
     * Tests that thread scope works as expected.
     */
    @Test
    public void testThreadScope() {
        ObjectFactory.loadModules(new AbstractModule() {
            protected void configure() {
                bind(ITest.class).to(TestClass.class).in(Scopes.THREAD);
            }
        });

        final ITest _instance1 = ObjectFactory.getInstance(ITest.class);
        ITest _instance2 = ObjectFactory.getInstance(ITest.class);

        assertEquals(
                "Thread scoped binding does not produce identical instances for the same thread",
                _instance1, _instance2);

        Thread _thread = new Thread() {
            public void run() {
                assertNotSame(
                        "Thread scoped binding produces identical instance in separate thread",
                        _instance1, ObjectFactory.getInstance(ITest.class));
            }
        };

        _thread.setUncaughtExceptionHandler(Thread.getDefaultUncaughtExceptionHandler());
        _thread.start();
    }
}
