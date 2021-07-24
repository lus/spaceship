package pm.lus.spaceship.app.config;

import pm.lus.spaceship.controller.Controller;
import pm.lus.spaceship.discovery.Explorer;
import pm.lus.spaceship.middleware.Middleware;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helps configuring the endpoint and middleware discovery by wrapping it using the builder state
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class DiscoveryConfig implements Explorer {

    private final Set<Explorer> explorers;

    public DiscoveryConfig() {
        this.explorers = new HashSet<>();
    }

    /**
     * Adds a new explorer to the discovery stack
     *
     * @param explorer The explorer to add
     * @return The new builder state
     */
    public DiscoveryConfig add(final Explorer explorer) {
        this.explorers.add(explorer);
        return this;
    }

    /**
     * Removes an explorer from the discovery stack
     *
     * @param explorer The explorer to remove
     * @return The new builder state
     */
    public DiscoveryConfig remove(final Explorer explorer) {
        this.explorers.remove(explorer);
        return this;
    }

    @Override
    public Set<Class<? extends Middleware>> findMiddlewares() {
        return this.explorers.stream()
                .map(Explorer::findMiddlewares)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Class<? extends Controller>> findControllers() {
        return this.explorers.stream()
                .map(Explorer::findControllers)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

}
