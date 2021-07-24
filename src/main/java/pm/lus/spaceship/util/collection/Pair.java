package pm.lus.spaceship.util.collection;

/**
 * Represents a dead simple pair of objects
 *
 * @param <A> The type of the first object
 * @param <B> The type of the second object
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class Pair<A, B> {

    private final A a;
    private final B b;

    public Pair(final A a, final B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return this.a;
    }

    public B getB() {
        return this.b;
    }

}
