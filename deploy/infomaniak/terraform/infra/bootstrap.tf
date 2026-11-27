# Kubeadm bootstrap: wait for the control plane, fetch kubeconfig + join command, join workers.
# Uses local ssh (null_resource local-exec). Requires ssh reachability to the CP floating IP.

locals {
  cp_fip    = openstack_networking_floatingip_v2.cp.address
  cp_priv   = openstack_networking_port_v2.control[0].fixed_ip[0].ip_address
  ssh_key   = var.ssh_private_key
  ssh_opts  = "-i ${local.ssh_key} -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o ConnectTimeout=10"
  worker_ips = [for p in openstack_networking_port_v2.workers : p.fixed_ip[0].ip_address]
}

resource "null_resource" "kubeadm_bootstrap" {
  triggers = {
    cp_id       = openstack_compute_instance_v2.control[0].id
    worker_ids  = join(",", openstack_compute_instance_v2.workers[*].id)
    k8s_version = var.k8s_version
  }

  provisioner "local-exec" {
    command = <<-EOT
      set -e
      CP=${local.cp_fip}
      echo "[bootstrap] waiting for SSH on control plane $CP ..."
      for i in $(seq 1 60); do
        ssh ${local.ssh_opts} ${var.ssh_user}@$CP 'true' 2>/dev/null && break
        sleep 10
      done
      echo "[bootstrap] waiting for kubeadm (join-command.sh) ..."
      for i in $(seq 1 60); do
        ssh ${local.ssh_opts} ${var.ssh_user}@$CP 'test -s /root/join-command.sh' 2>/dev/null && break
        sleep 10
      done
      echo "[bootstrap] fetching kubeconfig ..."
      scp ${local.ssh_opts} ${var.ssh_user}@$CP:/etc/kubernetes/admin.conf ./kubeconfig
      sed -i "s|server: https://.*|server: https://$CP:6443|" ./kubeconfig
      echo "[bootstrap] fetching join command ..."
      ssh ${local.ssh_opts} ${var.ssh_user}@$CP 'cat /root/join-command.sh' > ./join-command.sh
      chmod +x ./join-command.sh
      echo "[bootstrap] joining workers ..."
      for W in ${join(" ", local.worker_ips)}; do
        for i in $(seq 1 40); do
          ssh ${local.ssh_opts} ${var.ssh_user}@$W 'true' 2>/dev/null && break
          sleep 10
        done
        ssh ${local.ssh_opts} ${var.ssh_user}@$W 'bash -s' < ./join-command.sh
        echo "[bootstrap] worker $W joined"
      done
      echo "[bootstrap] waiting for nodes Ready ..."
      for i in $(seq 1 60); do
        N=$(kubectl --kubeconfig ./kubeconfig get nodes --no-headers 2>/dev/null | grep -c ' Ready' || true)
        [ "$N" -ge $((var.worker_count + 1)) ] && break
        sleep 10
      done
      kubectl --kubeconfig ./kubeconfig get nodes
      echo "[bootstrap] done. kubeconfig written to ./kubeconfig"
    EOT
  }

  depends_on = [openstack_networking_floatingip_associate_v2.cp, openstack_compute_instance_v2.workers]
}
