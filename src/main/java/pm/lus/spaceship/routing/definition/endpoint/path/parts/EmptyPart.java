package pm.lus.spaceship.routing.definition.endpoint.path.parts;

/**
 * Represents an empty path part
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class EmptyPart implements PathPart {

    @Override
    public boolean matches(final PathPart other) {
        return other instanceof EmptyPart;
    }

}
