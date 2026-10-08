package com.hampcoders.electrolink.assets.integration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.karatelabs.core.Runner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetsKarateTest {

    private static final String FEATURES_PATH = "classpath:com/hampcoders/electrolink/assets/integration/";
    private static final String EXISTING_PROPERTY_ID = "11111111-1111-1111-1111-111111111111";
    private static HttpServer mockServer;
    private static String mockBaseUrl;

    @BeforeAll
    static void startMockServer() throws IOException {
        mockServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        mockServer.createContext("/", AssetsKarateTest::handleRequest);
        mockServer.start();
        mockBaseUrl = "http://localhost:" + mockServer.getAddress().getPort();
    }

    @AfterAll
    static void stopMockServer() {
        if (mockServer != null) {
            mockServer.stop(0);
        }
    }

    @Test
    @DisplayName("Los features de Properties y Component Types tienen sintaxis Karate válida")
    void featureFilesAreValid() {
        // Arrange
        var runner = Runner.path(
                FEATURES_PATH + "properties.feature",
                FEATURES_PATH + "component-types.feature"
        ).dryRun(true);

        // Act
        var result = runner.parallel(1);

        // Assert
        assertEquals(2, result.getFeatureCount());
        assertEquals(13, result.getScenarioCount());
        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("Ejecuta los escenarios Karate de Assets contra un mock HTTP local")
    void assetsApiContractRunsAgainstLocalMock() {
        // Arrange
        var runner = Runner.path(
                FEATURES_PATH + "properties.feature",
                FEATURES_PATH + "component-types.feature"
                )
                .systemProperty("assets.baseUrl", mockBaseUrl)
                .outputHtmlReport(true);

        // Act
        var result = runner.parallel(1);

        // Assert
        assertEquals(2, result.getFeatureCount());
        assertEquals(13, result.getScenarioCount());
        assertTrue(result.isPassed(), "Fallaron los escenarios Karate del bounded context Assets");
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        var method = exchange.getRequestMethod();
        var path = exchange.getRequestURI().getPath();
        var requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        if (method.equals("GET") && path.equals("/api/v1/properties")) {
            sendJson(exchange, 200, "[" + existingPropertyJson() + "," + secondPropertyJson() + "]");
            return;
        }
        if (method.equals("GET") && path.equals("/api/v1/properties/owner/10")) {
            sendJson(exchange, 200, "[" + existingPropertyJson() + "]");
            return;
        }
        if (method.equals("GET") && path.equals("/api/v1/properties/" + EXISTING_PROPERTY_ID)) {
            sendJson(exchange, 200, existingPropertyJson());
            return;
        }
        if (method.equals("GET") && path.startsWith("/api/v1/properties/")) {
            sendEmpty(exchange, 404);
            return;
        }
        if (method.equals("POST") && path.equals("/api/v1/properties")) {
            if (!requestBody.contains("\"address\"")) {
                sendJson(exchange, 400, "{\"error\":\"Invalid property data\"}");
                return;
            }
            sendJson(exchange, 201, createdPropertyJson());
            return;
        }
        if (method.equals("PUT") && path.equals("/api/v1/properties/" + EXISTING_PROPERTY_ID)) {
            sendJson(exchange, 200, updatedPropertyJson());
            return;
        }
        if (method.equals("PUT") && path.startsWith("/api/v1/properties/")) {
            sendEmpty(exchange, 404);
            return;
        }
        if (method.equals("DELETE") && path.equals("/api/v1/properties/" + EXISTING_PROPERTY_ID)) {
            sendEmpty(exchange, 204);
            return;
        }
        if (method.equals("DELETE") && path.startsWith("/api/v1/properties/")) {
            sendEmpty(exchange, 404);
            return;
        }
        if (method.equals("GET") && path.equals("/api/v1/component-types")) {
            sendJson(exchange, 200, """
                    [
                      {"componentTypeId":1,"name":"Interruptor","description":"Protege el circuito eléctrico"},
                      {"componentTypeId":2,"name":"Cable","description":"Conductor eléctrico"}
                    ]
                    """);
            return;
        }
        if (method.equals("POST") && path.equals("/api/v1/component-types")) {
            if (!requestBody.contains("Tomacorriente")) {
                sendJson(exchange, 400, "{\"error\":\"Invalid component type data\"}");
                return;
            }
            sendJson(exchange, 201, """
                    {"componentTypeId":3,"name":"Tomacorriente","description":"Punto de conexión eléctrica"}
                    """);
            return;
        }

        sendEmpty(exchange, 404);
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        var bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static void sendEmpty(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
        exchange.close();
    }

    private static String existingPropertyJson() {
        return """
                {"id":"11111111-1111-1111-1111-111111111111","ownerId":"10","address":{"street":"Calle Los Pinos","number":"123","city":"Lima","postalCode":"15074","country":"Peru","latitude":-12.12,"longitude":-77.03},"region":{"name":"Lima"},"district":{"name":"Miraflores"}}
                """;
    }

    private static String secondPropertyJson() {
        return """
                {"id":"22222222-2222-2222-2222-222222222222","ownerId":"20","address":{"street":"Av. Brasil","number":"456","city":"Lima","postalCode":"15083","country":"Peru","latitude":-12.07,"longitude":-77.05},"region":{"name":"Lima"},"district":{"name":"Pueblo Libre"}}
                """;
    }

    private static String createdPropertyJson() {
        return """
                {"id":"33333333-3333-3333-3333-333333333333","ownerId":"30","address":{"street":"Av. Arequipa","number":"789","city":"Lima","postalCode":"15046","country":"Peru","latitude":-12.092,"longitude":-77.035},"region":{"name":"Lima"},"district":{"name":"Lince"}}
                """;
    }

    private static String updatedPropertyJson() {
        return """
                {"id":"11111111-1111-1111-1111-111111111111","ownerId":"10","address":{"street":"Av. Javier Prado","number":"456","city":"Lima","postalCode":"15036","country":"Peru","latitude":-12.091,"longitude":-77.026},"region":{"name":"Lima"},"district":{"name":"San Isidro"}}
                """;
    }
}
