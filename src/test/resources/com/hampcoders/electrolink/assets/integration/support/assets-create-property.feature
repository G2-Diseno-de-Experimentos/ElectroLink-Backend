@ignore
Feature: Create a real property owned by this test session
  Scenario:
    Given url karate.properties['assets.baseUrl']
    And path '/api/v1/properties'
    And header Authorization = 'Bearer ' + assetsSession.jwt
    And request payload
    When method post
    Then status 201
    * def property = response
