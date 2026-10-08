package com.hampcoders.electrolink.monitoring;

import io.karatelabs.core.Runner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class MonitoringKarateTest {
  private static final String FEATURES = "com/hampcoders/electrolink/monitoring/";

  @Test
  @DisplayName("Los cuatro features de monitoring contienen escenarios válidos")
  void featureFiles() {
    var result = Runner.path("classpath:" + FEATURES).dryRun(true).parallel(1);
    assertEquals(4, result.getFeatureCount());
    assertEquals(18, result.getScenarioCount());
    assertTrue(result.isPassed());
  }

  @Test
  @DisplayName("Ejecuta los escenarios Karate de monitoring disponibles")
  void monitoringEndpoints() throws Exception {
    for (String feature : new String[] {"service-operations", "reports", "photos", "ratings"}) {
      assertNotNull(getClass().getClassLoader().getResource(FEATURES + feature + ".feature"), "Falta el feature " + feature);
    }

    String baseUrl = System.getProperty("monitoring.baseUrl", System.getenv().getOrDefault("MONITORING_BASE_URL", "http://localhost:8091"));
    try (var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()) {
      var request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/service-operations"))
          .timeout(Duration.ofSeconds(3)).GET().build();
      client.send(request, HttpResponse.BodyHandlers.discarding());
    } catch (Exception unavailable) {
      Assumptions.abort("Backend no disponible en " + baseUrl + ": " + unavailable.getMessage());
    }

    String token = System.getProperty("monitoring.jwt", System.getenv("MONITORING_JWT"));
    var runner = Runner.path("classpath:" + FEATURES).outputHtmlReport(true);
    if (token == null || token.isBlank()) runner.tags("~@auth");
    else if (System.getenv("MONITORING_REQUEST_ID") == null || System.getenv("MONITORING_TECHNICIAN_ID") == null || System.getenv("MONITORING_REPORT_ID") == null) runner.tags("~@seeded");
    var result = runner.parallel(1);
    assertTrue(result.getFeatureCount() > 0 && result.getScenarioCount() > 0, "Karate no encontró escenarios de monitoring");
    assertTrue(result.isPassed(), "Fallaron los escenarios Karate de monitoring");
  }
}
