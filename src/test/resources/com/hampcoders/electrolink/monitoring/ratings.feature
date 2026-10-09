Feature: Ratings API

  Background:
    * url baseUrl

  Scenario: Reject a request without a token
    Given path '/api/v1/ratings'
    When method get
    Then status 401

  @auth
  Scenario: List ratings with a valid token
    Given path '/api/v1/ratings'
    And header Authorization = 'Bearer ' + jwt
    When method get
    Then status 200
    And match response == '#[]'

  @auth
  Scenario: Return 404 for an unknown rating
    Given path '/api/v1/ratings/2147483647'
    And header Authorization = 'Bearer ' + jwt
    When method get
    Then status 404

  @auth @seeded
  Scenario: Create, update, and delete a rating for a completed operation
    Given path '/api/v1/ratings'
    And header Authorization = 'Bearer ' + jwt
    And request { requestId: '#(requestId)', score: 5, comment: 'Great work', raterId: 'karate-test', technicianId: '#(technicianId)' }
    When method post
    Then status 201
    * def ratingId = response
    Given path '/api/v1/ratings'
    And header Authorization = 'Bearer ' + jwt
    And request { ratingId: '#(ratingId)', score: 4, comment: 'Updated' }
    When method put
    Then status 200
    Given path '/api/v1/ratings', ratingId
    And header Authorization = 'Bearer ' + jwt
    When method get
    Then status 200
    And match response.score == 4
    Given path '/api/v1/ratings', ratingId
    And header Authorization = 'Bearer ' + jwt
    When method delete
    Then status 204

  @auth @seeded
  Scenario: Find ratings by request and technician
    Given path '/api/v1/ratings/requests', requestId
    And header Authorization = 'Bearer ' + jwt
    When method get
    Then status 200
    And match response == '#[]'
    Given path '/api/v1/ratings/technicians', technicianId
    And header Authorization = 'Bearer ' + jwt
    When method get
    Then status 200
    And match response == '#[]'
