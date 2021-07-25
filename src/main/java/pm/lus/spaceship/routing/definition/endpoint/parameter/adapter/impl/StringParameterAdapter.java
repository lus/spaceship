package pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.impl;

import pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.ParameterAdapter;

/**
 * Implements the {@link ParameterAdapter} interface to use {@link String}s as route parameters
 * This adapter is registered by default and has a weight of 0
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class StringParameterAdapter implements ParameterAdapter<String> {

    @Override
    public String parse(final String raw) {
        return raw;
    }

}
