package com.hampcoders.electrolink.sdp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hampcoders.electrolink.assets.testing.AssetsApiSupport;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Real HTTP fixtures, unique identifiers and cleanup limited to this suite's data. */
public final class SdpApiSupport {
    private final AssetsApiSupport api = new AssetsApiSupport("sdp", "SDP");
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Created> created = new ArrayList<>();
    private AssetsApiSupport.Session owner;
    private AssetsApiSupport.Session technician;
    private record Created(String route, int deleteStatus) { }

    public String baseUrl() { return api.baseUrl(); }

    public Map<String, String> authenticate() throws Exception {
        owner = api.authenticate();
        technician = api.authenticate("ROLE_TECHNICIAN");
        return Map.of("jwt", owner.token(), "clientId", owner.ownerId(),
                "techJwt", technician.token(), "technicianId", technician.ownerId());
    }

    public String servicePayload() throws Exception {
        return mapper.writeValueAsString(Map.ofEntries(
                Map.entry("name", "SDP-Karate-" + UUID.randomUUID()),
                Map.entry("description", "Servicio de pruebas locales"), Map.entry("basePrice", 150),
                Map.entry("estimatedTime", "2 horas"), Map.entry("category", "Mantenimiento"),
                Map.entry("isVisible", true), Map.entry("createdBy", technician.ownerId()),
                Map.entry("policy", Map.of("cancellationPolicy", "Prueba", "termsAndConditions", "Solo pruebas")),
                Map.entry("restriction", Map.of("unavailableDistricts", List.of(), "forbiddenDays", List.of(), "requiresSpecialCertification", false)),
                Map.entry("tags", List.of()), Map.entry("components", List.of())));
    }

    public String createService() throws Exception {
        var payload = mapper.readTree(servicePayload());
        var response = api.request("POST", "/api/v1/services", payload, technician.token());
        AssetsApiSupport.requireStatus(response, 200, "Preparar servicio real");
        long id = api.json(response).asLong();
        if (id <= 0) throw new AssertionError("Servicio sin id válido");
        created.add(new Created("/api/v1/services/" + id, 200));
        return mapper.writeValueAsString(Map.of("id", id, "payload", payload));
    }

    public String createSchedule() throws Exception {
        var payload = Map.of("technicianId", technician.ownerId(), "day", "MONDAY", "startTime", "08:00", "endTime", "12:00");
        var response = api.request("POST", "/api/v1/schedules", payload, technician.token());
        AssetsApiSupport.requireStatus(response, 200, "Preparar horario real");
        long id = api.json(response).asLong();
        if (id <= 0) throw new AssertionError("Horario sin id válido");
        created.add(new Created("/api/v1/schedules/" + id, 200));
        return mapper.writeValueAsString(Map.of("id", id, "payload", payload));
    }

    public String createRequest() throws Exception {
        var service = mapper.readTree(createService());
        var property = api.request("POST", "/api/v1/properties", AssetsApiSupport.propertyPayload(owner.ownerId(), "Miraflores"), owner.token());
        AssetsApiSupport.requireStatus(property, 201, "Preparar propiedad real");
        String propertyId = api.json(property).path("id").asText();
        UUID.fromString(propertyId);
        created.add(new Created("/api/v1/properties/" + propertyId, 204));
        var payload = Map.of("clientId", owner.ownerId(), "technicianId", technician.ownerId(),
                "propertyId", propertyId, "serviceId", service.path("id").asText(),
                "problemDescription", "SDP-Karate-" + UUID.randomUUID(),
                "scheduledDate", LocalDate.now().plusDays(1).toString(),
                "bill", Map.of("billingPeriod", "2026-10", "energyConsumed", 150, "amountPaid", 200, "billImageUrl", "https://example.com/test-bill.jpg"),
                "photos", List.of());
        var response = api.request("POST", "/api/v1/requests", payload, owner.token());
        AssetsApiSupport.requireStatus(response, 201, "Preparar solicitud real");
        var message = api.json(response).path("message").asText();
        if (!message.matches("Request created with ID: \\d+")) throw new AssertionError("Solicitud sin id reconocido");
        long id = Long.parseLong(message.substring(message.lastIndexOf(' ') + 1));
        created.add(new Created("/api/v1/requests/" + id, 200));
        return mapper.writeValueAsString(Map.of("id", id, "payload", payload));
    }

    public void markDeleted(String route) { created.removeIf(resource -> resource.route().equals(route)); }

    public void cleanup() throws Exception {
        List<String> failures = new ArrayList<>();
        for (int index = created.size() - 1; index >= 0; index--) {
            var resource = created.get(index);
            try {
                var response = api.request("DELETE", resource.route(), null, owner.token());
                if (response.statusCode() != resource.deleteStatus()) failures.add(resource.route() + ": HTTP " + response.statusCode());
            } catch (Exception failure) { failures.add(resource.route() + ": " + failure.getClass().getSimpleName()); }
        }
        created.clear();
        if (!failures.isEmpty()) throw new AssertionError("No se limpiaron recursos de prueba: " + String.join(", ", failures));
    }
}
