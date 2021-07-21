package pm.lus.spaceship.routing.definition.endpoint.path.parts;

import java.util.Optional;

/**
 * Represents a parameter path part
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class ParameterPart implements PathPart {

    private final boolean optional;
    private final String defaultValue;
    private final Class<?> type;

    public ParameterPart(final boolean optional, final String defaultValue, final Class<?> type) {
        this.optional = optional;
        this.defaultValue = defaultValue;
        this.type = type;
    }

    public boolean isOptional() {
        return this.optional;
    }

    public Optional<String> getDefaultValue() {
        return Optional.ofNullable(this.defaultValue);
    }

    public Class<?> getType() {
        return this.type;
    }

    @Override
    public boolean matches(final PathPart other) {
        if (!(other instanceof ParameterPart)) {
            return false;
        }
        final ParameterPart otherPart = (ParameterPart) other;
        return otherPart.isOptional() == this.optional && otherPart.type == this.type;
    }

}
