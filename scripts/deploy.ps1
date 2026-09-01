<#
.SYNOPSIS
    Deploys QueueShield to the local kind Kubernetes cluster.

.DESCRIPTION
    Orchestrates, in the only order that actually works:
      1. terraform apply -target=null_resource.kind_cluster   (cluster must exist first)
      2. the Ansible playbook, in a container                 (images can only load into an existing cluster)
      3. terraform apply                                      (namespace, secret, then the Helm release)
    Each tool does the one job it's actually suited for - see terraform/kind_cluster.tf,
    ansible/playbook.yml and k8s/queueshield/ for why the work is split this way.
#>
[CmdletBinding()]
param(
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$terraformDir = Join-Path $repoRoot "terraform"

function Step($msg) {
    Write-Host ""
    Write-Host "==> $msg" -ForegroundColor Cyan
}

Step "1/3: terraform apply -target=null_resource.kind_cluster (create the kind cluster)"
Push-Location $terraformDir
try {
    terraform init -input=false | Out-Host
    terraform apply -auto-approve "-target=null_resource.kind_cluster"
    if ($LASTEXITCODE -ne 0) { throw "terraform apply (cluster) failed" }
}
finally {
    Pop-Location
}

Step "2/3: Ansible - build images and load them into the kind cluster"
docker run --rm `
    -v /var/run/docker.sock:/var/run/docker.sock `
    -v "${repoRoot}:/workspace" `
    -w /workspace/ansible `
    queueshield-ansible -i inventory.ini playbook.yml
if ($LASTEXITCODE -ne 0) { throw "Ansible playbook failed" }

Step "3/3: terraform apply (namespace, secret, Helm release)"
Push-Location $terraformDir
try {
    terraform apply -auto-approve
    if ($LASTEXITCODE -ne 0) { throw "terraform apply (full) failed" }
    terraform output
}
finally {
    Pop-Location
}

Step "Done. Gateway: http://localhost:9190  Frontend: http://localhost:9180"
Write-Host "Run scripts/healthcheck.ps1 to verify every pod is ready." -ForegroundColor Green
