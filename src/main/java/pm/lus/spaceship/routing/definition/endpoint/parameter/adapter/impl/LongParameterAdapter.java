package pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.impl;

import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterParseException;
import pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.ParameterAdapter;

/**
 * Implements the {@link ParameterAdapter} interface to use {@link Long}s as route parameters
 * This adapter is registered by default and has a weight of 40
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class LongParameterAdapter implements ParameterAdapter<Long> {

    @Override
    public Long parse(final String raw) throws ParameterParseException {
        try {
            return Long.parseLong(raw);
        } catch (final NumberFormatException ignored) {
            throw new ParameterParseException();
        }
    }

}
