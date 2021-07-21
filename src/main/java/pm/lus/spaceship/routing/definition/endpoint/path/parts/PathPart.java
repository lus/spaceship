package pm.lus.spaceship.routing.definition.endpoint.path.parts;

/**
 * Represents a part of a path definition
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public interface PathPart {

    /**
     * Checks whether or not this part matches another one
     *
     * @param other The other path part to check
     * @return Whether or not this part matches another one
     */
    boolean matches(PathPart other);

}
