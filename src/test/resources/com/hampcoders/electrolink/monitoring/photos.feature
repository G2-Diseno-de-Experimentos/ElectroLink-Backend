Feature: Report photos API

  Background:
    * url baseUrl

  Scenario: Reject a photo creation without a token
    Given path '/api/v1/photos'
    And request { reportId: 1, url: 'https://example.org/photo.jpg' }
    When method post
    Then status 401

  @auth @seeded
  Scenario: Attach a photo to an existing report
    Given path '/api/v1/photos'
    And header Authorization = 'Bearer ' + jwt
    And request { reportId: '#(reportId)', url: 'https://example.org/monitoring-test.jpg' }
    When method post
    Then status 201
    And match response.reportId == reportId

  @auth
  Scenario: Reject a photo for an unknown report
    Given path '/api/v1/photos'
    And header Authorization = 'Bearer ' + jwt
    And request { reportId: 2147483647, url: 'https://example.org/photo.jpg' }
    When method post
    Then status 404
