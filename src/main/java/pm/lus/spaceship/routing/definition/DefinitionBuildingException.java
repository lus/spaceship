package pm.lus.spaceship.routing.definition;

/**
 * Represents an exception that is thrown while building a definition
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class DefinitionBuildingException extends Exception {

    public DefinitionBuildingException(final String message) {
        super(message);
    }

    public DefinitionBuildingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DefinitionBuildingException(final Throwable cause) {
        super(cause);
    }

}
