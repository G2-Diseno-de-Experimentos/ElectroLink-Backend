# Assets — integrante 1

Las unitarias usan Mockito. Karate usa HTTP contra **Spring Boot y PostgreSQL reales**; ya no existe un mock HTTP en Assets. No ejecutar estas suites sobre una base de producción. El apartado 6.1.3 presenta los mismos features Gherkin de Karate y sus evidencias, como Components y Technician Inventory; no hay una suite Cucumber activa.

## Iniciar un entorno local real y aislado (Windows / PowerShell 7)

Con PostgreSQL instalado y `JAVA_HOME` apuntando a un JDK 21 o superior:

```powershell
.\scripts\start-assets-local.ps1 -PostgresBin 'D:\PostgreSQL\17\bin'
```

El script compila el backend, inicia un clúster PostgreSQL separado en `127.0.0.1:55432` con autenticación SCRAM y crea `electrolink_assets_tests`. Genera credenciales y configuración solo en `target/assets-local` (ignorado por Git) y arranca el backend en `127.0.0.1:8091`. No cambia la contraseña, la configuración ni los datos de tu PostgreSQL habitual o de Render.

Swagger: `http://localhost:8091/swagger-ui/index.html`. Logs: `target/assets-local/backend.log` y `backend-error.log`. El script no reemplaza procesos que ya ocupen el puerto.

La configuración principal requiere las variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` para Render o un entorno local propio. Sin ellas, el arranque normal desde IntelliJ falla. Si prefieres IntelliJ después de preparar este entorno, detén el JAR que inició el script y añade en **Program arguments**:

```text
--spring.config.additional-location=file:./target/assets-local/application.properties
```

También se incluye la configuración compartida de IntelliJ **ElectroLink - Assets Local (PostgreSQL real)** en `.run`, con ese argumento ya configurado. Selecciónala después de preparar la base con el script; no ejecutes simultáneamente el JAR y la configuración de IntelliJ en el mismo puerto.

Para detener únicamente los procesos de este entorno:

```powershell
$assetsPid = [int](Get-Content .\target\assets-local\backend.pid)
$assetsProcess = Get-CimInstance Win32_Process -Filter "ProcessId=$assetsPid"
if ($assetsProcess.CommandLine -like '*service-platform-parent*' -and $assetsProcess.CommandLine -like '*assets-local*') {
    Stop-Process -Id $assetsPid
}
& 'D:\PostgreSQL\17\bin\pg_ctl.exe' -D "$PWD\target\assets-local\postgres-data" -m fast -w stop
```

No usar `mvn clean` mientras este PostgreSQL esté funcionando: sus archivos se encuentran en `target`. Si limpias `target` después de detenerlo, el script genera un entorno nuevo.

## Pruebas unitarias (19 casos)

```powershell
.\mvnw.cmd "-Dtest=PropertyCommandServiceImplTest,PropertyQueryServiceImplTest,ComponentTypeCommandServiceImplTest,ComponentTypeQueryServiceImplTest" test
```

## Karate real (13 escenarios)

```powershell
.\mvnw.cmd "-Dtest=AssetsKarateIT" test
```

Reporte: `target/karate-assets-real/karate-summary.html`. `AssetsKarateTest` solo valida sintaxis con `dryRun`, en otra carpeta; no constituye evidencia de integración.

## Ejecutar toda la parte 1

```powershell
.\mvnw.cmd "-Dtest=PropertyCommandServiceImplTest,PropertyQueryServiceImplTest,ComponentTypeCommandServiceImplTest,ComponentTypeQueryServiceImplTest,AssetsKarateIT" test
```

Surefire registra 20 métodos JUnit: 19 unitarios y un runner Karate. Su reporte interno muestra 13 escenarios Karate, no 20 escenarios de API.

## Otro backend de pruebas

```powershell
.\mvnw.cmd "-Dtest=AssetsKarateIT" "-Dassets.baseUrl=http://localhost:8092" test
```

También puede usarse `ASSETS_BASE_URL`. Para un backend remoto de pruebas hay que autorizar expresamente sus escrituras con `-Dassets.allowRemoteWrites=true`. Las suites registran usuarios únicos, obtienen JWTs reales y crean sus propias propiedades; las propiedades se limpian por API. Los tipos de componentes y usuarios de prueba permanecen en la base aislada porque no existen endpoints para eliminarlos. No reutilizan identificadores fijos ni asumen un número fijo de registros.

Antes de publicar evidencias, ocultar los JWT de los logs HTTP de Karate. Nunca subir los secretos locales, contraseñas o tokens a Git.
