Feature: Properties API integration contract

  Background:
    * url karate.properties['assets.baseUrl']
    * def existingPropertyId = '11111111-1111-1111-1111-111111111111'
    * def unknownPropertyId = '99999999-9999-9999-9999-999999999999'

  Scenario: List all properties
    Given path '/api/v1/properties'
    When method get
    Then status 200
    And match response == '#[2]'
    And match each response contains { id: '#uuid', ownerId: '#string', address: '#object', region: '#object', district: '#object' }

  Scenario: List properties by owner
    Given path '/api/v1/properties/owner/10'
    When method get
    Then status 200
    And match response == '#[1]'
    And match each response contains { ownerId: '10' }

  Scenario: Get an existing property by id
    Given path '/api/v1/properties', existingPropertyId
    When method get
    Then status 200
    And match response contains { id: '#(existingPropertyId)', ownerId: '10' }
    And match response.address contains { street: 'Calle Los Pinos', number: '123' }

  Scenario: Return not found for an unknown property
    Given path '/api/v1/properties', unknownPropertyId
    When method get
    Then status 404

  Scenario: Create a property with valid data
    * def payload =
      """
      {
        "ownerId": "30",
        "address": {
          "street": "Av. Arequipa",
          "number": "789",
          "city": "Lima",
          "postalCode": "15046",
          "country": "Peru",
          "latitude": -12.092,
          "longitude": -77.035
        },
        "region": "Lima",
        "district": "Lince"
      }
      """
    Given path '/api/v1/properties'
    And request payload
    When method post
    Then status 201
    And match response.id == '#uuid'
    And match response.ownerId == payload.ownerId
    And match response.address == payload.address
    And match response.region.name == payload.region
    And match response.district.name == payload.district

  Scenario: Reject a property with missing required data
    Given path '/api/v1/properties'
    And request { ownerId: '', region: '', district: '' }
    When method post
    Then status 400
    And match response contains { error: 'Invalid property data' }

  Scenario: Update an existing property
    * def payload =
      """
      {
        "address": {
          "street": "Av. Javier Prado",
          "number": "456",
          "city": "Lima",
          "postalCode": "15036",
          "country": "Peru",
          "latitude": -12.091,
          "longitude": -77.026
        },
        "region": "Lima",
        "district": "San Isidro"
      }
      """
    Given path '/api/v1/properties', existingPropertyId
    And request payload
    When method put
    Then status 200
    And match response.id == existingPropertyId
    And match response.address == payload.address
    And match response.region.name == payload.region
    And match response.district.name == payload.district

  Scenario: Return not found when updating an unknown property
    Given path '/api/v1/properties', unknownPropertyId
    And request { address: { street: 'Av. Arequipa', number: '1', city: 'Lima', postalCode: '15046', country: 'Peru', latitude: -12.092, longitude: -77.035 }, region: 'Lima', district: 'Lince' }
    When method put
    Then status 404

  Scenario: Delete an existing property
    Given path '/api/v1/properties', existingPropertyId
    When method delete
    Then status 204

  Scenario: Return not found when deleting an unknown property
    Given path '/api/v1/properties', unknownPropertyId
    When method delete
    Then status 404
