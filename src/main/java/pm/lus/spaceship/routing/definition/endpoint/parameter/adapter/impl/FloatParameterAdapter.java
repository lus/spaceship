package pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.impl;

import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterParseException;
import pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.ParameterAdapter;

/**
 * Implements the {@link ParameterAdapter} interface to use {@link Float}s as route parameters
 * Unless {@link NonInfiniteFloatParameterAdapter}, this adapter also parses Infinity
 * This adapter is not registered by default
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class FloatParameterAdapter implements ParameterAdapter<Float> {

    @Override
    public Float parse(final String raw) throws ParameterParseException {
        try {
            return Float.parseFloat(raw);
        } catch (final NumberFormatException ignored) {
            throw new ParameterParseException();
        }
    }

}
