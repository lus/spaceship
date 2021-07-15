package pm.lus.spaceship.routing.endpoint.endpoint;

import pm.lus.spaceship.routing.Router;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a definition of a path of an {@link EndpointDefinition} for the {@link Router} to work with
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class PathDefinition {

    // Matches any string in the format '{}' or '{someText_-}' (parameters)
    private static final Pattern PARAMETER_REGEX = Pattern.compile("\\{[a-zA-Z_-]*}");

    private final String raw;
    private final Pattern pattern;
    private final int parameterCount;

    private PathDefinition(final String raw, final Pattern pattern, final int parameterCount) {
        this.raw = raw;
        this.pattern = pattern;
        this.parameterCount = parameterCount;
    }

    public static PathDefinition build(final String raw) throws IllegalArgumentException {
        String regex = Pattern.quote(raw);

        final Matcher matcher = PARAMETER_REGEX.matcher(regex);
        final int parameterCount = (int) matcher.results().count();
        regex = matcher.replaceAll("\\\\E(.*)\\\\Q");

        final Pattern pattern = Pattern.compile("^" + regex + "$", Pattern.CASE_INSENSITIVE);

        return new PathDefinition(raw, pattern, parameterCount);
    }

    public String getRaw() {
        return this.raw;
    }

    public Pattern getPattern() {
        return this.pattern;
    }

    public int getParameterCount() {
        return this.parameterCount;
    }

}
