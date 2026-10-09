package com.hampcoders.electrolink.assets.bdd;

import io.cucumber.core.cli.Main;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Run explicitly against the already running real backend: mvnw -Dtest=AssetsCucumberIT test. */
public class AssetsCucumberIT {
    @Test
    void businessScenariosRunAgainstRealBackend() {
        // Cucumber's real runtime executes the Gherkin steps. The exit code is asserted
        // explicitly so a failing scenario cannot produce a false Surefire BUILD SUCCESS.
        byte exitCode = Main.run(new String[]{
                "--glue", "com.hampcoders.electrolink.assets.bdd",
                "--plugin", "pretty",
                "--plugin", "html:target/cucumber-assets/cucumber.html",
                "--plugin", "json:target/cucumber-assets/cucumber.json",
                "--plugin", "junit:target/cucumber-assets/cucumber.xml",
                "--threads", "1",
                "classpath:com/hampcoders/electrolink/assets/bdd"
        }, getClass().getClassLoader());
        assertEquals(0, exitCode, "Fallaron escenarios Cucumber de Assets; revisar target/cucumber-assets");
    }
}
