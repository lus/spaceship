package pm.lus.spaceship.discovery;

import pm.lus.spaceship.endpoint.Controller;
import pm.lus.spaceship.middleware.Middleware;

import java.util.Set;

/**
 * Represents an explorer that finds middleware and controller classes using an implementation-specific strategy
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public interface Explorer {

    /**
     * @return All found middleware classes in the explored scope
     */
    Set<Class<? extends Middleware>> findMiddlewares();

    /**
     * @return All found controller classes in the explored scope
     */
    Set<Class<? extends Controller>> findControllers();

}
