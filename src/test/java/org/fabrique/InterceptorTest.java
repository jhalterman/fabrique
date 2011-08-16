package org.fabrique;

import static org.fabrique.matcher.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.fabrique.AbstractModule;
import org.fabrique.ObjectFactory;
import org.fabrique.intercept.IMethodInterceptor;
import org.fabrique.intercept.MethodInvocation;
import org.fabrique.matcher.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the binder factory's method interceptor capabilities.
 */
public class InterceptorTest {
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.TYPE, ElementType.METHOD })
  @interface Adder {
  }

  private AtomicInteger count = new AtomicInteger();

  /**
   * Method interceptor that returns the sum of the intercepted method's result and the argument.
   */
  private final IMethodInterceptor additionInterceptor = new IMethodInterceptor() {
    public Object invoke(MethodInvocation pInvocation) throws Throwable {
      Integer _arg = (Integer) pInvocation.getArguments()[0];
      return (Integer) pInvocation.proceed() + _arg;
    }
  };

  private final IMethodInterceptor countingInterceptor = new IMethodInterceptor() {
    public Object invoke(MethodInvocation pInvocation) throws Throwable {
      count.incrementAndGet();
      return pInvocation.proceed();
    }
  };

  private final IMethodInterceptor returnNullInterceptor = new IMethodInterceptor() {
    public Object invoke(MethodInvocation pInvocation) throws Throwable {
      return null;
    }
  };

  /** Test interceptor class. */
  @Adder
  public static class ClassSubject {
    /** Intercepted method. */
    int method(int pArg) {
      return pArg;
    }
  }

  /** Test Interceptable class */
  public static class Interceptable {
    /** Intercepted method */
    public List getList() {
      return new ArrayList();
    }

    /** Intercepted method */
    public Map getMap() {
      return new HashMap();
    }
  }

  /** Test interceptor class. */
  public static class MethodSubject {
    /** Intercepted method. */
    @Adder
    int method(int pArg) {
      return pArg;
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
   * Tests that the cached injector does not prevent an interceptor from being applied later for the
   * same type.
   */
  @Test
  public void testCachedInjectorCollision() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bind(Interceptable.class);
      }
    });

    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bindInterceptor(Matchers.only(Interceptable.class), Matchers.returns(only(List.class)),
            returnNullInterceptor);
      }
    });

    assertNull(ObjectFactory.getInstance(Interceptable.class).getList());
  }

  /**
   * Tests a single interceptor to see that it is called.
   */
  @Test
  public void testClassInterceptor() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bindInterceptor(Matchers.annotatedWith(Adder.class), Matchers.any(), additionInterceptor);
      }
    });

    ClassSubject _subject = ObjectFactory.getInstance(ClassSubject.class);

    int _input = 5;
    assertEquals(2 * _input, _subject.method(_input));
  }

  /**
   * Tests that IMethodInterceptor.getThis() returns the intercepted object as expected.
   */
  @Test
  public void testGetThis() {
    final AtomicReference<Object> lastTarget = new AtomicReference<Object>();

    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.any(), new IMethodInterceptor() {
          public Object invoke(MethodInvocation pInv) throws Throwable {
            lastTarget.set(pInv.getThis());
            return pInv.proceed();
          }
        });
      }
    });

    Interceptable interceptable = ObjectFactory.getInstance(Interceptable.class);
    interceptable.getList();
    assertSame(interceptable, lastTarget.get());
  }

  /**
   * Tests a single interceptor to see that it is called.
   */
  @Test
  public void testMethodInterceptor() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Adder.class), additionInterceptor);
      }
    });

    MethodSubject _subject = ObjectFactory.getInstance(MethodSubject.class);

    int _input = 4;
    assertEquals(2 * _input, _subject.method(_input));
  }

  /**
   * Tests that multiple interceptors can be loaded separately for the same method.
   */
  @Test
  public void testMultipleInterceptorsForMethod() {
    ObjectFactory.loadModules(new AbstractModule() {
      protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.returns(only(List.class)), countingInterceptor);
        bindInterceptor(Matchers.any(), Matchers.returns(only(List.class).or(only(Map.class))),
            returnNullInterceptor);
      }
    });

    // Increment the counter
    List _list = ObjectFactory.getInstance(Interceptable.class).getList();
    assertNull("Expected return null interceptor to be invoked", _list);
    assertEquals("Expected counting interceptor to be invoked", 1, count.get());

    Map _map = ObjectFactory.getInstance(Interceptable.class).getMap();
    assertNull("Expected return null interceptor to be invoked", _map);
  }
}
