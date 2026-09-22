param([switch]$Emulator)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (-not $env:JAVA_HOME) { $env:JAVA_HOME = "$env:ProgramFiles\Android\Android Studio\jbr" }
$tasks = @(':app:assembleDebug', ':app:assembleRelease', ':app:testDebugUnitTest', ':app:lintDebug')
if ($Emulator) { $tasks += ':app:connectedDebugAndroidTest' }
& .\gradlew.bat @tasks --console=plain
if ($LASTEXITCODE -ne 0) { throw 'Build or tests failed. Inspect Gradle reports.' }
& node scripts/check-backend.cjs
if ($LASTEXITCODE -ne 0) { throw 'Backend read-only probe failed.' }
Write-Host 'Local checks passed. This does not certify SMTP delivery, live RLS, or admin publishing. See STABILIZATION.md.'
