package pm.lus.spaceship.source;

import pm.lus.spaceship.endpoint.Controller;
import pm.lus.spaceship.endpoint.annotation.path.Get;
import pm.lus.spaceship.endpoint.annotation.path.Patch;
import pm.lus.spaceship.endpoint.annotation.path.Post;
import pm.lus.spaceship.middleware.Middleware;
import pm.lus.spaceship.request.context.RequestContext;

public class ControllerOne implements Controller {

    @Get("/testPath")
    public void handleGetTestPath(final RequestContext ctx) {
    }

    @Get("/testPath")
    @Post("/testPath")
    public void handlePostTestPath(final RequestContext ctx) {
    }

    @Patch("/testPath")
    public void illegalEndpoint() {
    }

    public static class NestedMiddleware implements Middleware {

        @Override
        public void apply(final RequestContext context) {
        }

    }

}
