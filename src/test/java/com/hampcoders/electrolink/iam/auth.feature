Feature: Obtención de Token JWT

  Background:
    * configure connectTimeout = 60000
    * configure readTimeout = 60000
    * url 'https://electrolink-backend-9u9l.onrender.com'

  Scenario: Iniciar sesión y obtener token
    Given path 'api/v1/authentication/sign-in'
    And request { username: 'test@test.com', password: '123456' }
    When method post
    Then status 200
    And match response.token == '#string'

    * def authToken = response.token