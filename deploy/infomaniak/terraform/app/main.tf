# Application on the cluster created by ../infra — mirrors deploy/infomaniak/kubernetes.yaml,
# but as native Terraform (Service type NodePort so the Octavia LB can target it).

resource "kubernetes_namespace_v1" "app" {
  metadata {
    name = var.namespace
  }
}

resource "kubernetes_secret_v1" "app" {
  metadata {
    name      = "russian-cases-secrets"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  type = "Opaque"
  data = {
    APP_JWT_SECRET           = var.app_jwt_secret
    APP_ADMIN_PASSWORD       = var.app_admin_password
    SPRING_DATASOURCE_URL    = var.datasource_url
    SPRING_DATASOURCE_USERNAME = var.datasource_username
    SPRING_DATASOURCE_PASSWORD = var.datasource_password
    SPRING_DATASOURCE_DRIVER = var.datasource_url == "" ? "org.h2.Driver" : "org.postgresql.Driver"
  }
}

resource "kubernetes_deployment_v1" "app" {
  metadata {
    name      = "russian-cases"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
    labels    = { app = "russian-cases" }
  }
  spec {
    replicas = var.replicas
    selector {
      match_labels = { app = "russian-cases" }
    }
    template {
      metadata {
        labels = { app = "russian-cases" }
      }
      spec {
        container {
          name  = "app"
          image = var.image
          port {
            container_port = 8080
          }
          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "prod"
          }
          env {
            name  = "SERVER_PORT"
            value = "8080"
          }
          env {
            name = "APP_ADMIN_USERNAME"
            value = var.app_admin_username
          }
          env_from {
            secret_ref {
              name = kubernetes_secret_v1.app.metadata[0].name
            }
          }
          resources {
            requests = { cpu = "250m", memory = "512Mi" }
            limits   = { cpu = "1000m", memory = "1Gi" }
          }
          liveness_probe {
            http_get {
              path = "/actuator/health"
              port = 8080
            }
            initial_delay_seconds = 30
            period_seconds        = 15
            timeout_seconds       = 5
            failure_threshold     = 3
          }
          readiness_probe {
            http_get {
              path = "/actuator/health"
              port = 8080
            }
            initial_delay_seconds = 10
            period_seconds        = 10
            timeout_seconds       = 5
            failure_threshold     = 3
          }
        }
      }
    }
  }
}

resource "kubernetes_service_v1" "app" {
  metadata {
    name      = "russian-cases"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  spec {
    selector = { app = "russian-cases" }
    type     = "NodePort"
    port {
      port        = 8080
      target_port = 8080
      node_port   = var.app_nodeport
    }
  }
}

output "app_url" {
  description = "App URL through the Octavia LB (health check: /actuator/health)"
  value       = "http://<lb_vip_public_ip_from_infra_outputs>/"
}
