package com.hampcoders.electrolink.assets.integration;

import io.karatelabs.core.Runner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Syntax only. Real HTTP integration runs explicitly through AssetsKarateIT. */
class AssetsKarateTest {
    @Test
    void featureFilesAreValid() {
        var result = Runner.path(AssetsKarateIT.FEATURES)
                .dryRun(true).outputDir("target/karate-assets-dry-run").parallel(1);
        assertEquals(2, result.getFeatureCount());
        assertEquals(13, result.getScenarioCount());
        assertTrue(result.isPassed());
    }
}
