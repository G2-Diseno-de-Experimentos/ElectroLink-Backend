@ignore
Feature: Create user helper

  Background:
    * url baseUrl

  Scenario: Create user without profile
    * def uid = (java.util.UUID.randomUUID() + '').substring(0, 8)
    * def email = 'user-' + uid + '@electrolink.test'
    * def password = 'Karate123!'

    Given path '/api/v1/authentication/sign-up'
    And request { username: '#(email)', password: '#(password)', roles: ['ROLE_TECHNICIAN'] }
    When method post
    Then status 201

    Given path '/api/v1/authentication/sign-in'
    And request { username: '#(email)', password: '#(password)' }
    When method post
    Then status 200
    * def token = response.token
