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
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
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

    /**
     * Processes a raw request path on this path definition
     *
     * @param raw                   The raw request path
     * @param ignoreTrailingSlashes Whether or not to ignore trailing slashes
     * @param ignoreStackedSlashes  Whether or not to ignore stacked slashes ('//)
     * @param ignoreCase            Whether or not to ignore case when comparing literals
     * @return The achieved processing result
     */
    public ProcessingResult process(final String raw, final boolean ignoreTrailingSlashes, final boolean ignoreStackedSlashes, final boolean ignoreCase) {
        final List<PathPart> parts = new ArrayList<>();
        for (final String rawPart : raw.split("/")) {
            if (rawPart.isEmpty()) {
                parts.add(new EmptyPart());
                continue;
            }
            parts.add(new LiteralPart(rawPart));
        }
        final List<PathPart> toCompare = new ArrayList<>(this.parts);

        // Remove the potential leading slash out of both lists
        if (parts.size() > 0 && parts.get(0) instanceof EmptyPart) {
            parts.remove(0);
        }
        if (toCompare.size() > 0 && toCompare.get(0) instanceof EmptyPart) {
            toCompare.remove(0);
        }

        // Both paths match the root path ('/')
        if (parts.isEmpty() && toCompare.isEmpty()) {
            return ProcessingResult.EMPTY_MATCHING;
        }

        // Remove any empty path parts out of the part lists if stacked slashes should be ignored
        if (ignoreStackedSlashes) {
            for (final ListIterator<PathPart> iterator = parts.listIterator(); iterator.hasNext(); ) {
                if (iterator.nextIndex() < parts.size() - 1 & iterator.next() instanceof EmptyPart) {
                    iterator.remove();
                }
            }

            for (final ListIterator<PathPart> iterator = toCompare.listIterator(); iterator.hasNext(); ) {
                if (iterator.nextIndex() < toCompare.size() - 1 & iterator.next() instanceof EmptyPart) {
                    iterator.remove();
                }
            }
        }

        // Remove any trailing empty path parts out of the part lists if trailing slashes should be ignored
        if (ignoreTrailingSlashes) {
            for (final ListIterator<PathPart> iterator = parts.listIterator(parts.size()); iterator.hasPrevious(); ) {
                if (iterator.previous() instanceof EmptyPart) {
                    iterator.remove();
                } else {
                    break;
                }
            }

            for (final ListIterator<PathPart> iterator = toCompare.listIterator(toCompare.size()); iterator.hasPrevious(); ) {
                if (iterator.previous() instanceof EmptyPart) {
                    iterator.remove();
                } else {
                    break;
                }
            }
        }

        // The request path must not be longer than the path to compare it to at this point
        if (parts.size() > toCompare.size()) {
            return ProcessingResult.NOT_MATCHING;
        }

        // Compare the part lists and extract parameters
        final List<String> parameters = new ArrayList<>();
        for (int i = 0; i < toCompare.size(); i++) {
            final PathPart part = parts.size() <= i ? null : parts.get(i);
            final PathPart partToCompare = toCompare.get(i);

            // The paths don't match if a literal or mandatory parameter part is not given
            if (part == null && (!(partToCompare instanceof ParameterPart) || !((ParameterPart) partToCompare).isOptional())) {
                return ProcessingResult.NOT_MATCHING;
            } else if (part == null) {
                final ParameterPart parameterPart = (ParameterPart) partToCompare;
                if (parameterPart.getDefaultValue().isPresent()) {
                    parameters.add(parameterPart.getDefaultValue().get());
                }
                break;
            }

            // Check if the parts match on a type-specific basis
            if (partToCompare instanceof EmptyPart && !partToCompare.matches(part)) {
                return ProcessingResult.NOT_MATCHING;
            } else if (partToCompare instanceof LiteralPart) {
                final LiteralPart literalPart = (LiteralPart) partToCompare;
                final boolean matches = ignoreCase
                        ? literalPart.matchesCaseInsensitive(part)
                        : literalPart.matches(part);
                if (!matches) {
                    return ProcessingResult.NOT_MATCHING;
                }
            } else if (partToCompare instanceof ParameterPart) {
                final ParameterPart parameterPart = (ParameterPart) partToCompare;
                if (!parameterPart.isOptional() && !(part instanceof LiteralPart)) {
                    return ProcessingResult.NOT_MATCHING;
                } else if (part instanceof LiteralPart) {
                    parameters.add(((LiteralPart) part).getContent());
                }
            }
        }

        return new ProcessingResult(true, parameters);
    }

    /**
     * Represents a simple result the {@link PathDefinition#process(String, boolean, boolean, boolean)} method achieves
     *
     * @author Lukas Schulte Pelkum
     * @version 0.1.0
     * @since 0.1.0
     */
    public static class ProcessingResult {

        private static final ProcessingResult EMPTY_MATCHING = new ProcessingResult(true, Collections.emptyList());
        private static final ProcessingResult NOT_MATCHING = new ProcessingResult(false, Collections.emptyList());

        private final boolean matches;
        private final List<String> parameters;

        private ProcessingResult(final boolean matches, final List<String> parameters) {
            this.matches = matches;
            this.parameters = parameters;
        }

        public boolean matches() {
            return this.matches;
        }

        public List<String> getParameters() {
            return this.parameters;
        }

    }

}
