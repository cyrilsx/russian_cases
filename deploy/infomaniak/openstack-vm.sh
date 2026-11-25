#!/usr/bin/env bash
# Infomaniak Public Cloud (OpenStack) — single-VM deployment of Russian Cases,
# with an Octavia load balancer whose health monitor uses /actuator/health.
#
# Prerequisites:
#   - openstack CLI configured with your Infomaniak Public Cloud credentials
#     (download the OpenStack RC file from the Infomaniak console, then: source openrc.sh)
#   - an SSH keypair registered:  openstack keypair create mykey --public-key ~/.ssh/id_rsa.pub
#   - the backend jar built (CI artifact 'russian-cases-backend-jar' or mvn -pl webapp,backend package)
#
# Usage:  ./deploy/infomaniak/openstack-vm.sh <flavor> <image> <zone>
#         e.g. ./deploy/infomaniak/openstack-vm.sh a4-ram8-disk50-perf1 "Ubuntu 24.04" nova
set -euo pipefail

FLAVOR="${1:-a4-ram8-disk50-perf1}"
IMAGE="${2:-Ubuntu 24.04 LTS}"
ZONE="${3:-nova}"
NAME="russian-cases"
SG="russian-cases-sg"
PORT=8080

# 1. Security group: SSH from anywhere (restrict to your IP), 80/443 public, 8080 from the LB only later
echo "== security group =="
openstack security group create "$SG" --description "russian-cases" || true
openstack security group rule create --protocol tcp --dst-port 22 "$SG" || true
openstack security group rule create --protocol tcp --dst-port 80 "$SG" || true
openstack security group rule create --protocol tcp --dst-port 443 "$SG" || true
openstack security group rule create --protocol tcp --dst-port "$PORT" "$SG" || true

# 2. Instance + floating IP
echo "== instance =="
openstack server create --flavor "$FLAVOR" --image "$IMAGE" --key-name mykey \
  --security-group "$SG" --zone "$ZONE" --wait "$NAME"
FIP=$(openstack floating ip create -f value -c floating_ip_address "public" | head -1)
openstack server add floating ip "$NAME" "$FIP"
echo "floating IP: $FIP"

# 3. Deploy the app on the VM (Temurin 25, systemd, prod profile with env secrets)
echo "== deploy app on $FIP =="
scp backend/target/backend-1.0.0-SNAPSHOT.jar "ubuntu@$FIP":/tmp/app.jar
ssh "ubuntu@$FIP" bash -s <<'REMOTE'
set -euo pipefail
curl -L -o /tmp/jdk25.tar.gz "https://api.adoptium.net/v3/binary/latest/25/ga/linux/x64/jdk/hotspot/normal/eclipse"
sudo mkdir -p /opt/jdk25 && sudo tar xzf /tmp/jdk25.tar.gz -C /opt/jdk25 --strip-components=1
sudo mkdir -p /opt/russian-cases && sudo cp /tmp/app.jar /opt/russian-cases/app.jar
sudo tee /etc/systemd/system/russian-cases.service >/dev/null <<'UNIT'
[Unit]
Description=Russian Cases
After=network.target
[Service]
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="SERVER_PORT=8080"
Environment="APP_JWT_SECRET=CHANGE_ME_openssl_rand_base64_64"
Environment="APP_ADMIN_PASSWORD=CHANGE_ME"
# PostgreSQL (e.g. Aiven on Infomaniak) instead of in-memory H2:
# Environment="SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/russiancases"
# Environment="SPRING_DATASOURCE_USERNAME=rc"
# Environment="SPRING_DATASOURCE_PASSWORD=..."
# Environment="SPRING_DATASOURCE_DRIVER=org.postgresql.Driver"
ExecStart=/opt/jdk25/bin/java -jar /opt/russian-cases/app.jar
Restart=always
[Install]
WantedBy=multi-user.target
UNIT
sudo systemctl daemon-reload && sudo systemctl enable --now russian-cases
REMOTE

# 4. Octavia load balancer in front, health check on the public endpoint
echo "== load balancer =="
LB=$(openstack loadbalancer create --name "$NAME-lb" --vip-subnet-id public -f value -c id)
openstack loadbalancer listener create --name "$NAME-http" --protocol HTTP --protocol-port 80 \
  --loadbalancer "$LB" -f value -c id >/dev/null
POOL=$(openstack loadbalancer pool create --name "$NAME-pool" --protocol HTTP --lb-algorithm ROUND_ROBIN \
  --listener "$NAME-http" -f value -c id)
# Health monitor: GET /actuator/health, expect 200, every 15s, 3 failures => unhealthy
openstack loadbalancer healthmonitor create --name "$NAME-hc" --pool "$POOL" \
  --type HTTP --url-path /actuator/health --http-method GET --expected-codes 200 \
  --delay 15 --timeout 5 --max-retries 3
PORT_ID=$(openstack server show "$NAME" -f json | python3 -c "import sys,json; print(list(json.load(sys.stdin)['addresses'].values())[0][0]['OS-EXT-IPS-MAC:mac_addr'] and '')" 2>/dev/null || true)
NETPORT=$(openstack port list --server "$NAME" -f value -c id | head -1)
openstack loadbalancer member create --name "$NAME-1" --pool "$POOL" \
  --subnet-id "$(openstack port show "$NETPORT" -f value -c fixed_ips | python3 -c 'import sys,json; print(json.loads(sys.stdin.read())[0]["subnet_id"])')" \
  --address "$(openstack port show "$NETPORT" -f json | python3 -c 'import sys,json; print(json.loads(sys.stdin.read())["fixed_ips"][0]["ip_address"])')" \
  --protocol-port "$PORT"
openstack loadbalancer vip set --name "$NAME-vip" "$LB" 2>/dev/null || true
VIP=$(openstack loadbalancer show "$LB" -f value -c vip_address)
echo "LB VIP: $VIP  ->  health check: http://$VIP/actuator/health"
echo "Done. App: http://$FIP:$PORT  (behind LB: http://$VIP)  — put nginx+certbot in front for HTTPS."
