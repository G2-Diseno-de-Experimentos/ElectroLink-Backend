param(
    [string]$PostgresBin = 'D:\PostgreSQL\17\bin',
    [int]$DatabasePort = 55432,
    [int]$BackendPort = 8091,
    [switch]$SkipBuild
)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$localRoot = Join-Path $repoRoot 'target\assets-local'
$dataRoot = Join-Path $localRoot 'postgres-data'
if (-not (Test-Path (Join-Path $PostgresBin 'initdb.exe'))) { throw 'Indica -PostgresBin con la carpeta bin de PostgreSQL.' }
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) { throw 'JAVA_HOME debe apuntar a un JDK compatible (21 o superior).' }
New-Item -ItemType Directory -Path $localRoot -Force | Out-Null

# Generated secrets belong only to this disposable local cluster, never to Render.
$passwordFile = Join-Path $localRoot 'postgres-password.txt'
if (-not (Test-Path $passwordFile)) {
    $randomBytes = New-Object byte[] 32
    [Security.Cryptography.RandomNumberGenerator]::Fill($randomBytes)
    [Convert]::ToBase64String($randomBytes) | Set-Content -LiteralPath $passwordFile -Encoding utf8NoBOM
}
if (-not (Test-Path (Join-Path $dataRoot 'PG_VERSION'))) {
    & (Join-Path $PostgresBin 'initdb.exe') -D $dataRoot -U assets_test --auth=scram-sha-256 --pwfile=$passwordFile --encoding=UTF8 --locale=C
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo inicializar la base aislada de pruebas.' }
}
& (Join-Path $PostgresBin 'pg_ctl.exe') -D $dataRoot status *> $null
if ($LASTEXITCODE -ne 0) {
    if (Get-NetTCPConnection -State Listen -LocalPort $DatabasePort -ErrorAction SilentlyContinue) { throw "Puerto PostgreSQL $DatabasePort ocupado; elige otro." }
    & (Join-Path $PostgresBin 'pg_ctl.exe') -D $dataRoot -l (Join-Path $localRoot 'postgres.log') -o "-h 127.0.0.1 -p $DatabasePort" -w start
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar PostgreSQL de pruebas.' }
}
$previousPgPassword = $env:PGPASSWORD
try {
    $env:PGPASSWORD = (Get-Content -LiteralPath $passwordFile -Raw).Trim()
    $databaseExists = & (Join-Path $PostgresBin 'psql.exe') -h 127.0.0.1 -p $DatabasePort -U assets_test -d postgres -w -Atc "SELECT 1 FROM pg_database WHERE datname='electrolink_assets_tests'"
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo conectar a PostgreSQL de pruebas.' }
    if ($databaseExists -ne '1') {
        & (Join-Path $PostgresBin 'createdb.exe') -h 127.0.0.1 -p $DatabasePort -U assets_test -w electrolink_assets_tests
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear la base de pruebas.' }
    }
    $jwtFile = Join-Path $localRoot 'jwt-secret.txt'
    if (-not (Test-Path $jwtFile)) {
        $jwtBytes = New-Object byte[] 48
        [Security.Cryptography.RandomNumberGenerator]::Fill($jwtBytes)
        [Convert]::ToBase64String($jwtBytes) | Set-Content -LiteralPath $jwtFile -Encoding utf8NoBOM
    }
    $localConfig = Join-Path $localRoot 'application.properties'
    @(
        "spring.datasource.url=jdbc:postgresql://127.0.0.1:$DatabasePort/electrolink_assets_tests",
        'spring.datasource.username=assets_test',
        "spring.datasource.password=$env:PGPASSWORD",
        "authorization.jwt.secret=$((Get-Content -LiteralPath $jwtFile -Raw).Trim())",
        'spring.jpa.hibernate.ddl-auto=update',
        'spring.jpa.show-sql=false',
        'server.address=127.0.0.1',
        "server.port=$BackendPort"
    ) | Set-Content -LiteralPath $localConfig -Encoding utf8NoBOM
} finally { $env:PGPASSWORD = $previousPgPassword }

if (Get-NetTCPConnection -State Listen -LocalPort $BackendPort -ErrorAction SilentlyContinue) { throw "Puerto backend $BackendPort ocupado. No se detuvo ningún proceso existente." }
if (-not $SkipBuild) {
    Push-Location $repoRoot
    try { & .\mvnw.cmd '-DskipTests' package; if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación del backend.' } }
    finally { Pop-Location }
}
$jar = Get-ChildItem (Join-Path $repoRoot 'target') -Filter '*.jar' | Where-Object Name -NotLike '*.original' | Select-Object -First 1
if (-not $jar) { throw 'No se encontró el JAR del backend.' }
$configUri = ([Uri]$localConfig).AbsoluteUri
$backendProcess = Start-Process -FilePath (Join-Path $env:JAVA_HOME 'bin\java.exe') -ArgumentList @('-jar', "`"$($jar.FullName)`"", "--spring.config.additional-location=$configUri") -WorkingDirectory $repoRoot -WindowStyle Hidden -RedirectStandardOutput (Join-Path $localRoot 'backend.log') -RedirectStandardError (Join-Path $localRoot 'backend-error.log') -PassThru
$backendProcess.Id | Set-Content -LiteralPath (Join-Path $localRoot 'backend.pid')
$ready = $false
for ($attempt = 0; $attempt -lt 60; $attempt++) {
    $backendProcess.Refresh()
    if ($backendProcess.HasExited) { throw 'El backend terminó durante el arranque. Revisa backend.log y backend-error.log.' }
    if (Select-String -LiteralPath (Join-Path $localRoot 'backend.log') -Pattern 'Started ElectrolinkPlatformApplication' -Quiet) { $ready = $true; break }
    Start-Sleep -Milliseconds 500
}
if (-not $ready) { throw 'El backend sigue iniciando; revisa backend.log antes de ejecutar las pruebas.' }
"Backend iniciado (PID $($backendProcess.Id)). Swagger: http://localhost:$BackendPort/swagger-ui/index.html"
"Base aislada: electrolink_assets_tests en 127.0.0.1:$DatabasePort. Tu PostgreSQL habitual no se modificó."
"Logs: $localRoot"
