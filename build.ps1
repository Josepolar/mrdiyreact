$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Set-Location "C:\Users\My PC\Downloads\MrDIYCareers_AndroidStudio\mrdiy_careers"

Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host "Building app..."

& ".\gradlew.bat" assembleDebug