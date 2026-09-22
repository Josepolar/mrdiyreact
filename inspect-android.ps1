param([string]$Name = 'screen', [string]$TapId = '')
$ErrorActionPreference = 'Stop'
$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
$outDir = Join-Path $PSScriptRoot '.integration'
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
if ($TapId) {
    [xml]$previous = Get-Content (Join-Path $outDir 'window.xml')
    $node = $previous.SelectNodes('//node') | Where-Object { $_.'resource-id' -eq "com.mrdiy.careers:id/$TapId" } | Select-Object -First 1
    if (!$node) { throw "Control $TapId was not present in the last snapshot." }
    $coords = [regex]::Matches($node.bounds, '\d+') | ForEach-Object { [int]$_.Value }
    & $adb shell input tap ([int](($coords[0]+$coords[2])/2)) ([int](($coords[1]+$coords[3])/2)) | Out-Null
}
$dump = & $adb shell uiautomator dump /sdcard/mrdiy-window.xml 2>&1
if ($dump -notmatch 'dumped to') { throw "UI snapshot failed: $dump" }
& $adb pull /sdcard/mrdiy-window.xml (Join-Path $outDir 'window.xml') | Out-Null
[xml]$ui = Get-Content (Join-Path $outDir 'window.xml')
$ui.SelectNodes('//node') | Where-Object { $_.text -or $_.clickable -eq 'true' } | ForEach-Object {
    '{0} | {1} | {2}' -f $_.'resource-id', $_.text, $_.bounds
}
& $adb shell screencap -p /sdcard/mrdiy-screen.png
& $adb pull /sdcard/mrdiy-screen.png (Join-Path $outDir "$Name.png") | Out-Null

