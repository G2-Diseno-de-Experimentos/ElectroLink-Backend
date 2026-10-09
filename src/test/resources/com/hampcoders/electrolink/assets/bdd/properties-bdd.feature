Feature: Gestión de propiedades del hogar
  Como propietario de un hogar
  Quiero registrar, consultar, actualizar y eliminar mis propiedades
  Para mantener la información de los lugares donde necesito servicios eléctricos

  Background:
    Given un propietario autenticado

  Scenario: Registrar una propiedad y consultar sus datos guardados
    Given los datos válidos de una propiedad en "Miraflores"
    When registra la propiedad
    Then la propiedad queda registrada y puede consultarse por su identificador

  Scenario: Actualizar la ubicación de una propiedad
    Given una propiedad registrada por el propietario
    When cambia el distrito de su propiedad a "San Isidro"
    Then al consultar la propiedad su distrito es "San Isidro"

  Scenario: Filtrar las propiedades por propietario
    Given propiedades registradas por dos propietarios diferentes
    When consulta las propiedades de su propietario
    Then la lista contiene su propiedad y no incluye las del otro propietario

  Scenario: Eliminar definitivamente una propiedad
    Given una propiedad registrada por el propietario
    When elimina su propiedad
    Then la propiedad ya no puede consultarse

  Scenario Outline: Rechazar el registro cuando falta un dato obligatorio
    Given los datos de una propiedad sin el campo obligatorio "<campo>"
    When registra la propiedad
    Then el sistema rechaza el registro con código 400

    Examples:
      | campo    |
      | ownerId  |
      | address  |
      | region   |
      | district |

  Scenario: Informar que una propiedad no existe
    When consulta una propiedad que no existe
    Then el sistema informa que la propiedad no fue encontrada
