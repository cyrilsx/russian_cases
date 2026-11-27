# Network, subnet, router and security groups for the cluster + LB.

resource "openstack_networking_network_v2" "net" {
  name = "${var.name_prefix}-net"
}

resource "openstack_networking_subnet_v2" "subnet" {
  name       = "${var.name_prefix}-subnet"
  network_id = openstack_networking_network_v2.net.id
  cidr       = "10.0.0.0/24"
  ip_version = 4
  dns_nameservers = ["1.1.1.1", "9.9.9.9"]
}

resource "openstack_networking_router_v2" "router" {
  name                = "${var.name_prefix}-router"
  external_network_id = "public"
}

resource "openstack_networking_router_interface_v2" "gw" {
  router_id = openstack_networking_router_v2.router.id
  subnet_id = openstack_networking_subnet_v2.subnet.id
}

resource "openstack_networking_secgroup_v2" "cluster" {
  name        = "${var.name_prefix}-sg"
  description = "Kubernetes cluster + app"
}

# SSH (restrict to your IP in production via remote_ip_prefix)
resource "openstack_networking_secgroup_rule_v2" "ssh" {
  security_group_id = openstack_networking_secgroup_v2.cluster.id
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 22
  port_range_max    = 22
  remote_ip_prefix  = "0.0.0.0/0"
}

# Kubernetes API
resource "openstack_networking_secgroup_rule_v2" "k8s_api" {
  security_group_id = openstack_networking_secgroup_v2.cluster.id
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 6443
  port_range_max    = 6443
  remote_ip_prefix  = "0.0.0.0/0"
}

# NodePort range (app behind the Octavia LB)
resource "openstack_networking_secgroup_rule_v2" "nodeport" {
  security_group_id = openstack_networking_secgroup_v2.cluster.id
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 30000
  port_range_max    = 32767
  remote_ip_prefix  = "0.0.0.0/0"
}

# All traffic inside the cluster network (control plane, etcd, flannel, kubelet)
resource "openstack_networking_secgroup_rule_v2" "internal" {
  security_group_id = openstack_networking_secgroup_v2.cluster.id
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  remote_ip_prefix  = openstack_networking_subnet_v2.subnet.cidr
}
resource "openstack_networking_secgroup_rule_v2" "internal_udp" {
  security_group_id = openstack_networking_secgroup_v2.cluster.id
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "udp"
  remote_ip_prefix  = openstack_networking_subnet_v2.subnet.cidr
}

# ICMP inside the cluster
resource "openstack_networking_secgroup_rule_v2" "icmp" {
  security_group_id = openstack_networking_secgroup_v2.cluster.id
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "icmp"
  remote_ip_prefix  = openstack_networking_subnet_v2.subnet.cidr
}
