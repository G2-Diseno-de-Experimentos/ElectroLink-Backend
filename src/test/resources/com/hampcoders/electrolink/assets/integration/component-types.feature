Feature: Component Types API real integration

  Background:
    * url karate.properties['assets.baseUrl']
    * configure headers = { Authorization: '#("Bearer " + assetsSession.jwt)' }
    * configure matchEachEmptyAllowed = true

  Scenario: List real component types without a fixed number of records
    Given path '/api/v1/component-types'
    When method get
    Then status 200
    And match response == '#[]'
    And match each response contains { componentTypeId: '#number', name: '#string', description: '##string' }

  Scenario: Create a uniquely named component type and verify it in the catalog
    * def name = 'Assets-Karate-' + java.util.UUID.randomUUID()
    * def payload = { name: '#(name)', description: 'Tipo creado por pruebas reales de Assets' }
    Given path '/api/v1/component-types'
    And request payload
    When method post
    Then status 201
    And match response contains { componentTypeId: '#number', name: '#(name)', description: '#(payload.description)' }
    * def created = response
    Given path '/api/v1/component-types'
    When method get
    Then status 200
    And match response contains created

  Scenario: Reject malformed JSON without assuming non-existent name validation
    Given path '/api/v1/component-types'
    And header Content-Type = 'application/json'
    And request '{"name":'
    When method post
    Then status 400
