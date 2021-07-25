package pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.impl;

import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterParseException;
import pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.ParameterAdapter;

/**
 * Implements the {@link ParameterAdapter} interface to use {@link Float}s as route parameters
 * Unless {@link FloatParameterAdapter}, this adapter does not accept Infinity as a valid value
 * This adapter is registered by default and has a weight of 30
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class NonInfiniteFloatParameterAdapter implements ParameterAdapter<Float> {

    @Override
    public Float parse(final String raw) throws ParameterParseException {
        try {
            final Float value = Float.parseFloat(raw);
            if (!value.isInfinite()) {
                return value;
            } else {
                throw new ParameterParseException();
            }
        } catch (final NumberFormatException ignored) {
            throw new ParameterParseException();
        }
    }

}
