variable "cluster_name" {
  description = "Name of the local kind cluster."
  type        = string
  default     = "queueshield"
}

variable "namespace" {
  description = "Kubernetes namespace the app is deployed into."
  type        = string
  default     = "queueshield"
}

variable "db_username" {
  description = "Shared dev Postgres username (matches the default already baked into every service's application.yml - not a real secret, this is a local dev stack)."
  type        = string
  default     = "queueshield"
}

variable "db_password" {
  description = "Shared dev Postgres password (see db_username - local dev default, not sensitive in this context)."
  type        = string
  default     = "queueshield"
  sensitive   = true
}

variable "kind_config_path" {
  description = "Path to the kind cluster config (extraPortMappings for the gateway/frontend NodePorts)."
  type        = string
  default     = "../k8s/kind-cluster.yaml"
}

variable "chart_path" {
  description = "Path to the QueueShield Helm chart."
  type        = string
  default     = "../k8s/queueshield"
}
