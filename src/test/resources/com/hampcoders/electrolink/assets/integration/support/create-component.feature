@ignore
Feature: Create component helper

  Background:
    * url baseUrl

  Scenario: Create component
    * def type = call read('classpath:com/hampcoders/electrolink/assets/integration/support/create-component-type.feature') { token: '#(token)' }
    * def componentTypeId = type.componentTypeId
    * def uid = java.util.UUID.randomUUID() + ''
    * def componentName = 'Component ' + uid

    Given path '/api/v1/components'
    And header Authorization = 'Bearer ' + token
    And request { name: '#(componentName)', description: 'Component description', componentTypeId: '#(componentTypeId)', isActive: true }
    When method post
    Then status 201
    * def componentId = Number(response.id)
