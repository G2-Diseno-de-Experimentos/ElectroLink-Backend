Feature: Components API

  Background:
    * url baseUrl
    * def tech = callonce read('classpath:com/hampcoders/electrolink/assets/integration/support/create-technician.feature')
    * def token = tech.token
    * def type = callonce read('classpath:com/hampcoders/electrolink/assets/integration/support/create-component-type.feature') { token: '#(token)' }
    * def uid = java.util.UUID.randomUUID() + ''

  Scenario Outline: Manage the full life cycle of a component - <name>
    * def compName = '<name> ' + uid
    Given path '/api/v1/components'
    And header Authorization = 'Bearer ' + token
    And request { name: '#(compName)', description: '<description>', componentTypeId: '#(type.componentTypeId)', isActive: true }
    When method post
    Then status 201
    And match response.id == '#string'
    And match response.isActive == true
    And match response.componentTypeId == type.componentTypeId
    * def componentId = response.id

    Given path '/api/v1/components', componentId
    And header Authorization = 'Bearer ' + token
    When method get
    Then status 200
    And match response.name == compName

    * def updatedName = '<name> updated ' + uid
    Given path '/api/v1/components', componentId
    And header Authorization = 'Bearer ' + token
    And request { name: '#(updatedName)', description: 'Updated description' }
    When method put
    Then status 200
    And match response.name == updatedName
    And match response.description == 'Updated description'

    Given path '/api/v1/components', componentId
    And header Authorization = 'Bearer ' + token
    When method delete
    Then status 204

    Given path '/api/v1/components', componentId
    And header Authorization = 'Bearer ' + token
    When method get
    Then status 404

    Examples:
      | name    | description                  |
      | Breaker | Thermomagnetic breaker 2x20A |
      | Cable   | Copper cable 12 AWG          |

  Scenario: List components including a newly created one
    * def created = call read('classpath:com/hampcoders/electrolink/assets/integration/support/create-component.feature') { token: '#(token)' }
    Given path '/api/v1/components'
    And header Authorization = 'Bearer ' + token
    When method get
    Then status 200
    And match response[*].id contains (created.componentId + '')

  Scenario: Reject a component with a duplicated name
    * def compName = 'Duplicate ' + uid
    * def compPayload = { name: '#(compName)', description: 'Test duplicate component', componentTypeId: '#(type.componentTypeId)', isActive: true }
    Given path '/api/v1/components'
    And header Authorization = 'Bearer ' + token
    And request compPayload
    When method post
    Then status 201

    Given path '/api/v1/components'
    And header Authorization = 'Bearer ' + token
    And request compPayload
    When method post
    Then status 409
    And match response.message == 'Component with the same name already exists'

  Scenario Outline: Reject an invalid component - <case>
    Given path '/api/v1/components'
    And header Authorization = 'Bearer ' + token
    And request <payload>
    When method post
    Then status 400

    Examples:
      | case                    | payload                                                                               |
      | blank name              | { name: '', description: 'Invalid blank name', componentTypeId: 1, isActive: true }    |
      | componentTypeId: 0      | { name: 'Invalid Type ID', description: 'Invalid type id', componentTypeId: 0, isActive: true } |
      | missing componentTypeId | { name: 'Missing Type ID', description: 'Missing type id', isActive: true }          |

  Scenario: Reject an update with a blank name
    * def created = call read('classpath:com/hampcoders/electrolink/assets/integration/support/create-component.feature') { token: '#(token)' }
    Given path '/api/v1/components', created.componentId
    And header Authorization = 'Bearer ' + token
    And request { name: '' }
    When method put
    Then status 400

  Scenario Outline: Return not found for an unknown component - <method>
    Given path '/api/v1/components/999999999'
    And header Authorization = 'Bearer ' + token
    And request <body>
    When method <method>
    Then status 404

    Examples:
      | method | body                                   |
      | get    | null                                   |
      | put    | { name: 'Any name', description: 'x' } |
      | delete | null                                   |

  Scenario: Reject a non positive component id
    Given path '/api/v1/components/0'
    And header Authorization = 'Bearer ' + token
    When method get
    Then status 400

  Scenario: Reject deleting a component that is in a technician stock
    * def created = call read('classpath:com/hampcoders/electrolink/assets/integration/support/create-component.feature') { token: '#(token)' }
    Given path '/api/v1/technician-inventories/technician', tech.technicianId, 'stocks'
    And header Authorization = 'Bearer ' + token
    And request { componentId: '#(created.componentId)', quantity: 1, alertThreshold: 0 }
    When method post
    Then status 200

    Given path '/api/v1/components', created.componentId
    And header Authorization = 'Bearer ' + token
    When method delete
    Then status 409

  Scenario: Reject a request without a token
    Given path '/api/v1/components'
    When method get
    Then status 401
