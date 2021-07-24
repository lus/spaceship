package pm.lus.spaceship.request.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a simple key-value data container used for local values in a {@link RequestContext}
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class DataContainer {

    private final Map<String, Object> data;

    public DataContainer() {
        this.data = new HashMap<>();
    }

    public void set(final String key, final Object value) {
        this.data.put(key, value);
    }

    public <T> Optional<T> get(final String key, final Class<? extends T> type) {
        final Object value = this.data.get(key);
        if (value == null) {
            return Optional.empty();
        }

        if (type.isAssignableFrom(value.getClass())) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    public void setByte(final String key, final Byte value) {
        this.set(key, value);
    }

    public Optional<Byte> getByte(final String key) {
        return this.get(key, Byte.class);
    }

    public void setShort(final String key, final Short value) {
        this.set(key, value);
    }

    public Optional<Short> getShort(final String key) {
        return this.get(key, Short.class);
    }

    public void setInt(final String key, final Integer value) {
        this.set(key, value);
    }

    public Optional<Integer> getInt(final String key) {
        return this.get(key, Integer.class);
    }

    public void setLong(final String key, final Long value) {
        this.set(key, value);
    }

    public Optional<Long> getLong(final String key) {
        return this.get(key, Long.class);
    }

    public void setFloat(final String key, final Float value) {
        this.set(key, value);
    }

    public Optional<Float> getFloat(final String key) {
        return this.get(key, Float.class);
    }

    public void setDouble(final String key, final Double value) {
        this.set(key, value);
    }

    public Optional<Double> getDouble(final String key) {
        return this.get(key, Double.class);
    }

    public void setBoolean(final String key, final Boolean value) {
        this.set(key, value);
    }

    public Optional<Boolean> getBoolean(final String key) {
        return this.get(key, Boolean.class);
    }

    public void setChar(final String key, final Character value) {
        this.set(key, value);
    }

    public Optional<Character> getChar(final String key) {
        return this.get(key, Character.class);
    }

    public void setString(final String key, final String value) {
        this.set(key, value);
    }

    public Optional<String> getString(final String key) {
        return this.get(key, String.class);
    }

}
