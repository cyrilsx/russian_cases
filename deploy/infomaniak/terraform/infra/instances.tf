# Kubernetes control plane + workers, bootstrapped with cloud-init (kubeadm).

locals {
  k8s_apt = <<-EOT
    curl -fsSL https://pkgs.k8s.io/core:/stable:/${var.k8s_version}/deb/Release.key | gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
    echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/${var.k8s_version}/deb/ /" > /etc/apt/sources.list.d/kubernetes.list
    apt-get update
    apt-get install -y kubelet kubeadm kubectl
    systemctl enable --now kubelet
  EOT

  base_cloud_init = <<-EOT
    #cloud-config
    package_update: true
    packages:
      - apt-transport-https
      - ca-certificates
      - curl
      - gnupg
      - containerd
    runcmd:
      - systemctl enable --now containerd
      - ${local.k8s_apt}
  EOT

  control_plane_cloud_init = templatefile("${path.module}/cloud-init-control.yaml.tftpl", {
    k8s_version = var.k8s_version
    pod_cidr    = var.pod_cidr
    cp_ip       = openstack_networking_port_v2.control[0].fixed_ip[0].ip_address
  })

  worker_cloud_init = local.base_cloud_init
}

resource "openstack_networking_port_v2" "control" {
  count          = 1
  name           = "${var.name_prefix}-cp"
  network_id     = openstack_networking_network_v2.net.id
  security_group_ids = [openstack_networking_secgroup_v2.cluster.id]
  fixed_ip {
    subnet_id = openstack_networking_subnet_v2.subnet.id
  }
}

resource "openstack_networking_port_v2" "workers" {
  count          = var.worker_count
  name           = "${var.name_prefix}-worker-${count.index}"
  network_id     = openstack_networking_network_v2.net.id
  security_group_ids = [openstack_networking_secgroup_v2.cluster.id]
  fixed_ip {
    subnet_id = openstack_networking_subnet_v2.subnet.id
  }
}

resource "openstack_compute_instance_v2" "control" {
  count       = 1
  name        = "${var.name_prefix}-cp"
  flavor_name = var.flavor_control
  image_name  = var.image
  key_pair    = var.ssh_keypair
  availability_zone = var.zone

  network {
    port = openstack_networking_port_v2.control[count.index].id
  }

  user_data = local.control_plane_cloud_init

  depends_on = [openstack_networking_router_interface_v2.gw]
}

resource "openstack_compute_instance_v2" "workers" {
  count       = var.worker_count
  name        = "${var.name_prefix}-worker-${count.index}"
  flavor_name = var.flavor_worker
  image_name  = var.image
  key_pair    = var.ssh_keypair
  availability_zone = var.zone

  network {
    port = openstack_networking_port_v2.workers[count.index].id
  }

  user_data = local.worker_cloud_init

  depends_on = [openstack_networking_router_interface_v2.gw, openstack_compute_instance_v2.control]
}

# Floating IPs for the control plane (SSH/kubeconfig) and the LB VIP
resource "openstack_networking_floatingip_v2" "cp" {
  pool = "public"
}
resource "openstack_networking_floatingip_associate_v2" "cp" {
  port_id     = openstack_networking_port_v2.control[0].id
  floating_ip = openstack_networking_floatingip_v2.cp.address
}

resource "openstack_networking_floatingip_v2" "lb" {
  pool = "public"
}
