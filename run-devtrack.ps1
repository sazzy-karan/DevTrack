$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$javaFxRoot = Join-Path $env:USERPROFILE ".m2\repository\org\openjfx"
$javaFxJars = @(
    (Join-Path $javaFxRoot "javafx-base\21.0.7\javafx-base-21.0.7-win.jar"),
    (Join-Path $javaFxRoot "javafx-graphics\21.0.7\javafx-graphics-21.0.7-win.jar"),
    (Join-Path $javaFxRoot "javafx-controls\21.0.7\javafx-controls-21.0.7-win.jar")
)
$mysqlJar = Join-Path $env:USERPROFILE ".m2\repository\com\mysql\mysql-connector-j\9.3.0\mysql-connector-j-9.3.0.jar"

foreach ($jar in $javaFxJars + $mysqlJar) {
    if (-not (Test-Path -LiteralPath $jar)) {
        throw "Required library is missing: $jar. Restore project dependencies before launching."
    }
}

$sources = @(Get-ChildItem (Join-Path $projectRoot "src\main\java") -Recurse -Filter "*.java" |
    ForEach-Object { $_.FullName })
if ($sources.Count -eq 0) {
    throw "No Java source files were found under src\main\java."
}

$classes = Join-Path $projectRoot "target\classes"
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$modulePath = $javaFxJars -join ";"

Write-Host "Building DevTrack..."
& javac --module-path $modulePath --add-modules javafx.controls `
    -encoding UTF-8 -d $classes $sources
if ($LASTEXITCODE -ne 0) {
    throw "DevTrack compilation failed (javac exit code $LASTEXITCODE)."
}

Write-Host "Launching DevTrack..."
$classPath = $classes + ";" + $mysqlJar
$javaArguments = @(
    "--module-path", ('"' + $modulePath + '"'),
    "--add-modules", "javafx.controls",
    "-classpath", ('"' + $classPath + '"'),
    "com.devtrack.App"
)
$launcher = Start-Process -FilePath "java.exe" -WorkingDirectory $projectRoot `
    -ArgumentList $javaArguments -PassThru
Start-Sleep -Seconds 3
$application = Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
    Where-Object { $_.CommandLine -match 'com\.devtrack\.App' } |
    Select-Object -First 1
if ($null -eq $application) {
    throw "DevTrack did not stay running after launch (launcher PID $($launcher.Id))."
}
Write-Host "DevTrack is running (PID $($application.ProcessId))."
