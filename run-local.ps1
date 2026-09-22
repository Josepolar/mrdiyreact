$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$javaCandidates = @(
    $env:JAVA_HOME,
    "$env:ProgramFiles\Android\Android Studio\jbr",
    "$env:ProgramFiles\Android\Android Studio\jre",
    "$env:LOCALAPPDATA\Programs\Android Studio\jbr"
) | Where-Object { $_ -and (Test-Path (Join-Path $_ "bin\java.exe")) } | ForEach-Object { $_ }

if (-not $javaCandidates) {
    throw "Java was not found. Install Android Studio, then run .\run-local.ps1 again."
}

$env:JAVA_HOME = $javaCandidates | Select-Object -First 1
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$sdkCandidates = @(
    $env:ANDROID_HOME,
    $env:ANDROID_SDK_ROOT,
    "$env:LOCALAPPDATA\Android\Sdk",
    "$env:USERPROFILE\AppData\Local\Android\Sdk"
) | Where-Object { $_ -and (Test-Path (Join-Path $_ "platform-tools")) }

if (-not $sdkCandidates) {
    throw "Android SDK was not found. Open Android Studio, install the SDK and Android API 36, then run .\run-local.ps1 again."
}

$sdkPath = $sdkCandidates | Select-Object -First 1
$env:ANDROID_HOME = $sdkPath
$env:ANDROID_SDK_ROOT = $sdkPath
$sdkPropertyPath = $sdkPath.Replace('\', '\\')
$localPropertiesPath = Join-Path $projectRoot "local.properties"
$sdkProperty = "sdk.dir=$sdkPropertyPath"
$currentSdkProperty = if (Test-Path $localPropertiesPath) { (Get-Content $localPropertiesPath -Raw).Trim() } else { "" }
if ($currentSdkProperty -ne $sdkProperty) {
    $sdkProperty | Set-Content $localPropertiesPath
}

Set-Location $projectRoot
Write-Host "Using JDK: $env:JAVA_HOME"
Write-Host "Using Android SDK: $env:ANDROID_HOME"
Write-Host "Building app..."
& (Join-Path $projectRoot "gradlew.bat") assembleDebug
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$apkPath = Join-Path $projectRoot "app\build\outputs\apk\debug\app-debug.apk"
Write-Host "Build complete: $apkPath" -ForegroundColor Green

$adb = Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"
if (Test-Path $adb) {
    $devices = & $adb devices | Select-String "\tdevice$"
    if ($devices) {
        Write-Host "Installing on connected Android device..."
        & $adb install -r $apkPath
        & $adb shell am start -n "com.mrdiy.careers/.MainActivity"
    } else {
        Write-Host "No Android device or emulator is connected. APK is ready at: $apkPath"
    }
}
