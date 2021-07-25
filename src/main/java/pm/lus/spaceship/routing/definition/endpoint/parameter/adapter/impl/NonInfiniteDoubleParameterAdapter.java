package pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.impl;

import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterParseException;
import pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.ParameterAdapter;

/**
 * Implements the {@link ParameterAdapter} interface to use {@link Double}s as route parameters
 * Unless {@link DoubleParameterAdapter}, this adapter does not accept Infinity as a valid value
 * This adapter is registered by default and has a weight of 20
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class NonInfiniteDoubleParameterAdapter implements ParameterAdapter<Double> {

    @Override
    public Double parse(final String raw) throws ParameterParseException {
        try {
            final Double value = Double.parseDouble(raw);
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
