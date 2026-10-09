@ignore
Feature: Remove only the property created by this scenario
  Scenario:
    Given url karate.properties['assets.baseUrl']
    And path '/api/v1/properties', propertyId
    And header Authorization = 'Bearer ' + assetsSession.jwt
    When method delete
    Then status 204
