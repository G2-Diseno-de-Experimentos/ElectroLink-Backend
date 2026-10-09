@sdp
Feature: Gestión de solicitudes SDP contra el backend real

  Background:
    * url karate.properties['sdp.baseUrl']
    * configure headers = { Authorization: '#("Bearer " + sdpSession.jwt)' }

  Scenario: Registrar una solicitud y encontrarla por id y cliente
    * def created = karate.fromString(sdpApi.createRequest())
    Given path 'api/v1/requests', created.id
    When method get
    Then status 200
    And match response contains { id: '#(created.id)', clientId: '#(sdpSession.clientId)', problemDescription: '#(created.payload.problemDescription)' }
    Given path 'api/v1/requests/clients', sdpSession.clientId, 'requests'
    When method get
    Then status 200
    And match response contains deep { id: '#(created.id)' }
    And match each response contains { clientId: '#(sdpSession.clientId)' }

  Scenario: Actualizar y eliminar una solicitud con persistencia comprobada
    * def created = karate.fromString(sdpApi.createRequest())
    * def update = created.payload
    * set update.problemDescription = 'Solicitud actualizada por Karate'
    Given path 'api/v1/requests', created.id
    And request update
    When method put
    Then status 200
    Given path 'api/v1/requests', created.id
    When method get
    Then status 200
    And match response.problemDescription == update.problemDescription
    Given path 'api/v1/requests', created.id
    When method delete
    Then status 200
    * eval sdpApi.markDeleted('/api/v1/requests/' + created.id)
    Given path 'api/v1/requests', created.id
    When method get
    Then status 404

  Scenario: Rechazar la consulta de solicitudes sin autenticación
    * configure headers = {}
    Given path 'api/v1/requests/clients', sdpSession.clientId, 'requests'
    When method get
    Then status 401
