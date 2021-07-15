package pm.lus.spaceship.discovery;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiscoveryTests {

    @Test
    public void correctClassNamesInPackage() {
        final Explorer explorer = Discovery.packageDiscovery("pm.lus.spaceship.source");

        // Test for middleware class names
        final List<String> expectedMiddlewareClassNames = Arrays.asList(
                "MiddlewareOne",
                "NestedMiddleware"
        );
        final List<String> actualMiddlewareClassNames = explorer.findMiddlewares().stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
        Assertions.assertEquals(actualMiddlewareClassNames.size(), expectedMiddlewareClassNames.size());
        Assertions.assertTrue(actualMiddlewareClassNames.containsAll(expectedMiddlewareClassNames));

        // Test for controller class names
        final List<String> expectedControllerClassNames = Arrays.asList(
                "ControllerOne",
                "ControllerTwo",
                "NestedController"
        );
        final List<String> actualControllerClassNames = explorer.findControllers().stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
        Assertions.assertEquals(actualControllerClassNames.size(), expectedControllerClassNames.size());
        Assertions.assertTrue(actualControllerClassNames.containsAll(expectedControllerClassNames));
    }

}
