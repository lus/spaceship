package pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.impl;

import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterParseException;
import pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.ParameterAdapter;

/**
 * Implements the {@link ParameterAdapter} interface to use {@link Short}s as route parameters
 * This adapter is registered by default and has a weight of 60
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class ShortParameterAdapter implements ParameterAdapter<Short> {

    @Override
    public Short parse(final String raw) throws ParameterParseException {
        try {
            return Short.parseShort(raw);
        } catch (final NumberFormatException ignored) {
            throw new ParameterParseException();
        }
    }

}
