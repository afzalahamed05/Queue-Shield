<#
.SYNOPSIS
    One-time WinRM setup so Ansible (running in a container) can manage this Windows host.

.DESCRIPTION
    RUN THIS YOURSELF - it is not run automatically by anything in this repo. It changes Windows
    security settings (enables a network listener, adds a firewall rule), and that's a line this
    project's tooling doesn't cross on its own.

    What it does, and why each piece is here:
      - Enable-PSRemoting: starts the WinRM service and adds the firewall rule Windows scopes to
        the Private/Domain network profiles only (not Public) - this is Microsoft's own default,
        not something this script loosens further.
      - Basic auth + AllowUnencrypted: the simplest way for Ansible's pywinrm client to
        authenticate without setting up Kerberos or a TLS certificate. Fine for a local dev/demo
        box reachable only from containers on this same machine; NOT something you'd do for a
        WinRM endpoint reachable from the network. See "to undo" below.

.NOTES
    To undo everything this script does:
        Disable-PSRemoting -Force
        Remove-NetFirewallRule -Name "WINRM-HTTP-In-TCP*"
#>
$ErrorActionPreference = "Stop"

Write-Host "Enabling PowerShell Remoting (WinRM service + firewall rule, Private/Domain only)..." -ForegroundColor Cyan
Enable-PSRemoting -Force

Write-Host "Enabling Basic auth on the WinRM service (needed for Ansible's pywinrm client)..." -ForegroundColor Cyan
Set-Item -Path WSMan:\localhost\Service\Auth\Basic -Value $true
Set-Item -Path WSMan:\localhost\Service\AllowUnencrypted -Value $true

Write-Host ""
Write-Host "Done. Verify the listener:" -ForegroundColor Green
winrm enumerate winrm/config/listener

Write-Host ""
Write-Host "Now run the Ansible playbook from ansible/:" -ForegroundColor Green
Write-Host '  $env:WINRM_PASSWORD = "<your Windows account password>"'
Write-Host '  docker run --rm -e WINRM_PASSWORD -v "${PWD}\..:/workspace" -w /workspace/ansible queueshield-ansible -i inventory.ini windows-playbook.yml -e ansible_user=<your-windows-username>'
