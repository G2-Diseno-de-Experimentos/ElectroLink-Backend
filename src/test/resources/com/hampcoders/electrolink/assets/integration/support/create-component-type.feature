@ignore
Feature: Create component type helper

  Background:
    * url baseUrl

  Scenario: Create component type
    * def uid = java.util.UUID.randomUUID() + ''
    * def typeName = 'Type ' + uid
    Given path '/api/v1/component-types'
    And header Authorization = 'Bearer ' + token
    And request { name: '#(typeName)', description: 'Component type description' }
    When method post
    Then status 201
    * def componentTypeId = response.componentTypeId
