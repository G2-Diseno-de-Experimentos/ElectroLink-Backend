package com.hampcoders.electrolink.assets.testing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/** Real HTTP client shared by Karate and Cucumber. Never prints credentials or JWTs. */
public final class AssetsApiSupport {
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String baseUrl;

    public AssetsApiSupport() {
        baseUrl = System.getProperty("assets.baseUrl", System.getenv().getOrDefault("ASSETS_BASE_URL", "http://localhost:8091")).replaceAll("/+$", "");
        var uri = URI.create(baseUrl);
        if (!java.util.Set.of("localhost", "127.0.0.1", "::1", "[::1]").contains(uri.getHost()) && !Boolean.getBoolean("assets.allowRemoteWrites")) {
            throw new IllegalArgumentException("Use un backend local de pruebas. Para autorizar escrituras remotas: -Dassets.allowRemoteWrites=true");
        }
    }
    public String baseUrl() { return baseUrl; }
    public record Session(String ownerId, String token) { }

    public Session authenticate() throws Exception {
        var username = "assets-" + UUID.randomUUID().toString().substring(0, 12) + "@test.local";
        var bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        var password = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        var signup = request("POST", "/api/v1/authentication/sign-up", Map.of("username", username, "password", password, "roles", java.util.List.of("ROLE_HOMEOWNER")), null);
        requireStatus(signup, 201, "Registro del usuario de prueba");
        var signin = request("POST", "/api/v1/authentication/sign-in", Map.of("username", username, "password", password), null);
        requireStatus(signin, 200, "Autenticación del usuario de prueba");
        var body = json(signin);
        if (!body.hasNonNull("token") || body.path("token").asText().isBlank() || !body.hasNonNull("id")) throw new AssertionError("Autenticación sin JWT o identificador");
        return new Session(body.path("id").asText(), body.path("token").asText());
    }

    public HttpResponse<String> request(String method, String route, Object body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(baseUrl + route)).timeout(Duration.ofSeconds(30));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        var publisher = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body));
        if (body != null) builder.header("Content-Type", "application/json");
        try { return client.send(builder.method(method, publisher).build(), HttpResponse.BodyHandlers.ofString()); }
        catch (java.io.IOException unavailable) { throw new IllegalStateException("No se pudo conectar al backend real en " + baseUrl + ". Inícielo antes de ejecutar Karate/Cucumber.", unavailable); }
    }
    public JsonNode json(HttpResponse<String> response) throws Exception { return mapper.readTree(response.body()); }
    public static void requireStatus(HttpResponse<String> response, int expected, String action) {
        if (response.statusCode() != expected) throw new AssertionError(action + ": esperado HTTP " + expected + ", obtenido " + response.statusCode());
    }
    public static Map<String, Object> propertyPayload(String ownerId, String district) {
        return Map.of("ownerId", ownerId, "address", Map.of("street", "Calle Assets Test", "number", "123", "city", "Lima", "postalCode", "15074", "country", "Peru", "latitude", 0, "longitude", 0), "region", "Lima", "district", district);
    }
}
