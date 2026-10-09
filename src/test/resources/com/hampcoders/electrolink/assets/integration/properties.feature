Feature: Properties API real integration

  Background:
    * url karate.properties['assets.baseUrl']
    * configure headers = { Authorization: '#("Bearer " + assetsSession.jwt)' }
    * configure matchEachEmptyAllowed = true
    * def unknownId = java.util.UUID.randomUUID() + ''
    * def payload = { ownerId: '#(assetsSession.ownerId)', address: { street: 'Calle Assets Test', number: '123', city: 'Lima', postalCode: '15074', country: 'Peru', latitude: 0, longitude: 0 }, region: 'Lima', district: 'Miraflores' }
    * def createProperty = function(){ return karate.call('classpath:com/hampcoders/electrolink/assets/integration/support/assets-create-property.feature', { payload: payload }); }

  Scenario: List all real properties without assuming a fixed database size
    Given path '/api/v1/properties'
    When method get
    Then status 200
    And match response == '#[]'
    And match each response contains { id: '#uuid', ownerId: '#string', address: '#object', region: '#object', district: '#object' }

  Scenario: List properties by their actual owner
    * def created = createProperty()
    Given path '/api/v1/properties/owner', assetsSession.ownerId
    When method get
    Then status 200
    And match response contains deep { id: '#(created.property.id)' }
    And match each response contains { ownerId: '#(assetsSession.ownerId)' }
    * def cleanup = karate.call('classpath:com/hampcoders/electrolink/assets/integration/support/assets-delete-property.feature', { propertyId: created.property.id })

  Scenario: Retrieve an existing property using its generated UUID
    * def created = createProperty()
    Given path '/api/v1/properties', created.property.id
    When method get
    Then status 200
    And match response == created.property
    * def cleanup = karate.call('classpath:com/hampcoders/electrolink/assets/integration/support/assets-delete-property.feature', { propertyId: created.property.id })

  Scenario: Return not found for an unknown UUID
    Given path '/api/v1/properties', unknownId
    When method get
    Then status 404

  Scenario: Create a valid property and confirm that it is persisted
    Given path '/api/v1/properties'
    And request payload
    When method post
    Then status 201
    And match response contains { id: '#uuid', ownerId: '#(payload.ownerId)' }
    And match response.address == payload.address
    And match response.region.name == payload.region
    And match response.district.name == payload.district
    * def created = response
    Given path '/api/v1/properties', created.id
    When method get
    Then status 200
    And match response == created
    * def cleanup = karate.call('classpath:com/hampcoders/electrolink/assets/integration/support/assets-delete-property.feature', { propertyId: created.id })

  Scenario: Reject missing mandatory property data in the real API
    Given path '/api/v1/properties'
    And request { ownerId: '', region: '', district: '' }
    When method post
    Then status 400

  Scenario: Update an existing property and verify the saved changes
    * def created = createProperty()
    * def update = { address: { street: 'Av. Assets Updated', number: '456', city: 'Lima', postalCode: '15036', country: 'Peru', latitude: 0, longitude: 0 }, region: 'Lima', district: 'San Isidro' }
    Given path '/api/v1/properties', created.property.id
    And request update
    When method put
    Then status 200
    And match response.id == created.property.id
    And match response.address == update.address
    And match response.district.name == update.district
    Given path '/api/v1/properties', created.property.id
    When method get
    Then status 200
    And match response.address == update.address
    And match response.district.name == update.district
    * def cleanup = karate.call('classpath:com/hampcoders/electrolink/assets/integration/support/assets-delete-property.feature', { propertyId: created.property.id })

  Scenario: Return not found when updating an unknown property
    Given path '/api/v1/properties', unknownId
    And request payload
    When method put
    Then status 404

  Scenario: Delete an existing property and confirm that it cannot be retrieved
    * def created = createProperty()
    Given path '/api/v1/properties', created.property.id
    When method delete
    Then status 204
    Given path '/api/v1/properties', created.property.id
    When method get
    Then status 404

  Scenario: Return not found when deleting an unknown property
    Given path '/api/v1/properties', unknownId
    When method delete
    Then status 404
