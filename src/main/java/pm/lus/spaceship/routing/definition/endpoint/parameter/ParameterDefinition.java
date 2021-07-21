package pm.lus.spaceship.routing.definition.endpoint.parameter;

import pm.lus.spaceship.endpoint.annotation.parameter.OptionalParam;
import pm.lus.spaceship.routing.Router;
import pm.lus.spaceship.routing.definition.DefinitionBuildingException;
import pm.lus.spaceship.routing.definition.endpoint.EndpointDefinition;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a definition of a parameter of an {@link EndpointDefinition} for the {@link Router} to work with
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class ParameterDefinition {

    // We need to map primitives to their corresponding boxed equivalents for generics to properly work (type adapters)
    private static final Map<Class<?>, Class<?>> TYPE_MAPPINGS = new ConcurrentHashMap<>() {
        {
            this.put(byte.class, Byte.class);
            this.put(byte[].class, Byte[].class);
            this.put(short.class, Short.class);
            this.put(short[].class, Short[].class);
            this.put(int.class, Integer.class);
            this.put(int[].class, Integer[].class);
            this.put(long.class, Long.class);
            this.put(long[].class, Long[].class);
            this.put(float.class, Float.class);
            this.put(float[].class, Float[].class);
            this.put(double.class, Double.class);
            this.put(double[].class, Double[].class);
            this.put(boolean.class, Boolean.class);
            this.put(boolean[].class, Boolean[].class);
            this.put(char.class, Character.class);
            this.put(char[].class, Character[].class);
        }
    };

    private final Class<?> type;
    private final boolean optional;
    private final String defaultValue;

    private ParameterDefinition(final Class<?> type, final boolean optional, final String defaultValue) {
        this.type = type;
        this.optional = optional;
        this.defaultValue = defaultValue;
    }

    public static ParameterDefinition build(final Parameter parameter) throws DefinitionBuildingException {
        if (parameter.isVarArgs()) {
            throw new DefinitionBuildingException("VarArgs is not supported for parameters");
        }

        final boolean isOptional = parameter.isAnnotationPresent(OptionalParam.class);

        String defaultValue = null;
        if (isOptional) {
            defaultValue = parameter.getAnnotation(OptionalParam.class).value();
            if (defaultValue.equals("")) {
                defaultValue = null;
            }
        }

        return new ParameterDefinition(
                TYPE_MAPPINGS.getOrDefault(parameter.getType(), parameter.getType()),
                isOptional,
                defaultValue
        );
    }

    public Class<?> getType() {
        return this.type;
    }

    public boolean isOptional() {
        return this.optional;
    }

    public Optional<String> getDefaultValue() {
        return Optional.ofNullable(this.defaultValue);
    }

}
