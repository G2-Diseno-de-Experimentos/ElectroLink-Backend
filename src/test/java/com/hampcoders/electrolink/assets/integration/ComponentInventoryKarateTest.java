package com.hampcoders.electrolink.assets.integration;

import io.karatelabs.core.Runner;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentInventoryKarateTest {

  @Test
  @DisplayName("Ejecuta los escenarios Karate de Components y Technician Inventory contra la API real")
  void componentAndInventoryEndpoints() {
    // Arrange
    String baseUrl = System.getProperty("monitoring.baseUrl", System.getenv().getOrDefault("MONITORING_BASE_URL", "http://localhost:8091"));
    try (var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()) {
      var request = HttpRequest.newBuilder(URI.create(baseUrl + "/v3/api-docs"))
          .timeout(Duration.ofSeconds(2)).GET().build();
      client.send(request, HttpResponse.BodyHandlers.discarding());
    } catch (Exception unavailable) {
      Assumptions.abort("Backend no disponible en " + baseUrl + ": " + unavailable.getMessage());
    }

    var runner = Runner.path(
        "classpath:com/hampcoders/electrolink/assets/integration/components.feature",
        "classpath:com/hampcoders/electrolink/assets/integration/technician-inventories.feature"
    ).outputHtmlReport(true);

    // Act
    var result = runner.parallel(5);

    // Assert
    assertEquals(2, result.getFeatureCount());
    assertTrue(result.isPassed(), "Fallaron los escenarios Karate de Components y Technician Inventory");
  }
}
