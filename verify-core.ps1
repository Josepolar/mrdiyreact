$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$sdk = 'C:\Users\HP\AppData\Local\Android\Sdk'
$adb = Join-Path $sdk 'platform-tools\adb.exe'
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Set-Location $root

Write-Host '== Build and unit tests =='
& .\gradlew.bat :app:assembleDebug testDebugUnitTest
if ($LASTEXITCODE -ne 0) { throw 'Gradle validation failed.' }

Write-Host '== Emulator and app launch =='
$devices = & $adb devices
if (-not ($devices | Select-String '\tdevice$')) { throw 'No ready Android emulator/device found.' }
$apk = Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk'
& $adb install -r $apk | Out-Host
& $adb shell am force-stop com.mrdiy.careers
& $adb logcat -c
& $adb shell am start -W -n 'com.mrdiy.careers/.MainActivity' | Out-Host
Start-Sleep -Seconds 2
$crash = & $adb logcat -d -v brief | Select-String 'FATAL EXCEPTION|Application Error|Process: com.mrdiy.careers'
if ($crash) { $crash; throw 'App crash detected.' }
$activity = & $adb shell dumpsys activity activities | Select-String 'ResumedActivity|mCurrentFocus' | Select-Object -Last 4
$activity | Out-Host

Write-Host '== Backend checks =='
try {
    $supabase = Invoke-WebRequest -Uri 'https://sfjpiyevasnmvddgtofz.supabase.co/rest/v1/' -Headers @{apikey='configured-anon-key'} -UseBasicParsing
    Write-Host "Supabase HTTP $($supabase.StatusCode)"
} catch {
    if ($_.Exception.Response) { Write-Host "Supabase reachable; HTTP $([int]$_.Exception.Response.StatusCode)" } else { throw }
}
$jobs = Invoke-WebRequest -Uri 'https://mrd-i-y-careers.onrender.com/api/jobs.php?status=Open&page=1&limit=50' -UseBasicParsing | Select-Object -ExpandProperty Content | ConvertFrom-Json
Write-Host "Jobs API success=$($jobs.success) total=$($jobs.total) payloadCount=$($jobs.jobs.Count)"

Write-Host '== Placeholder audit =='
$placeholders = Get-ChildItem app\src\main\java -Recurse -File -Filter *.kt | Select-String -Pattern 'Coming soon|\(demo\)|TODO|Applications feature' -CaseSensitive
if ($placeholders) { $placeholders | ForEach-Object { Write-Host "$($_.Path):$($_.LineNumber): $($_.Line.Trim())" } } else { Write-Host 'No placeholder markers found.' }

Write-Host 'CORE VERIFICATION COMPLETE'
