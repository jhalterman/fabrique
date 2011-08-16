package org.fabrique;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link com.dorado.common.binder.AbstractModule} and
 * {@link com.Module.common.binder.IModule}.
 */
public class ModuleTest {
  /** */
  private static class CircularModule1 extends AbstractModule {
    /**
     * {@inheritDoc}
     */
    public void configure() {
      bind(List.class).to(ArrayList.class);
      install(new CircularModule2());
    }
  }

  /** */
  private static class CircularModule2 extends AbstractModule {
    /**
     * {@inheritDoc}
     */
    public void configure() {
      bind(Collection.class).to(Vector.class);
      install(new CircularModule1());
    }
  }

  /** */
  private static class DiamondModule1 extends AbstractModule {
    /**
     * {@inheritDoc}
     */
    public void configure() {
      bind(List.class).to(ArrayList.class);
      install(new DiamondModule2());
      install(new DiamondModule3());
    }
  }

  /** */
  private static class DiamondModule2 extends AbstractModule {
    /**
     * {@inheritDoc}
     */
    public void configure() {
      bind(Collection.class).to(Vector.class);
      install(new DiamondModule3());
    }
  }

  /** */
  private static class DiamondModule3 extends AbstractModule {
    /**
     * {@inheritDoc}
     */
    public void configure() {
      bind(Set.class).to(HashSet.class);
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
   * Tests binding by a key.
   */
  @Test
  public void testBindByKey() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Key.get(List.class)).to(ArrayList.class);
      }
    });

    assertNotNull("Binding was not created", ObjectFactory.getBinding(List.class));
  }

  /**
   * Tests that circularly dependent modules don't blow up anything.
   */
  @Test
  public void testCircularDependency() {
    ObjectFactory.loadModules(new CircularModule1());

    assertNotNull(ObjectFactory.getInstance(List.class));
    assertNotNull(ObjectFactory.getInstance(Collection.class));
  }

  /**
   * Tests that the diamond pattern doesn't blow up anything.
   */
  @Test
  public void testDiamondDependency() {
    ObjectFactory.loadModules(new DiamondModule1());

    assertNotNull(ObjectFactory.getInstance(List.class));
    assertNotNull(ObjectFactory.getInstance(Collection.class));
    assertNotNull(ObjectFactory.getInstance(Set.class));
  }

  /**
   * Tests {@link AbstractModule#install(Module)}.
   */
  @Test
  public void testInstall() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        install(new DiamondModule3());
      }
    });

    Set<?> _set = ObjectFactory.getInstance(Set.class);
    assertNotNull("Factory failed to produce an object", _set);
    assertTrue("Factory failed to produce a set", Set.class.isAssignableFrom(_set.getClass()));
  }

  /**
   * Tests that a module does not allow the configure method to be re-entered.
   */
  @Test(expected = IllegalStateException.class)
  public void testReEntry() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        this.configure(binder);
      }
    });
  }
}
