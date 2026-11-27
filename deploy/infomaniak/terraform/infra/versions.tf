terraform {
  required_version = ">= 1.6"

  required_providers {
    openstack = {
      source  = "terraform-provider-openstack/openstack"
      version = "~> 3.0"
    }
    null = {
      source  = "hashicorp/null"
      version = "~> 3.2"
    }
  }
}

# Authenticate via environment (Infomaniak Public Cloud OpenStack RC file):
#   source openrc.sh   (sets OS_AUTH_URL, OS_PROJECT_NAME, OS_USERNAME, ...)
# or use ~/.config/openstack/clouds.yaml with `cloud = "infomaniak"`.
provider "openstack" {
  cloud = var.cloud_name
}
