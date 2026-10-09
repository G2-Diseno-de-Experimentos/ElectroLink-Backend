@sdp
Feature: Gestión del catálogo de servicios SDP contra el backend real

  Background:
    * url karate.properties['sdp.baseUrl']
    * configure headers = { Authorization: '#("Bearer " + sdpSession.techJwt)' }

  Scenario: Registrar un servicio y encontrarlo por id y en el catálogo
    * def created = karate.fromString(sdpApi.createService())
    Given path 'api/v1/services', created.id
    When method get
    Then status 200
    And match response contains { id: '#(created.id)', name: '#(created.payload.name)', basePrice: 150 }
    Given path 'api/v1/services'
    When method get
    Then status 200
    And match response contains deep { name: '#(created.payload.name)' }

  Scenario: Actualizar un servicio y comprobar que fue eliminado
    * def created = karate.fromString(sdpApi.createService())
    * def update = created.payload
    * set update.name = update.name + '-Updated'
    * set update.basePrice = 200
    Given path 'api/v1/services', created.id
    And request update
    When method put
    Then status 200
    Given path 'api/v1/services', created.id
    When method get
    Then status 200
    And match response contains { id: '#(created.id)', name: '#(update.name)', basePrice: 200 }
    Given path 'api/v1/services', created.id
    When method delete
    Then status 200
    * eval sdpApi.markDeleted('/api/v1/services/' + created.id)
    Given path 'api/v1/services', created.id
    When method get
    Then status 404

  Scenario: Rechazar la consulta del catálogo sin autenticación
    * configure headers = {}
    Given path 'api/v1/services'
    When method get
    Then status 401
