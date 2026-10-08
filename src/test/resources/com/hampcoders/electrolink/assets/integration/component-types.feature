Feature: Component Types API integration contract

  Background:
    * url karate.properties['assets.baseUrl']

  Scenario: List all component types
    Given path '/api/v1/component-types'
    When method get
    Then status 200
    And match response == '#[2]'
    And match each response contains { componentTypeId: '#number', name: '#string', description: '#string' }

  Scenario: Create a component type with valid data
    * def payload = { name: 'Tomacorriente', description: 'Punto de conexión eléctrica' }
    Given path '/api/v1/component-types'
    And request payload
    When method post
    Then status 201
    And match response contains { componentTypeId: '#number', name: '#(payload.name)', description: '#(payload.description)' }

  Scenario: Reject a component type with an empty name
    Given path '/api/v1/component-types'
    And request { name: '', description: 'Nombre obligatorio' }
    When method post
    Then status 400
    And match response contains { error: 'Invalid component type data' }
