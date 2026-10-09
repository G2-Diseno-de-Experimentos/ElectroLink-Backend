package com.hampcoders.electrolink.sdp.integration;

import io.karatelabs.core.Runner;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Syntax validation only; its report is never evidence of actual HTTP execution. */
class SdpKarateTest {
    @Test
    void sdpFeatureSyntaxIsValid() {
        var result = Runner.path(SdpKarateIT.FEATURES).dryRun(true)
                .outputDir("target/karate-sdp-dry-run").parallel(1);
        assertEquals(3, result.getFeatureCount());
        assertEquals(9, result.getScenarioCount());
        assertTrue(result.isPassed());
    }
}
