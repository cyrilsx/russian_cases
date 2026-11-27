variable "namespace" {
  type    = string
  default = "russian-cases"
}

variable "image" {
  description = "ghcr image published by CI (make the ghcr package public first)"
  type        = string
  default     = "ghcr.io/cyrilsx/russian_cases/backend:latest"
}

variable "replicas" {
  type    = number
  default = 2
}

variable "app_nodeport" {
  description = "Must match app_nodeport of the infra module (Octavia LB member port)"
  type        = number
  default     = 30080
}

variable "app_jwt_secret" {
  description = "JWT signing secret (required)"
  type        = string
  sensitive   = true
}

variable "app_admin_password" {
  description = "Initial admin password (required)"
  type        = string
  sensitive   = true
}

variable "app_admin_username" {
  type    = string
  default = "admin"
}

variable "datasource_url" {
  description = "PostgreSQL JDBC URL (e.g. Aiven on Infomaniak). Empty = in-memory H2 (demo only)"
  type        = string
  default     = ""
  sensitive   = true
}

variable "datasource_username" {
  type      = string
  default   = ""
  sensitive = true
}

variable "datasource_password" {
  type      = string
  default   = ""
  sensitive = true
}
