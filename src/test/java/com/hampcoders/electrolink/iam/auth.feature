Feature: Obtención de Tokens JWT para Múltiples Roles

  Background:
    * configure connectTimeout = 60000
    * configure readTimeout = 60000
    * url 'https://electrolink-backend-9u9l.onrender.com'

  Scenario: Iniciar sesión y obtener tokens para Técnico y Homeowner
    # 1. Login como Técnico
    Given path 'api/v1/authentication/sign-in'
    And request { username: 'admin@gmail.com', password: 'password123' }
    When method post
    Then status 200
    * def techToken = response.token

    # 2. Login como Homeowner
    Given path 'api/v1/authentication/sign-in'
    And request { username: 'client@test.com', password: 'password123' }
    When method post
    Then status 200
    * def homeownerToken = response.token