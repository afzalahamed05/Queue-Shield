<#
.SYNOPSIS
    Tears down the QueueShield kind deployment: uninstalls the Helm release, deletes the
    namespace/secret, and deletes the kind cluster itself (via terraform destroy).
#>
$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$terraformDir = Join-Path $repoRoot "terraform"

Push-Location $terraformDir
try {
    terraform destroy -auto-approve
}
finally {
    Pop-Location
}

Write-Host "Cluster and all resources destroyed." -ForegroundColor Green
