package com.hampcoders.electrolink.assets.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hampcoders.electrolink.assets.testing.AssetsApiSupport;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Each scenario has its own state. Steps use real HTTP, never services mocked with Mockito. */
public class AssetsSteps {
    private static AssetsApiSupport.Session sharedSession;
    private AssetsApiSupport api;
    private AssetsApiSupport.Session session;
    private HttpResponse<String> response;
    private String propertyId;
    private Object propertyPayload;
    private JsonNode componentType;
    private String componentName;
    private final List<String> createdProperties = new ArrayList<>();

    @Before
    public void connect(Scenario scenario) throws Exception {
        api = new AssetsApiSupport();
        if (sharedSession == null) sharedSession = api.authenticate();
        session = sharedSession;
        scenario.log("Backend real: " + api.baseUrl() + "; autenticación JWT; repositorios y PostgreSQL reales.");
    }

    @After
    public void cleanOwnProperties() throws Exception {
        for (String id : createdProperties) {
            var cleanup = api.request("DELETE", "/api/v1/properties/" + id, null, session.token());
            assertTrue(cleanup.statusCode() == 204 || cleanup.statusCode() == 404, "No se pudo limpiar la propiedad de prueba " + id);
        }
    }

    @Given("un propietario autenticado")
    public void authenticatedOwner() { assertNotNull(session.token()); }

    @Given("los datos válidos de una propiedad en {string}")
    public void validProperty(String district) { propertyPayload = AssetsApiSupport.propertyPayload(session.ownerId(), district); }

    @Given("una propiedad registrada por el propietario")
    public void existingProperty() throws Exception { propertyId = createProperty(session.ownerId()); }

    @Given("propiedades registradas por dos propietarios diferentes")
    public void twoOwners() throws Exception {
        propertyId = createProperty(session.ownerId());
        var other = api.authenticate();
        createProperty(other.ownerId());
    }

    private String createProperty(String ownerId) throws Exception {
        var created = api.request("POST", "/api/v1/properties", AssetsApiSupport.propertyPayload(ownerId, "Miraflores"), session.token());
        AssetsApiSupport.requireStatus(created, 201, "Preparación de propiedad real");
        var id = api.json(created).path("id").asText();
        UUID.fromString(id);
        createdProperties.add(id);
        return id;
    }

    @When("registra la propiedad")
    public void registerProperty() throws Exception {
        response = api.request("POST", "/api/v1/properties", propertyPayload, session.token());
        if (response.statusCode() == 201) {
            propertyId = api.json(response).path("id").asText();
            createdProperties.add(propertyId);
        }
    }

    @Then("la propiedad queda registrada y puede consultarse por su identificador")
    public void persistedProperty() throws Exception {
        AssetsApiSupport.requireStatus(response, 201, "Registro");
        UUID.fromString(propertyId);
        var retrieved = api.request("GET", "/api/v1/properties/" + propertyId, null, session.token());
        AssetsApiSupport.requireStatus(retrieved, 200, "Consulta posterior al registro");
        assertEquals(api.json(response), api.json(retrieved));
        assertEquals(session.ownerId(), api.json(retrieved).path("ownerId").asText());
    }

    @When("cambia el distrito de su propiedad a {string}")
    public void updateDistrict(String district) throws Exception {
        response = api.request("PUT", "/api/v1/properties/" + propertyId, AssetsApiSupport.propertyPayload(session.ownerId(), district), session.token());
    }

    @Then("al consultar la propiedad su distrito es {string}")
    public void savedDistrict(String district) throws Exception {
        AssetsApiSupport.requireStatus(response, 200, "Actualización");
        var retrieved = api.request("GET", "/api/v1/properties/" + propertyId, null, session.token());
        AssetsApiSupport.requireStatus(retrieved, 200, "Consulta posterior a la actualización");
        assertEquals(district, api.json(retrieved).path("district").path("name").asText());
    }

    @When("consulta las propiedades de su propietario")
    public void byOwner() throws Exception { response = api.request("GET", "/api/v1/properties/owner/" + session.ownerId(), null, session.token()); }

    @Then("la lista contiene su propiedad y no incluye las del otro propietario")
    public void ownerFilter() throws Exception {
        AssetsApiSupport.requireStatus(response, 200, "Filtro por propietario");
        var list = api.json(response);
        assertTrue(list.isArray());
        boolean containsOwn = false;
        for (var item : list) {
            assertEquals(session.ownerId(), item.path("ownerId").asText());
            containsOwn |= item.path("id").asText().equals(propertyId);
        }
        assertTrue(containsOwn, "El filtro debe incluir la propiedad preparada en este escenario");
    }

    @When("elimina su propiedad")
    public void deleteProperty() throws Exception { response = api.request("DELETE", "/api/v1/properties/" + propertyId, null, session.token()); }

    @Then("la propiedad ya no puede consultarse")
    public void propertyGone() throws Exception {
        AssetsApiSupport.requireStatus(response, 204, "Eliminación");
        var retrieved = api.request("GET", "/api/v1/properties/" + propertyId, null, session.token());
        AssetsApiSupport.requireStatus(retrieved, 404, "La eliminación debe ser efectiva en PostgreSQL");
    }

    @Given("los datos de una propiedad sin el campo obligatorio {string}")
    public void missingField(String field) {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        ObjectNode payload = mapper.valueToTree(AssetsApiSupport.propertyPayload(session.ownerId(), "Miraflores"));
        payload.remove(field);
        propertyPayload = payload;
    }

    @Then("el sistema rechaza el registro con código {int}")
    public void rejected(int expected) { AssetsApiSupport.requireStatus(response, expected, "Validación de datos obligatorios"); }

    @When("consulta una propiedad que no existe")
    public void unknownProperty() throws Exception { response = api.request("GET", "/api/v1/properties/" + UUID.randomUUID(), null, session.token()); }

    @Then("el sistema informa que la propiedad no fue encontrada")
    public void notFound() { AssetsApiSupport.requireStatus(response, 404, "Propiedad inexistente"); }

    @Given("un tipo de componente nuevo con nombre único")
    public void newType() { componentName = "Assets-BDD-" + UUID.randomUUID(); }

    @When("registra el tipo de componente")
    public void registerType() throws Exception {
        response = api.request("POST", "/api/v1/component-types", Map.of("name", componentName, "description", "Tipo creado por BDD real"), session.token());
        if (response.statusCode() == 201) componentType = api.json(response);
    }

    @Then("el tipo queda disponible en el catálogo con el mismo nombre")
    public void typeInCatalog() throws Exception {
        AssetsApiSupport.requireStatus(response, 201, "Registro del tipo");
        assertTrue(componentType.path("componentTypeId").asLong() > 0);
        var list = api.request("GET", "/api/v1/component-types", null, session.token());
        AssetsApiSupport.requireStatus(list, 200, "Consulta posterior al registro del tipo");
        boolean found = false;
        for (var item : api.json(list)) if (item.path("componentTypeId").equals(componentType.path("componentTypeId"))) { assertEquals(componentName, item.path("name").asText()); found = true; }
        assertTrue(found);
    }

    @When("consulta el catálogo de tipos de componentes")
    public void catalog() throws Exception { response = api.request("GET", "/api/v1/component-types", null, session.token()); }

    @Then("recibe una lista de tipos con identificador y nombre")
    public void catalogStructure() throws Exception {
        AssetsApiSupport.requireStatus(response, 200, "Catálogo");
        var list = api.json(response);
        assertTrue(list.isArray());
        for (var item : list) { assertTrue(item.path("componentTypeId").asLong() > 0); assertTrue(item.path("name").isTextual()); }
    }

    @When("intenta consultar el catálogo sin enviar su autenticación")
    public void anonymousCatalog() throws Exception { response = api.request("GET", "/api/v1/component-types", null, null); }

    @Then("el sistema exige autenticación")
    public void authenticationRequired() { AssetsApiSupport.requireStatus(response, 401, "Acceso sin autenticación"); }
}
