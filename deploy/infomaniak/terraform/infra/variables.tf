variable "cloud_name" {
  description = "Entry in clouds.yaml / OS_CLOUD env (Infomaniak Public Cloud project)"
  type        = string
  default     = "infomaniak"
}

variable "zone" {
  description = "Availability zone (e.g. nova, dc04-z1...)"
  type        = string
  default     = "nova"
}

variable "image" {
  description = "Glance image for the cluster nodes"
  type        = string
  default     = "Ubuntu 24.04 LTS"
}

variable "flavor_control" {
  description = "Flavor for the Kubernetes control plane"
  type        = string
  default     = "a4-ram8-disk50-perf1"
}

variable "flavor_worker" {
  description = "Flavor for the Kubernetes workers"
  type        = string
  default     = "a4-ram8-disk50-perf1"
}

variable "worker_count" {
  description = "Number of Kubernetes worker nodes"
  type        = number
  default     = 2
}

variable "ssh_keypair" {
  description = "Name of the OpenStack keypair registered for SSH access"
  type        = string
  default     = "mykey"
}

variable "ssh_private_key" {
  description = "Local path of the SSH private key matching ssh_keypair"
  type        = string
  default     = "~/.ssh/id_rsa"
}

variable "ssh_user" {
  description = "SSH user of the image"
  type        = string
  default     = "ubuntu"
}

variable "k8s_version" {
  description = "Kubernetes apt channel (kubeadm/kubelet/kubectl)"
  type        = string
  default     = "v1.31"
}

variable "pod_cidr" {
  description = "Pod network CIDR (flannel)"
  type        = string
  default     = "10.244.0.0/16"
}

variable "app_nodeport" {
  description = "NodePort the app Service exposes (target of the Octavia LB)"
  type        = number
  default     = 30080
}

variable "name_prefix" {
  description = "Prefix for all created resources"
  type        = string
  default     = "russian-cases"
}
