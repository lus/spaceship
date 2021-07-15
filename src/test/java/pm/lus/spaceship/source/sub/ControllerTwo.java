package pm.lus.spaceship.source.sub;

import pm.lus.spaceship.endpoint.Controller;
import pm.lus.spaceship.endpoint.annotation.ControllerOptions;
import pm.lus.spaceship.endpoint.annotation.path.Get;
import pm.lus.spaceship.endpoint.annotation.path.Path;
import pm.lus.spaceship.request.RequestMethod;
import pm.lus.spaceship.request.context.RequestContext;

@ControllerOptions(basePath = "/basePath")
public class ControllerTwo implements Controller {

    @Path(value = "/subPath", methods = {RequestMethod.DELETE, RequestMethod.OPTIONS})
    public void handleSubPath(final RequestContext ctx) {
    }

    public static class NestedController implements Controller {

        @Get("/rootPath")
        public void handleRootPath(final RequestContext ctx) {
        }

    }

}
