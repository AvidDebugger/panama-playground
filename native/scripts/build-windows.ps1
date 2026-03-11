Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
Push-Location $PSScriptRoot
try {
    Push-Location ..
    if (-not (Test-Path build)) { New-Item -ItemType Directory -Path build }
    Set-Location build
    cmake ..
    cmake --build .
} finally {
    Pop-Location
}
