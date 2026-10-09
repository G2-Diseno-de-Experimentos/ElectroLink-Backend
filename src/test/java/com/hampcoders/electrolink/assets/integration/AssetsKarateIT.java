package com.hampcoders.electrolink.assets.integration;

import com.hampcoders.electrolink.assets.testing.AssetsApiSupport;
import io.karatelabs.core.Runner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/** Explicit real HTTP tests: mvnw -Dtest=AssetsKarateIT test. No mocks or skipped scenarios. */
class AssetsKarateIT {
    static final String[] FEATURES = {
            "classpath:com/hampcoders/electrolink/assets/integration/properties.feature",
            "classpath:com/hampcoders/electrolink/assets/integration/component-types.feature"
    };

    @Test
    @DisplayName("Properties y Component Types funcionan contra Spring Boot y PostgreSQL reales")
    void assetsApiRunsAgainstRealBackend() throws Exception {
        // Arrange: register and authenticate a unique user through the actual API.
        var api = new AssetsApiSupport();
        var session = api.authenticate();
        var runner = Runner.path(FEATURES)
                .systemProperty("assets.baseUrl", api.baseUrl())
                .global("assetsSession", Map.of("jwt", session.token(), "ownerId", session.ownerId()))
                .outputDir("target/karate-assets-real").outputHtmlReport(true);
        // Act: read-after-write checks hit the running backend.
        var result = runner.parallel(1);
        // Assert: unavailable backend or failing scenarios fail, not silently skip.
        assertEquals(2, result.getFeatureCount());
        assertEquals(13, result.getScenarioCount());
        assertTrue(result.isPassed(), "Fallaron escenarios reales de Assets; revisar target/karate-assets-real");
    }
}
