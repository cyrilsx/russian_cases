# Octavia load balancer in front of the app NodePort, health check on /actuator/health.

resource "openstack_lb_loadbalancer_v2" "app" {
  name         = "${var.name_prefix}-lb"
  vip_subnet_id = openstack_networking_subnet_v2.subnet.id
  vip_address  = cidrhost(openstack_networking_subnet_v2.subnet.cidr, 100)

  depends_on = [openstack_networking_router_interface_v2.gw]
}

# associate the public floating IP with the LB VIP
resource "openstack_networking_floatingip_associate_v2" "lb" {
  floating_ip = openstack_networking_floatingip_v2.lb.address
  port_id     = openstack_lb_loadbalancer_v2.app.vip_port_id
}

resource "openstack_lb_listener_v2" "http" {
  name            = "${var.name_prefix}-http"
  protocol        = "HTTP"
  protocol_port   = 80
  loadbalancer_id = openstack_lb_loadbalancer_v2.app.id
}

resource "openstack_lb_pool_v2" "app" {
  name        = "${var.name_prefix}-pool"
  protocol    = "HTTP"
  lb_method   = "ROUND_ROBIN"
  listener_id = openstack_lb_listener_v2.http.id
}

resource "openstack_lb_monitor_v2" "app" {
  name           = "${var.name_prefix}-hc"
  pool_id        = openstack_lb_pool_v2.app.id
  type           = "HTTP"
  url_path       = "/actuator/health"
  http_method    = "GET"
  expected_codes = "200"
  delay          = 15
  timeout        = 5
  max_retries    = 3
}

# LB members: every k8s node, on the app NodePort
resource "openstack_lb_member_v2" "nodes" {
  count         = 1 + var.worker_count
  name          = "${var.name_prefix}-node-${count.index}"
  pool_id       = openstack_lb_pool_v2.app.id
  address       = count.index == 0 ? local.cp_priv : local.worker_ips[count.index - 1]
  protocol_port = var.app_nodeport
  subnet_id     = openstack_networking_subnet_v2.subnet.id
}
