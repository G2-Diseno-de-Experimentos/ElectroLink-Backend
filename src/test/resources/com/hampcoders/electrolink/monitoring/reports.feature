Feature: Reports API

  Background:
    * url baseUrl

  Scenario: Reject a request without a token
    Given path '/api/v1/reports'
    When method get
    Then status 401

  @auth
  Scenario: List reports with a valid token
    Given path '/api/v1/reports'
    And header Authorization = 'Bearer ' + jwt
    When method get
    Then status 200
    And match response == '#[]'

  @auth
  Scenario: Return 404 for an unknown report
    Given path '/api/v1/reports/2147483647'
    And header Authorization = 'Bearer ' + jwt
    When method get
    Then status 404

  @auth @seeded
  Scenario: Create, read, and delete a report
    Given path '/api/v1/reports'
    And header Authorization = 'Bearer ' + jwt
    And request { requestId: '#(requestId)', reportType: 'INCIDENT', description: 'Karate monitoring test' }
    When method post
    Then status 201
    * def createdId = response
    Given path '/api/v1/reports', createdId
    And header Authorization = 'Bearer ' + jwt
    When method get
    Then status 200
    And match response.reportType == 'INCIDENT'
    And match response.description == 'Karate monitoring test'
    Given path '/api/v1/reports', createdId
    And header Authorization = 'Bearer ' + jwt
    When method delete
    Then status 204

  @auth @seeded
  Scenario: Find reports by request
    Given path '/api/v1/reports/requests', requestId
    And header Authorization = 'Bearer ' + jwt
    When method get
    Then status 200
    And match response == '#[]'
