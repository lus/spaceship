package pm.lus.spaceship.app.config;

import pm.lus.spaceship.routing.RouterSettings;

/**
 * Helps configuring the {@link RouterSettings} by wrapping it using the builder state
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class RouterConfig {

    private boolean ignoreTrailingSlashes;
    private boolean ignoreStackedSlashes;
    private boolean ignoreCase;

    public RouterConfig() {
        this.ignoreTrailingSlashes = true;
        this.ignoreStackedSlashes = true;
        this.ignoreCase = true;
    }

    /**
     * Strictly handle trailing slashes ('/some/route' != '/some/route/')
     *
     * @return The new builder state
     */
    public RouterConfig strictTrailingSlashes() {
        this.ignoreTrailingSlashes = false;
        return this;
    }

    /**
     * Ignore trailing slashes ('/some/route' = '/some/route/')
     *
     * @return The new builder state
     */
    public RouterConfig ignoreTrailingSlashes() {
        this.ignoreTrailingSlashes = true;
        return this;
    }

    /**
     * Strictly handle stacked slashes ('/some/route' != '/some//route')
     *
     * @return The new builder state
     */
    public RouterConfig strictStackedSlashes() {
        this.ignoreStackedSlashes = false;
        return this;
    }

    /**
     * Ignore stacked slashes ('/some/route' = '/some//route')
     *
     * @return The new builder state
     */
    public RouterConfig ignoreStackedSlashes() {
        this.ignoreStackedSlashes = true;
        return this;
    }

    /**
     * Strictly handle case of literals ('/some/route' != '/SOME/route/')
     *
     * @return The new builder state
     */
    public RouterConfig strictCase() {
        this.ignoreCase = false;
        return this;
    }

    /**
     * Ignore case of literals ('/some/route' = '/SOME/route/')
     *
     * @return The new builder state
     */
    public RouterConfig ignoreCase() {
        this.ignoreCase = true;
        return this;
    }

    public RouterSettings getRouterSettings() {
        return new RouterSettings(
                this.ignoreTrailingSlashes,
                this.ignoreStackedSlashes,
                this.ignoreCase
        );
    }

}
