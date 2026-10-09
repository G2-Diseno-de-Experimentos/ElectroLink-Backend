@sdp
Feature: Gestión de horarios SDP contra el backend real

  Background:
    * url karate.properties['sdp.baseUrl']
    * configure headers = { Authorization: '#("Bearer " + sdpSession.techJwt)' }

  Scenario: Registrar un horario y consultarlo por técnico
    * def created = karate.fromString(sdpApi.createSchedule())
    Given path 'api/v1/technicians', sdpSession.technicianId, 'schedules'
    When method get
    Then status 200
    And match response contains deep { id: '#(created.id)', technicianId: '#(sdpSession.technicianId)', day: 'MONDAY', startTime: '08:00', endTime: '12:00' }
    And match each response contains { technicianId: '#(sdpSession.technicianId)' }

  Scenario: Actualizar un horario y confirmar su eliminación
    * def created = karate.fromString(sdpApi.createSchedule())
    * def update = { technicianId: '#(sdpSession.technicianId)', day: 'TUESDAY', startTime: '09:00', endTime: '13:00' }
    Given path 'api/v1/schedules', created.id
    And request update
    When method put
    Then status 200
    Given path 'api/v1/technicians', sdpSession.technicianId, 'schedules'
    When method get
    Then status 200
    And match response contains deep { id: '#(created.id)', day: 'TUESDAY', startTime: '09:00', endTime: '13:00' }
    Given path 'api/v1/schedules', created.id
    When method delete
    Then status 200
    * eval sdpApi.markDeleted('/api/v1/schedules/' + created.id)
    Given path 'api/v1/technicians', sdpSession.technicianId, 'schedules'
    When method get
    Then status 200
    And match response[*].id !contains created.id

  Scenario: Rechazar la consulta de horarios sin autenticación
    * configure headers = {}
    Given path 'api/v1/technicians', sdpSession.technicianId, 'schedules'
    When method get
    Then status 401
