package org.fabrique;

import org.fabrique.Examples;
import org.fabrique.matcher.MatcherTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;



/**
 * Binder factory test suite.
  */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	BindingTest.class, 
	BindingsTest.class,
	BindingParamsTest.class, 
	ObjectFactoryTest.class, 
	InjectionTest.class,
	InterceptorTest.class,
	JitBindingTest.class,
	KeyTest.class, 
	MatcherTest.class,
	ModuleTest.class, 
	NamedBindingTest.class,
	PrimitivesTest.class,
	ProviderTest.class, 
	ScopesTest.class, 
	Examples.class,
	})
public class TestSuite {
}