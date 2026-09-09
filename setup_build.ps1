# MrDIY Careers - Build Setup Script
# Run this in PowerShell to download Gradle wrapper

Write-Host "Downloading Gradle Wrapper..." -ForegroundColor Cyan

# Create wrapper directory
$wrapperDir = "C:\Users\My PC\Downloads\MrDIYCareers_AndroidStudio\mrdiy_careers\gradle\wrapper"
if (!(Test-Path $wrapperDir)) {
    New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
}

# Download gradle-wrapper.jar
$jarUrl = "https://github.com/gradle/gradle/raw/v8.13.0/gradle/wrapper/gradle-wrapper.jar"
$jarPath = "$wrapperDir\gradle-wrapper.jar"

try {
    Invoke-WebRequest -Uri $jarUrl -OutFile $jarPath -UseBasicParsing
    Write-Host "Downloaded gradle-wrapper.jar" -ForegroundColor Green
} catch {
    Write-Host "Failed to download: $_" -ForegroundColor Red
}

# Check if Android Studio is available
$androidStudio = "C:\Program Files\Android\Android Studio\bin\studio64.exe"
if (Test-Path $androidStudio) {
    Write-Host "Android Studio found!" -ForegroundColor Green
    Write-Host "Opening project..." -ForegroundColor Cyan
    & $androidStudio "C:\Users\My PC\Downloads\MrDIYCareers_AndroidStudio\mrdiy_careers"
} else {
    Write-Host "Android Studio not found at: $androidStudio" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To build the app:" -ForegroundColor Cyan
    Write-Host "1. Open Android Studio" -ForegroundColor White
    Write-Host "2. Open the project folder: mrdiy_careers" -ForegroundColor White
    Write-Host "3. Wait for Gradle to sync" -ForegroundColor White
    Write-Host "4. Build -> Build Bundle(s) / APK(s) -> Build APK(s)" -ForegroundColor White
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Green