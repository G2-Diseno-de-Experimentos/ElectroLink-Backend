# SDP — Requests, Schedules y Services

Las seis clases unitarias de Calin se mantienen sin cambios (20 casos). Sus tres features Karate se adaptaron al mismo backend real y PostgreSQL local aislado que Assets. No usan mocks HTTP ni dependen de cuentas, ids o contraseñas de Render.

## Preparar el entorno

En PowerShell 7, con `JAVA_HOME` configurado, ejecutar desde el backend:

```powershell
.\scripts\start-assets-local.ps1 -PostgresBin 'D:\PostgreSQL\17\bin'
```

Si ya está iniciado en `http://localhost:8091`, no volver a iniciarlo. No ejecutar `mvn clean` mientras el PostgreSQL aislado bajo `target/assets-local` esté funcionando.

## Ejecutar Karate real

```powershell
.\mvnw.cmd "-Dtest=SdpKarateIT" test
```

Reporte real: `target/karate-sdp-real/karate-summary.html`. Son 9 escenarios (3 por recurso). Registran usuarios únicos con autenticación real; los fixtures Java envían POST reales para preparar servicios, horarios y solicitudes, con propiedades y servicios propios cuando son necesarios. Los features comprueban consulta, actualización, eliminación efectiva y rechazo sin token. No atribuyen `401` a restricciones por rol que el backend no implementa.

El runner ejecuta una limpieza en `finally`, incluso si falla una aserción, limitada a los recursos creados por la suite. Los usuarios quedan en la base aislada porque no existe endpoint de eliminación de cuentas. No modificar datos de producción.

Los helpers validan los status de preparación y los ids devueltos. Requests usa la ruta real `/api/v1/requests/clients/{clientId}/requests`; Schedules utiliza `day` y horas `HH:mm`. No se cambiaron controladores ni reglas de negocio para hacer pasar las pruebas.

Para otra URL local usar `-Dsdp.baseUrl=http://localhost:8092` o `SDP_BASE_URL`. Las escrituras remotas están bloqueadas salvo autorización explícita con `-Dsdp.allowRemoteWrites=true`.

`SdpKarateTest` solo comprueba sintaxis con `dryRun` y escribe en `target/karate-sdp-dry-run`. No sustituye la evidencia HTTP. El runner general `ElectrolinkPlatformApplicationTests` no prepara las sesiones y fixtures específicos; utilizar `SdpKarateIT` para estas pruebas.

## Unitarias SDP

```powershell
.\mvnw.cmd "-Dtest=RequestCommandServiceImplTest,RequestQueryServiceImplTest,ScheduleCommandServiceImplTest,ScheduleQueryServiceImplTest,ServiceCommandServiceImplTest,ServiceQueryServiceImplTest" test
```

6.1.2 documenta el contrato HTTP ejecutado. 6.1.3 presenta los mismos features Gherkin y sus resultados desde el comportamiento esperado, sin una suite Cucumber independiente. Los JWT deben ocultarse en las copias publicadas de los reportes.
