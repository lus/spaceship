package pm.lus.spaceship.routing;

/**
 * Represents the settings that apply to the {@link Router}
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class RouterSettings {

    private final boolean ignoreTrailingSlashes;
    private final boolean ignoreStackedSlashes;
    private final boolean ignoreCase;

    public RouterSettings(final boolean ignoreTrailingSlashes, final boolean ignoreStackedSlashes, final boolean ignoreCase) {
        this.ignoreTrailingSlashes = ignoreTrailingSlashes;
        this.ignoreStackedSlashes = ignoreStackedSlashes;
        this.ignoreCase = ignoreCase;
    }

    public boolean doIgnoreTrailingSlashes() {
        return this.ignoreTrailingSlashes;
    }

    public boolean doIgnoreStackedSlashes() {
        return this.ignoreStackedSlashes;
    }

    public boolean doIgnoreCase() {
        return this.ignoreCase;
    }

}
