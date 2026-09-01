resource "kubernetes_namespace" "queueshield" {
  metadata {
    name = var.namespace
  }
  depends_on = [null_resource.kind_cluster]
}

resource "kubernetes_secret" "db_credentials" {
  metadata {
    name      = "queueshield-db-credentials"
    namespace = kubernetes_namespace.queueshield.metadata[0].name
  }
  data = {
    username = var.db_username
    password = var.db_password
  }
  type = "Opaque"
}

# Images must already be loaded into the kind cluster's containerd (`kind load docker-image`,
# run by the Ansible playbook in ../ansible/) before this succeeds - imagePullPolicy: Never means
# pods will sit in ImagePullBackOff otherwise, since these images live nowhere but this machine.
resource "helm_release" "queueshield" {
  name       = "queueshield"
  namespace  = kubernetes_namespace.queueshield.metadata[0].name
  chart      = var.chart_path
  timeout    = 360
  set {
    name  = "dbSecretName"
    value = kubernetes_secret.db_credentials.metadata[0].name
  }

  depends_on = [kubernetes_secret.db_credentials]
}
