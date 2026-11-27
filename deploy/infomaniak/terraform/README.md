# Terraform — Infomaniak Public Cloud (OpenStack) + Kubernetes

Creates the **whole infra as code** on Infomaniak Public Cloud and deploys the app on a
self-managed Kubernetes cluster (kubeadm on OpenStack VMs):

```
infra/   network, subnet, router, security groups
         control-plane + N worker VMs (cloud-init: containerd + kubeadm + flannel)
         bootstrap via SSH: kubeadm init, join workers, writes ./kubeconfig
         Octavia load balancer (VIP floating IP) -> nodes:<app_nodeport>,
             health monitor GET /actuator/health (200, 15s/5s/3)
app/     Kubernetes resources (namespace, Secret, Deployment with /actuator/health
         probes, Service type NodePort) applied with the generated kubeconfig
```

## Prerequisites
1. Terraform >= 1.6, OpenStack CLI, kubectl, ssh keypair uploaded:
   `openstack keypair create mykey --public-key ~/.ssh/id_rsa.pub`
2. Infomaniak Public Cloud credentials: download the **OpenStack RC file** in the
   Infomaniak console and `source openrc.sh` (sets `OS_*`), or configure
   `~/.config/openstack/clouds.yaml` with a `infomaniak` entry and set
   `cloud_name = "infomaniak"`.
3. The container image published by CI: `ghcr.io/cyrilsx/russian_cases/backend:latest`
   (make the ghcr package **public** first, see README).

## Deploy
```bash
cd deploy/infomaniak/terraform/infra
cp terraform.tfvars.example terraform.tfvars   # adjust flavors/zone/keypair
terraform init
terraform apply                                # ~10 min: VMs + kubeadm + LB

cd ../app
terraform init
terraform apply \
  -var="app_jwt_secret=$(openssl rand -base64 64)" \
  -var="app_admin_password=<strong password>" \
  -var="datasource_url=jdbc:postgresql://<aiven-pg>:5432/russiancases" \
  -var="datasource_username=rc" -var="datasource_password=<db pw>"
```

Outputs of `infra`: `lb_vip_public_ip` (app URL: `http://<ip>/`, health `…/actuator/health`),
`control_plane_floating_ip`, `kubeconfig_path`.

## Destroy
```bash
cd app && terraform destroy
cd ../infra && terraform destroy
```

## Notes
- The LB health monitor uses the public `/actuator/health` endpoint (PR #26) — unhealthy
  nodes are removed from the pool automatically.
- TLS: add nginx-ingress + cert-manager later and front the LB with 443; the app is
  served over HTTP on port 80 of the LB VIP for now.
- Secrets are passed as sensitive Terraform variables (never committed); use a remote
  backend (e.g. S3/Swift) for the state in a team setup.
