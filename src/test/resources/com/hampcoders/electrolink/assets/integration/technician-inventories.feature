Feature: Technician inventory API

  Background:
    * url baseUrl
    * def tech = call read('classpath:com/hampcoders/electrolink/assets/integration/support/create-technician.feature')
    * def token = tech.token
    * def technicianId = tech.technicianId
    * def created = call read('classpath:com/hampcoders/electrolink/assets/integration/support/create-component.feature') { token: '#(token)' }
    * def componentId = created.componentId

  Scenario: Create the inventory automatically when a technician profile is registered
    Given path '/api/v1/technician-inventories/technician', technicianId
    And header Authorization = 'Bearer ' + token
    When method get
    Then status 200
    And match response == { inventoryId: '#uuid', technicianId: '#(technicianId)', stock: [] }

  Scenario: Reject a second inventory for the same technician
    Given path '/api/v1/technician-inventories'
    And header Authorization = 'Bearer ' + token
    When method post
    Then status 409
    And match response.message == 'Technician inventory already exists for this technician ID'

  Scenario: Forbid inventory creation for a user without a technician profile
    * def user = call read('classpath:com/hampcoders/electrolink/assets/integration/support/create-user.feature')
    Given path '/api/v1/technician-inventories'
    And header Authorization = 'Bearer ' + user.token
    When method post
    Then status 403

  Scenario: Return not found for a technician without inventory
    Given path '/api/v1/technician-inventories/technician/999999999'
    And header Authorization = 'Bearer ' + token
    When method get
    Then status 404

  Scenario: Add a component to the technician stock
    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks'
    And header Authorization = 'Bearer ' + token
    And request { componentId: '#(componentId)', quantity: 10, alertThreshold: 3 }
    When method post
    Then status 200
    And match response.stock == '#[1]'
    And match response.stock[0] contains { componentId: '#(componentId)', componentName: '#(created.componentName)', quantityAvailable: 10, alertThreshold: 3 }

  Scenario: Increase the quantity when the same component is added again
    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks'
    And header Authorization = 'Bearer ' + token
    And request { componentId: '#(componentId)', quantity: 10, alertThreshold: 3 }
    When method post
    Then status 200

    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks'
    And header Authorization = 'Bearer ' + token
    And request { componentId: '#(componentId)', quantity: 5, alertThreshold: 4 }
    When method post
    Then status 200
    And match response.stock == '#[1]'
    And match response.stock[0].quantityAvailable == 15
    And match response.stock[0].alertThreshold == 4

  Scenario Outline: Reject an invalid stock entry - <case>
    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks'
    And header Authorization = 'Bearer ' + token
    And request { componentId: '#(componentId)', quantity: <quantity>, alertThreshold: <threshold> }
    When method post
    Then status 400

    Examples:
      | case               | quantity | threshold |
      | quantity: 0        | 0        | 3         |
      | alertThreshold: -1 | 1        | -1        |

  Scenario Outline: Return not found when adding stock - <case>
    * def targetCompId = <compId>
    Given path '/api/v1/technician-inventories/technician', <techId>, 'stocks'
    And header Authorization = 'Bearer ' + token
    And request { componentId: '#(targetCompId)', quantity: 10, alertThreshold: 3 }
    When method post
    Then status 404

    Examples:
      | case               | techId       | compId      |
      | unknown component  | technicianId | 999999999   |
      | unknown technician | 999999999    | componentId |

  Scenario: Get the stock detail of a component
    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks'
    And header Authorization = 'Bearer ' + token
    And request { componentId: '#(componentId)', quantity: 10, alertThreshold: 3 }
    When method post
    Then status 200

    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks', componentId
    And header Authorization = 'Bearer ' + token
    When method get
    Then status 200
    And match response contains { componentId: '#(componentId)', componentName: '#(created.componentName)', quantityAvailable: 10, alertThreshold: 3 }

  Scenario: Return not found for a component that is not in stock
    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks', componentId
    And header Authorization = 'Bearer ' + token
    When method get
    Then status 404

  Scenario: Update the quantity and alert threshold of a stock item
    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks'
    And header Authorization = 'Bearer ' + token
    And request { componentId: '#(componentId)', quantity: 10, alertThreshold: 3 }
    When method post
    Then status 200

    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks', componentId
    And header Authorization = 'Bearer ' + token
    And request { newQuantity: 7, newAlertThreshold: 2 }
    When method put
    Then status 200
    And match response.stock[0].quantityAvailable == 7
    And match response.stock[0].alertThreshold == 2

  Scenario: Reject updating a component that is not in stock
    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks', componentId
    And header Authorization = 'Bearer ' + token
    And request { newQuantity: 7, newAlertThreshold: 2 }
    When method put
    Then status 404

  Scenario: Reject a negative quantity on update
    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks'
    And header Authorization = 'Bearer ' + token
    And request { componentId: '#(componentId)', quantity: 10, alertThreshold: 3 }
    When method post
    Then status 200

    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks', componentId
    And header Authorization = 'Bearer ' + token
    And request { newQuantity: -1, newAlertThreshold: 2 }
    When method put
    Then status 400

  Scenario: List the inventory among low stock inventories
    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks'
    And header Authorization = 'Bearer ' + token
    And request { componentId: '#(componentId)', quantity: 10, alertThreshold: 3 }
    When method post
    Then status 200
    * def inventoryId = response.inventoryId

    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks', componentId
    And header Authorization = 'Bearer ' + token
    And request { newQuantity: 2, newAlertThreshold: 3 }
    When method put
    Then status 200

    Given path '/api/v1/technician-inventories/low-stock'
    And header Authorization = 'Bearer ' + token
    When method get
    Then status 200
    And match response[*].inventoryId contains inventoryId

  Scenario: Exclude an inventory with enough stock from low stock
    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks'
    And header Authorization = 'Bearer ' + token
    And request { componentId: '#(componentId)', quantity: 10, alertThreshold: 3 }
    When method post
    Then status 200
    * def inventoryId = response.inventoryId

    Given path '/api/v1/technician-inventories/low-stock'
    And header Authorization = 'Bearer ' + token
    When method get
    Then status 200
    And match response[*].inventoryId !contains inventoryId

  Scenario: Remove a component from the technician stock
    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks'
    And header Authorization = 'Bearer ' + token
    And request { componentId: '#(componentId)', quantity: 10, alertThreshold: 3 }
    When method post
    Then status 200

    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks', componentId
    And header Authorization = 'Bearer ' + token
    When method delete
    Then status 204

    Given path '/api/v1/technician-inventories/technician', technicianId, 'stocks', componentId
    And header Authorization = 'Bearer ' + token
    When method delete
    Then status 404

  Scenario: Reject a request without a token
    Given path '/api/v1/technician-inventories/technician', technicianId
    When method get
    Then status 401
