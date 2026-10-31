[CmdletBinding()]
param(
    [ValidateSet('local', 'dev', 'demo')]
    [string]$Profile = 'local',
    [string]$DatabaseUrl = $env:SPRING_DATASOURCE_URL,
    [string]$DatabaseName = 'sicms'
)

$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($DatabaseUrl)) {
    $DatabaseUrl = 'jdbc:postgresql://localhost:5432/sicms'
}
if ($Profile -match '(?i)prod') {
    throw 'Refusing demo reset for a production-like Spring profile.'
}
if (($env:SPRING_PROFILES_ACTIVE -split ',') -match '(?i)prod') {
    throw 'Refusing demo reset because SPRING_PROFILES_ACTIVE contains a production-like profile.'
}
if ($env:APP_DEMO_DATA_ENABLED -ne 'true' -or $env:APP_DEMO_DATA_RESET -ne 'true') {
    throw 'Set APP_DEMO_DATA_ENABLED=true and APP_DEMO_DATA_RESET=true to authorize a one-shot demo reset.'
}
if ([string]::IsNullOrWhiteSpace($env:SICMS_DEMO_CUSTOMER_PASSWORD) -or
    [string]::IsNullOrWhiteSpace($env:SICMS_DEMO_AGENT_PASSWORD) -or
    [string]::IsNullOrWhiteSpace($env:SICMS_DEMO_CLAIMS_OFFICER_PASSWORD) -or
    [string]::IsNullOrWhiteSpace($env:SICMS_DEMO_ADMIN_PASSWORD)) {
    throw 'All four SICMS_DEMO_*_PASSWORD variables must be set. Password values are never printed.'
}

$postgresUrl = $DatabaseUrl -replace '^jdbc:', ''
$uri = [Uri]$postgresUrl
if ($uri.Host -notin @('localhost', '127.0.0.1', '::1')) {
    throw 'Refusing demo reset because the PostgreSQL host is not local.'
}
if ($uri.AbsolutePath.Trim('/') -ne $DatabaseName) {
    throw 'Refusing demo reset because the configured and URL database names differ.'
}

$pgDump = Get-Command pg_dump -ErrorAction SilentlyContinue
if ($null -eq $pgDump) {
    throw 'pg_dump was not found on PATH. Install PostgreSQL client tools before any reset.'
}

$backupDirectory = Join-Path $env:LOCALAPPDATA 'SICMS\backups'
New-Item -ItemType Directory -Force -Path $backupDirectory | Out-Null
$backupPath = Join-Path $backupDirectory ("sicms-{0:yyyyMMdd-HHmmss}.dump" -f (Get-Date))
Write-Host "Confirmed local demo target: profile=$Profile host=$($uri.Host) database=$DatabaseName"
& $pgDump.Source --format=custom --file=$backupPath --dbname=$postgresUrl
if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $backupPath)) {
    throw 'PostgreSQL backup failed; no reset was started.'
}
Write-Host "Backup completed: $backupPath"

$env:SPRING_PROFILES_ACTIVE = $Profile
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    & .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=$Profile"
    if ($LASTEXITCODE -ne 0) {
        throw 'Demo data seeding failed; review the application log. The backup remains available.'
    }
}
finally {
    Pop-Location
    Remove-Item Env:APP_DEMO_DATA_RESET -ErrorAction SilentlyContinue
}
