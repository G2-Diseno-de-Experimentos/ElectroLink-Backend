@sdp
Feature: Pruebas integrales de Horarios en SDP

  Background:
    * def baseUrl = 'https://electrolink-backend-9u9l.onrender.com'
    * url baseUrl

    * def authResult = callonce read('classpath:com/hampcoders/electrolink/iam/auth.feature')
    * def techToken = authResult.techToken
    * def homeownerToken = authResult.homeownerToken

  Scenario: Registrar un nuevo horario como Técnico
    Given path 'api/v1/schedules'
    And header Authorization = 'Bearer ' + techToken
    And request { technicianId: 1, startTime: '2026-10-10T08:00:00', endTime: '2026-10-10T12:00:00' }
    When method post
    Then status 200
    And match response == '#present'

  Scenario: Validar denegación de autorización al actualizar horario como Homeowner
    Given path 'api/v1/schedules/9999'
    And header Authorization = 'Bearer ' + homeownerToken
    And request { startTime: '2026-10-10T09:00:00', endTime: '2026-10-10T13:00:00' }
    When method put
    Then status 401

  Scenario: Validar denegación de autorización al eliminar horario como Homeowner
    Given path 'api/v1/schedules/9999'
    And header Authorization = 'Bearer ' + homeownerToken
    When method delete
    Then status 401