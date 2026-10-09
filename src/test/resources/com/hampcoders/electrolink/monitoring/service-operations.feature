Feature: Service operations API

  Background:
    * url baseUrl

  Scenario: Reject a request without a token
    Given path '/api/v1/service-operations'
    When method get
    Then status 401

  @auth
  Scenario: List operations with a valid token
    Given path '/api/v1/service-operations'
    And header Authorization = 'Bearer ' + jwt
    When method get
    Then status 200
    And match response == '#[]'

  @auth
  Scenario: Return 404 for an unknown operation
    Given path '/api/v1/service-operations/2147483647'
    And header Authorization = 'Bearer ' + jwt
    When method get
    Then status 404

  @auth @seeded
  Scenario: Create and complete a service operation
    Given path '/api/v1/service-operations'
    And header Authorization = 'Bearer ' + jwt
    And request { technicianId: '#(technicianId)' }
    When method post
    Then status 201
    * def operationId = response
    Given path '/api/v1/service-operations/status'
    And header Authorization = 'Bearer ' + jwt
    And request { requestId: '#(operationId)', newStatus: 'COMPLETED' }
    When method put
    Then status 204
    Given path '/api/v1/service-operations', operationId
    And header Authorization = 'Bearer ' + jwt
    When method get
    Then status 200
    And match response.currentStatus == 'COMPLETED'
    And match response.completedAt == '#notnull'

  @auth @seeded
  Scenario: Find operations by technician
    Given path '/api/v1/service-operations/technicians', technicianId
    And header Authorization = 'Bearer ' + jwt
    When method get
    Then status 200
    And match response == '#[]'
