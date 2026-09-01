# No official Terraform provider manages kind reliably on Windows, so cluster lifecycle is driven
# by local-exec - this is the standard fallback pattern for tools without a first-class provider.
# PowerShell is used explicitly (not the default cmd.exe interpreter) because kind.exe resolves
# the docker.exe on PATH correctly there but not from a Git Bash environment on this machine.
resource "null_resource" "kind_cluster" {
  triggers = {
    config_hash  = filemd5(var.kind_config_path)
    cluster_name = var.cluster_name
  }

  provisioner "local-exec" {
    interpreter = ["PowerShell", "-Command"]
    command     = <<-EOT
      $existing = kind get clusters 2>$null
      if ($existing -contains "${self.triggers.cluster_name}") {
        Write-Host "kind cluster '${self.triggers.cluster_name}' already exists, skipping create"
      } else {
        kind create cluster --name ${self.triggers.cluster_name} --config ${var.kind_config_path}
        if ($LASTEXITCODE -ne 0) { exit 1 }
      }
    EOT
  }

  provisioner "local-exec" {
    when        = destroy
    interpreter = ["PowerShell", "-Command"]
    command     = "kind delete cluster --name ${self.triggers.cluster_name}"
  }
}
