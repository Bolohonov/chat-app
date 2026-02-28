#!/bin/bash
set -e

# ==========================================
# Configuration
# ==========================================
APP=${1:-chat-app}
TAG=${2:-latest}

VPS="root@77.222.35.2"
SERVER="michael@10.0.0.2"
REMOTE_BUILD_DIR="/home/michael/build/$APP"
REMOTE_SECRETS_DIR="/home/michael/secrets/$APP"
HELM_CHART="./helm/$APP"

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log()  { echo -e "${GREEN}[deploy]${NC} $1"; }
warn() { echo -e "${YELLOW}[warn]${NC} $1"; }
fail() { echo -e "${RED}[error]${NC} $1"; exit 1; }

# ==========================================
# 1. Проверки
# ==========================================
log "Starting deploy of $APP:$TAG"

[ ! -d "$HELM_CHART" ] && fail "Helm chart not found: $HELM_CHART"
[ ! -f "$HELM_CHART/values.example.yaml" ] && fail "values.example.yaml not found"

# ==========================================
# 2. Копируем исходники на сервер
# ==========================================
log "Syncing sources to server..."

ssh -J $VPS $SERVER "mkdir -p $REMOTE_BUILD_DIR"

# Копируем каждый сервис
for SERVICE in java-service go-gateway frontend; do
  if [ -d "./$SERVICE" ]; then
    log "  Syncing $SERVICE..."
    rsync -az --delete \
      --exclude='target/' \
      --exclude='node_modules/' \
      --exclude='.git/' \
      --exclude='*.class' \
      -e "ssh -J $VPS" \
      ./$SERVICE/ $SERVER:$REMOTE_BUILD_DIR/$SERVICE/
  fi
done

# ==========================================
# 3. Собираем образы на сервере
# ==========================================
log "Building images on server..."

ssh -J $VPS $SERVER bash << EOF
set -e

REGISTRY="localhost:5000"
TAG="$TAG"
BUILD_DIR="$REMOTE_BUILD_DIR"

echo "[server] Building chat-service..."
docker build -t \$REGISTRY/chat-service:\$TAG \$BUILD_DIR/java-service/

echo "[server] Building chat-gateway..."
docker build -t \$REGISTRY/chat-gateway:\$TAG \$BUILD_DIR/go-gateway/

echo "[server] Building chat-frontend..."
docker build -t \$REGISTRY/chat-frontend:\$TAG \$BUILD_DIR/frontend/

echo "[server] Pushing to local registry..."
docker push \$REGISTRY/chat-service:\$TAG
docker push \$REGISTRY/chat-gateway:\$TAG
docker push \$REGISTRY/chat-frontend:\$TAG

echo "[server] Done building"
EOF

# ==========================================
# 4. Копируем helm chart на сервер
# ==========================================
log "Syncing helm chart..."

ssh -J $VPS $SERVER "mkdir -p $REMOTE_BUILD_DIR/helm"

rsync -az --delete \
  --exclude='values.prod.yaml' \
  -e "ssh -J $VPS" \
  $HELM_CHART/ $SERVER:$REMOTE_BUILD_DIR/helm/

# ==========================================
# 5. Деплоим через helm
# ==========================================
log "Deploying to k3s..."

ssh -J $VPS $SERVER bash << EOF
set -e

SECRETS="$REMOTE_SECRETS_DIR/values.prod.yaml"
CHART="$REMOTE_BUILD_DIR/helm"

[ ! -f "\$SECRETS" ] && echo "ERROR: values.prod.yaml not found at \$SECRETS" && exit 1

helm upgrade --install $APP \$CHART \
  --values \$SECRETS \
  --set chatService.image.tag=$TAG \
  --set gateway.image.tag=$TAG \
  --set frontend.image.tag=$TAG \

echo "[server] Checking rollout..."
kubectl rollout status deployment/$APP-service
kubectl rollout status deployment/$APP-gateway
kubectl rollout status deployment/$APP-frontend

echo "[server] Deploy complete"
EOF

# ==========================================
# 6. Итог
# ==========================================
log "✓ Deploy of $APP:$TAG complete"