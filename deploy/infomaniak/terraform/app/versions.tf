terraform {
  required_version = ">= 1.6"

  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.33"
    }
  }
}

# Uses the kubeconfig written by the infra module bootstrap step (../kubeconfig)
provider "kubernetes" {
  config_path = "${path.module}/../kubeconfig"
}
