output "control_plane_floating_ip" {
  description = "Public IP of the control plane (SSH + kubeconfig server)"
  value       = local.cp_fip
}

output "control_plane_private_ip" {
  value = local.cp_priv
}

output "worker_private_ips" {
  value = local.worker_ips
}

output "lb_vip_public_ip" {
  description = "Public IP of the Octavia LB — this is your app URL: http://<ip>/ (health: /actuator/health)"
  value       = openstack_networking_floatingip_v2.lb.address
}

output "app_nodeport" {
  value = var.app_nodeport
}

output "kubeconfig_path" {
  description = "Local kubeconfig written by the bootstrap step (used by the app/ module)"
  value       = "${path.module}/kubeconfig"
}

output "next_step" {
  value = "cd ../app && terraform init && terraform apply -var=app_jwt_secret=... -var=app_admin_password=..."
}
