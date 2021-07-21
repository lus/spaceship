package pm.lus.spaceship.routing.definition.endpoint.parameter;

/**
 * Has to be implemented for types that should be able to be passed to endpoint handlers as parameters
 *
 * @param <T> The generic type
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public interface ParameterAdapter<T> {

    /**
     * Parses a raw string into the desired type
     *
     * @param raw The raw string to parse
     * @return The parsed value
     * @throws ParameterParseException If the type cannot be parsed
     */
    T parse(String raw) throws ParameterParseException;

}
