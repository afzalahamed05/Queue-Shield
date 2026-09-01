<#
.SYNOPSIS
    Reports the health of every pod and the public endpoints of the kind-deployed QueueShield stack.
#>
$ErrorActionPreference = "Continue"

Write-Host "== Pods (queueshield namespace) ==" -ForegroundColor Cyan
kubectl --context kind-queueshield -n queueshield get pods -o wide

Write-Host ""
Write-Host "== HTTP checks (through kind's extraPortMappings) ==" -ForegroundColor Cyan
$targets = @(
    @{ Name = "gateway";  Url = "http://localhost:9190/api/incidents" }
    @{ Name = "frontend"; Url = "http://localhost:9180/" }
)
foreach ($t in $targets) {
    try {
        $resp = Invoke-WebRequest -Uri $t.Url -UseBasicParsing -TimeoutSec 5
        Write-Host ("{0,-10} {1,-45} -> {2}" -f $t.Name, $t.Url, $resp.StatusCode) -ForegroundColor Green
    }
    catch {
        Write-Host ("{0,-10} {1,-45} -> FAILED: {2}" -f $t.Name, $t.Url, $_.Exception.Message) -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "(The READY column above already reflects each pod's actuator health probe - a 1/1" -ForegroundColor DarkGray
Write-Host " here means /actuator/health/readiness returned 200 for that service.)" -ForegroundColor DarkGray
