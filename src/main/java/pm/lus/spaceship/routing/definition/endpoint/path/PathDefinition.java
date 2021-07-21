package pm.lus.spaceship.routing.definition.endpoint.path;

import pm.lus.spaceship.routing.Router;
import pm.lus.spaceship.routing.definition.DefinitionBuildingException;
import pm.lus.spaceship.routing.definition.endpoint.EndpointDefinition;
import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterDefinition;
import pm.lus.spaceship.routing.definition.endpoint.path.parts.EmptyPart;
import pm.lus.spaceship.routing.definition.endpoint.path.parts.LiteralPart;
import pm.lus.spaceship.routing.definition.endpoint.path.parts.ParameterPart;
import pm.lus.spaceship.routing.definition.endpoint.path.parts.PathPart;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a definition of a path of an {@link EndpointDefinition} for the {@link Router} to work with
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class PathDefinition {

    private static final Pattern PARAMETER_REGEX = Pattern.compile("\\{[a-zA-Z_-]*}");

    private final List<PathPart> parts;

    private PathDefinition(final List<PathPart> parts) {
        this.parts = parts;
    }

    public static PathDefinition build(final String raw, final List<ParameterDefinition> parameters) throws DefinitionBuildingException {
        final List<PathPart> parts = new ArrayList<>();

        // Split the raw path declaration into multiple path parts
        final String[] rawParts = raw.split("/");
        int parameterCount = 0;
        boolean optionalsIntroduced = false;
        for (int i = 0; i < rawParts.length; i++) {
            final String rawPart = rawParts[i];

            // Empty parts ('//') are needed for strict routing
            if (rawPart.isEmpty()) {
                parts.add(new EmptyPart());
                continue;
            }

            // Check if the current part is a parameter
            if (PARAMETER_REGEX.matcher(rawPart).matches()) {
                // The amount of parameters in the raw path declaration has to match the amount of parameters in the method signature
                if (parameterCount >= parameters.size()) {
                    throw new DefinitionBuildingException("unequal amount of path parameters and parameter definitions");
                }

                // Mandatory parameters must not come after the first optional parameter
                final ParameterDefinition parameter = parameters.get(parameterCount);
                if (optionalsIntroduced && !parameter.isOptional()) {
                    throw new DefinitionBuildingException("declaration of mandatory parameters after optional parameters");
                } else if (parameter.isOptional()) {
                    optionalsIntroduced = true;
                }

                parts.add(new ParameterPart(parameter.isOptional(), parameter.getDefaultValue().orElse(null), parameter.getType()));
                parameterCount++;
                continue;
            }

            // Literals must not come after the first optional parameter
            if (optionalsIntroduced) {
                throw new DefinitionBuildingException("declaration of literals after optional parameters");
            }

            parts.add(new LiteralPart(rawPart));
        }

        return new PathDefinition(parts);
    }

    public List<PathPart> getParts() {
        return this.parts;
    }

}
