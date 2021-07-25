package pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.impl;

import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterParseException;
import pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.ParameterAdapter;

/**
 * Implements the {@link ParameterAdapter} interface to use {@link Boolean}s as route parameters
 * This adapter is registered by default and has a weight of 10
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class BooleanParameterAdapter implements ParameterAdapter<Boolean> {

    @Override
    public Boolean parse(final String raw) throws ParameterParseException {
        if ("true".equalsIgnoreCase(raw)) {
            return true;
        } else if ("false".equalsIgnoreCase(raw)) {
            return false;
        }
        throw new ParameterParseException();
    }

}
