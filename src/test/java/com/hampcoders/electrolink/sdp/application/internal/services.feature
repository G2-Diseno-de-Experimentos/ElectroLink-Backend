@sdp
Feature: Pruebas integrales de Servicios en SDP

  Background:
    * def baseUrl = 'https://electrolink-backend-9u9l.onrender.com'
    * url baseUrl

    * def authResult = callonce read('classpath:com/hampcoders/electrolink/iam/auth.feature')
    * def techToken = authResult.techToken
    * def homeownerToken = authResult.homeownerToken

  Scenario: Consultar catálogo de servicios disponibles como Técnico
    Given path 'api/v1/services'
    And header Authorization = 'Bearer ' + techToken
    When method get
    Then status 200
    And match response == '#array'

  Scenario: Validar denegación de autorización al registrar un nuevo servicio como Técnico
    Given path 'api/v1/services'
    And header Authorization = 'Bearer ' + techToken
    And request { name: 'Mantenimiento preventivo', description: 'Revisión técnica de sistemas de carga', price: 150.0 }
    When method post
    Then status 401

  Scenario: Validar denegación de autorización al eliminar un servicio inexistente como Homeowner
    Given path 'api/v1/services/9999'
    And header Authorization = 'Bearer ' + homeownerToken
    When method delete
    Then status 401