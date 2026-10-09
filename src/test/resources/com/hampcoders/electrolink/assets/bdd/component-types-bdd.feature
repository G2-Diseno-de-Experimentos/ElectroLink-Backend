Feature: Catálogo de tipos de componentes eléctricos
  Como usuario autenticado de ElectroLink
  Quiero registrar y consultar tipos de componentes
  Para identificar los materiales eléctricos disponibles en el catálogo

  Background:
    Given un propietario autenticado

  Scenario: Registrar un tipo de componente y encontrarlo en el catálogo
    Given un tipo de componente nuevo con nombre único
    When registra el tipo de componente
    Then el tipo queda disponible en el catálogo con el mismo nombre

  Scenario: Consultar los tipos de componentes disponibles
    When consulta el catálogo de tipos de componentes
    Then recibe una lista de tipos con identificador y nombre

  Scenario: Exigir autenticación para consultar el catálogo
    When intenta consultar el catálogo sin enviar su autenticación
    Then el sistema exige autenticación
