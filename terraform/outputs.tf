output "gateway_url" {
  description = "Gateway URL reachable from the host, via kind's extraPortMappings."
  value       = "http://localhost:9190"
}

output "frontend_url" {
  description = "Frontend URL reachable from the host, via kind's extraPortMappings."
  value       = "http://localhost:9180"
}

output "kubeconfig_context" {
  value = "kind-${var.cluster_name}"
}
