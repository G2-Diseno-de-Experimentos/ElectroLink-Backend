package com.hampcoders.electrolink.sdp.integration;

import io.karatelabs.core.Runner;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Explicit real integration, never a mock or a successful skip if the API is unavailable. */
class SdpKarateIT {
    static final String[] FEATURES = {
            "classpath:com/hampcoders/electrolink/sdp/application/internal/request.feature",
            "classpath:com/hampcoders/electrolink/sdp/application/internal/schedules.feature",
            "classpath:com/hampcoders/electrolink/sdp/application/internal/services.feature"
    };

    @Test
    void sdpRunsAgainstRealLocalBackend() throws Exception {
        var api = new SdpApiSupport();
        try {
            var session = api.authenticate();
            var result = Runner.path(FEATURES)
                    .systemProperty("sdp.baseUrl", api.baseUrl())
                    .global("sdpSession", session).global("sdpApi", api)
                    .outputDir("target/karate-sdp-real").outputHtmlReport(true).parallel(1);
            assertEquals(3, result.getFeatureCount());
            assertEquals(9, result.getScenarioCount());
            assertTrue(result.isPassed(), "Fallaron escenarios SDP; revisar target/karate-sdp-real");
        } finally { api.cleanup(); }
    }
}
