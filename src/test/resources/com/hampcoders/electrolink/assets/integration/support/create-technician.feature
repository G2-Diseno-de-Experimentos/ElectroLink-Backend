@ignore
Feature: Create technician helper

  Background:
    * url baseUrl

  Scenario: Create a technician with profile
    * def uid = (java.util.UUID.randomUUID() + '').substring(0, 8)
    * def email = 'tech-' + uid + '@electrolink.test'
    * def password = 'Karate123!'

    Given path '/api/v1/authentication/sign-up'
    And request { username: '#(email)', password: '#(password)', roles: ['ROLE_TECHNICIAN'] }
    When method post
    Then status 201

    Given path '/api/v1/profiles'
    And request { firstName: 'Karate', lastName: 'Technician', email: '#(email)', street: 'Av. Test 123', role: 'TECHNICIAN', additionalInfoOrCertification: 'CERT-KARATE' }
    When method post
    Then status 201
    * def technicianId = response.id

    Given path '/api/v1/authentication/sign-in'
    And request { username: '#(email)', password: '#(password)' }
    When method post
    Then status 200
    * def token = response.token
