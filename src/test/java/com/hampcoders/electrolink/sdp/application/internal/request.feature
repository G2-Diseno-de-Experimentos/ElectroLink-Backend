@sdp
Feature: Pruebas integrales de Solicitudes de Servicio en SDP

  Background:
    * def baseUrl = 'https://electrolink-backend-9u9l.onrender.com'
    * url baseUrl

    * def authResult = callonce read('classpath:com/hampcoders/electrolink/iam/auth.feature')
    * def homeownerToken = authResult.homeownerToken
    * def techToken = authResult.techToken

  Scenario: Validar denegación de autorización al crear solicitud como Homeowner
    Given path 'api/v1/requests'
    And header Authorization = 'Bearer ' + homeownerToken
    And request { clientId: 1, serviceId: 1, description: 'Mantenimiento de tablero' }
    When method post
    Then status 401

  Scenario: Validar denegación de autorización al consultar solicitudes como Homeowner
    Given path 'api/v1/requests'
    And param clientId = 1
    And header Authorization = 'Bearer ' + homeownerToken
    When method get
    Then status 401

  Scenario: Validar denegación de autorización al eliminar una solicitud como Homeowner
    Given path 'api/v1/requests/9999'
    And header Authorization = 'Bearer ' + homeownerToken
    When method delete
    Then status 401