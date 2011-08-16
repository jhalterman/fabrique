package org.fabrique.matcher;

import java.io.Serializable;

/**
 * Implements {@code and()} and {@code or()}.
 * 
 * @author Bob Lee (crazybob@google.com)
 * @param <T> Type to match
 */
public abstract class AbstractMatcher<T> implements Matcher<T> {
    /**
     * Boolean AND matcher.
     */
    private static class AndMatcher<T> extends AbstractMatcher<T> implements Serializable {
        private static final long serialVersionUID = 0;
        private final Matcher<? super T> a;
        private final Matcher<? super T> b;

        /**
         * Creates a new AndMatcher object.
         * 
         * @param a Matcher A
         * @param b Matcher B
         */
        public AndMatcher(Matcher<? super T> a, Matcher<? super T> b) {
            this.a = a;
            this.b = b;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof AndMatcher && ((AndMatcher<?>) other).a.equals(a)
                    && ((AndMatcher<?>) other).b.equals(b);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 41 * (a.hashCode() ^ b.hashCode());
        }

        /**
         * {@inheritDoc}
         */
        public boolean matches(T t) {
            return a.matches(t) && b.matches(t);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "and(" + a + ", " + b + ")";
        }
    }

    /**
     * Boolean OR matcher.
     */
    private static class OrMatcher<T> extends AbstractMatcher<T> implements Serializable {
        private static final long serialVersionUID = 0;
        private final Matcher<? super T> a;
        private final Matcher<? super T> b;

        /**
         * Creates a new OrMatcher object.
         * 
         * @param a Matcher A
         * @param b Matcher B
         */
        public OrMatcher(Matcher<? super T> a, Matcher<? super T> b) {
            this.a = a;
            this.b = b;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof OrMatcher && ((OrMatcher<?>) other).a.equals(a)
                    && ((OrMatcher<?>) other).b.equals(b);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 37 * (a.hashCode() ^ b.hashCode());
        }

        /**
         * {@inheritDoc}
         */
        public boolean matches(T t) {
            return a.matches(t) || b.matches(t);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "or(" + a + ", " + b + ")";
        }
    }

    /**
     * {@inheritDoc}
     */
    public Matcher<T> and(final Matcher<? super T> other) {
        return new AndMatcher<T>(this, other);
    }

    /**
     * {@inheritDoc}
     */
    public Matcher<T> or(Matcher<? super T> other) {
        return new OrMatcher<T>(this, other);
    }
}
