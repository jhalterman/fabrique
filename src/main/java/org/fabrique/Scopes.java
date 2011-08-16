package org.fabrique;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Provides default scope implementations.
 */
public final class Scopes {
  /** Singleton scope */
  public static final Scope SINGLETON = new SingletonScope();

  /** Eager Singleton scope */
  public static final Scope EAGER_SINGLETON = new SingletonScope();

  /**
   * Thread scope
   */
  public static final Scope THREAD = new Scope() {
    public <T> ScopedProvider<T> scope(final Key<T> key) {
      return new ScopedProvider<T>() {
        public T get() {
          ThreadLocalCache cache = ThreadLocalCache.getInstance();
          T value = cache.get(key);

          if (value == null) {
            value = provider.get();
            cache.add(key, value);
          }

          return value;
        }
      };
    }
  };

  /** Simple scope */
  public static final SimpleScope SIMPLE = new SimpleScope();

  /**
   * Pool scope. Maintains a sized pool of objects.
   */
  public static class PoolScope implements Scope {
    private static Map<Key<?>, BlockingQueue<?>> pools = new HashMap<Key<?>, BlockingQueue<?>>();
    private int poolSize;

    /**
     * Creates a new PoolScope object.
     * 
     * @param poolSize Pool size
     */
    public PoolScope(int poolSize) {
      this.poolSize = poolSize;
    }

    /**
     * Gets a pool for the given key.
     * 
     * @param <T> Pooled Type
     * @param key .
     * @return Pool
     */
    @SuppressWarnings("unchecked")
    public static <T> BlockingQueue<T> getPool(Key<T> key) {
      return (BlockingQueue<T>) pools.get(key);
    }

    /**
     * Gets a pool for the given type.
     * 
     * @param <T> Pooled Type
     * @param type .
     * @return Pool
     */
    @SuppressWarnings("unchecked")
    public static <T> BlockingQueue<T> getPool(Class<T> type) {
      return (BlockingQueue<T>) pools.get(Key.get(type));
    }

    /**
     * Releases an object back to the pool.
     * 
     * @param <T> Type
     * @param key Key of object to release
     * @param object Object to release
     */
    @SuppressWarnings("unchecked")
    public static <T> void release(Key<T> key, T object) {
      BlockingQueue<T> pool = (BlockingQueue<T>) pools.get(key);
      if (pool != null)
        pool.add(object);

    }

    /**
     * {@inheritDoc}
     */
    public <T> ScopedProvider<T> scope(final Key<T> key) {
      final BlockingQueue<T> pool = new ArrayBlockingQueue<T>(poolSize);
      createPool(key, pool);

      return new ScopedProvider<T>() {
        int objectCount;

        public T get() {
          if (pool.isEmpty() && objectCount < poolSize) {
            objectCount++;
            return provider.get();
          }

          try {
            return pool.take();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
          }
        }
      };
    }

    /**
     * Creates a pool.
     * 
     * @param pKey .
     * @param pPool .
     */
    private static void createPool(Key<?> pKey, BlockingQueue<?> pPool) {
      pools.put(pKey, pPool);
    }
  }

  /**
   * Provides common behavior for scoped objects that are scoped by some shared context. Scoped
   * object retrievals synchronize on the shared context.
   * 
   * @param <C> Context type
   */
  public abstract static class SharedContextScope<C> implements Scope {
    protected final Map<C, Map<Key<?>, Object>> scoped = new HashMap<C, Map<Key<?>, Object>>();

    /**
     * Gets the current shared context.
     * 
     * @return shared context
     */
    protected abstract C getContext();

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> ScopedProvider<T> scope(final Key<T> key) {
      return new ScopedProvider<T>() {
        public T get() {
          C context = getContext();

          synchronized (context) {
            T object = null;
            Map<Key<?>, Object> storage = scoped.get(context);

            if (storage == null) {
              storage = new HashMap<Key<?>, Object>();
              scoped.put(context, storage);
            } else
              object = (T) storage.get(key);

            if (object == null) {
              object = provider.get();
              storage.put(key, object);
            }

            return object;
          }
        }
      };
    }
  }

  /**
   * Simple scope. Items can be manually added and removed from scope, or the scope can be reset
   * entirely. Useful for testing.
   */
  public static class SimpleScope implements Scope {
    private static final Map<Key<?>, Object> scoped = new HashMap<Key<?>, Object>();

    /**
     * Removes {@code key} from scope.
     * 
     * @param key Key of binding to scope
     */
    public static void remove(Key<?> key) {
      scoped.remove(key);
    }

    /**
     * Removes all scopes entirely.
     */
    public static void reset() {
      scoped.clear();
    }

    /**
     * Adds {@code object} to scope for {@code key}.
     * 
     * @param key Scope key
     * @param object Object to scope
     */
    public static void scope(Key<?> key, Object object) {
      scoped.put(key, object);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> ScopedProvider<T> scope(final Key<T> key) {
      return new ScopedProvider<T>() {
        public T get() {
          T instance = (T) scoped.get(key);

          if (instance == null) {
            instance = provider.get();
            scoped.put(key, instance);
          }

          return instance;
        }
      };
    }
  }

  /**
   * Thread local cache implementation.
   */
  private static final class ThreadLocalCache {
    private static final ThreadLocal<ThreadLocalCache> THREAD_LOCAL = new ThreadLocal<ThreadLocalCache>() {
      @Override
      protected ThreadLocalCache initialValue() {
        return new ThreadLocalCache();
      }
    };

    /** Thread local scoped storage */
    private Map<Key<?>, Object> storage = new HashMap<Key<?>, Object>();

    /**
     * @see Map#put(Object, Object)
     */
    public <T> void add(Key<T> key, T value) {
      storage.put(key, value);
    }

    /**
     * @see Map#get(Object)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
      return (T) storage.get(key);
    }

    /**
     * Static factory method.
     * 
     * @return ThreadLocalCache
     */
    public static ThreadLocalCache getInstance() {
      return THREAD_LOCAL.get();
    }
  }

  /**
   * Singleton scope implementation.
   */
  private static class SingletonScope implements Scope {
    /**
     * {@inheritDoc}
     */
    public <T> ScopedProvider<T> scope(Key<T> key) {
      return new ScopedProvider<T>() {
        private volatile T instance;

        public T get() {
          if (instance == null)
            synchronized (Scopes.class) {
              if (instance == null)
                instance = provider.get();
            }

          return instance;
        }
      };
    }
  }
}
