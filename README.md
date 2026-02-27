# Chat App — Real-time Messaging

Пет-проект: мессенджер с WebSocket (Go) + REST API (Java/Spring Boot), развёрнутый в Kubernetes через Helm.

## Архитектура

```
Client (Browser/Mobile)
        │
        ▼
  NGINX Ingress
  /ws  ──────────►  Go Gateway        (WebSocket Hub, fan-out по комнатам)
  /api ──────────►  Java Spring Boot  (Auth, Rooms, Messages REST)
                         │                     │
                    PostgreSQL             Redis (cache)
```

**Почему два сервиса:**
- **Go** идеально держит тысячи concurrent WebSocket соединений — minimal memory per goroutine
- **Java/Spring** удобен для сложной бизнес-логики, JPA/миграций, security

## Структура проекта

```
chat-app/
├── go-gateway/         # Go WebSocket сервис
│   ├── main.go         # HTTP сервер, роутинг
│   ├── hub.go          # Hub: регистрация клиентов, broadcast по комнатам
│   ├── client.go       # WebSocket клиент: readPump / writePump goroutines
│   ├── go.mod
│   └── Dockerfile
├── java-service/       # Spring Boot REST сервис
│   ├── src/main/java/com/chat/
│   │   ├── entity/     # User, Room, Message (JPA)
│   │   ├── repository/ # Spring Data JPA репозитории
│   │   ├── service/    # AuthService, RoomService, MessageService
│   │   ├── controller/ # AuthController, RoomController
│   │   ├── security/   # JwtService, SecurityConfig
│   │   └── dto/        # Request/Response records
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/changelog/  # Liquibase миграции
│   ├── pom.xml
│   └── Dockerfile
├── helm/chat-app/      # Helm chart для Kubernetes
│   ├── Chart.yaml      # Зависимости: bitnami/postgresql, bitnami/redis
│   ├── values.yaml
│   └── templates/
│       ├── gateway.yaml
│       ├── chat-service.yaml
│       ├── ingress-secrets.yaml
│       └── _helpers.tpl
└── docker/
    └── docker-compose.yml  # Для локальной разработки
```

## Быстрый старт (локально)

```bash
cd docker
docker compose up --build
```

Сервисы:
- Go Gateway: http://localhost:8080
- Java Service: http://localhost:8081

## REST API

### Auth
```
POST /api/auth/register   { username, email, password, displayName }
POST /api/auth/login      { username, password }
→ { token, userId, username, displayName }
```

### Rooms
```
GET  /api/rooms                          # Мои комнаты
POST /api/rooms                          # Создать комнату
POST /api/rooms/{id}/members/{userId}    # Добавить участника
```

### Messages
```
GET  /api/rooms/{id}/messages?page=0&size=50   # История
POST /api/rooms/{id}/messages                  # Сохранить сообщение
```

### WebSocket

Подключение: `ws://host/ws/{roomId}?userId={userId}`

Входящее сообщение (клиент → сервер):
```json
{ "content": "Hello!", "type": "message" }
```

Исходящее сообщение (сервер → клиент):
```json
{ "roomId": "...", "userId": "...", "content": "Hello!", "type": "message" }
```

Типы: `message`, `join`, `leave`, `typing`

## Деплой в Kubernetes

### Предварительные требования

```bash
# На Ubuntu home server
sudo snap install microk8s --classic
microk8s enable dns ingress storage
alias kubectl='microk8s kubectl'
alias helm='microk8s helm'
```

### Собрать и запушить образы

```bash
# Если используешь локальный registry в microk8s
microk8s enable registry

# Go gateway
cd go-gateway
docker build -t localhost:32000/chat-gateway:latest .
docker push localhost:32000/chat-gateway:latest

# Java service
cd ../java-service
docker build -t localhost:32000/chat-service:latest .
docker push localhost:32000/chat-service:latest
```

### Обновить values.yaml

```yaml
gateway:
  image:
    repository: localhost:32000/chat-gateway

chatService:
  image:
    repository: localhost:32000/chat-service
  env:
    JWT_SECRET: "your-strong-secret-min-32-chars-here"
```

### Установить через Helm

```bash
cd helm
helm repo add bitnami https://charts.bitnami.com/bitnami
helm dependency update ./chat-app

helm upgrade --install chat ./chat-app \
  --namespace chat \
  --create-namespace \
  --values ./chat-app/values.yaml
```

### Проверить

```bash
kubectl get pods -n chat
kubectl get ingress -n chat
kubectl logs -n chat deployment/chat-gateway -f
```

### Добавить /etc/hosts для локального доступа

```
127.0.0.1  chat.local
```

## Дальнейшее развитие

- **Масштабирование Go Gateway**: при нескольких репликах нужен Redis Pub/Sub для межнодового broadcast
- **Индикатор печатания**: отправлять type=`typing`, не сохранять в БД
- **Онлайн-статус**: хранить userId → lastSeen в Redis с TTL
- **Файлы/медиа**: S3-совместимое хранилище (MinIO для home server)
- **Frontend**: React/Next.js или мобильное приложение

## Технологии

| Компонент | Технология |
|-----------|-----------|
| Real-time | Go + gorilla/websocket |
| REST API | Java 21 + Spring Boot 3.2 |
| База данных | PostgreSQL 16 |
| Кэш | Redis 7 |
| Миграции | Liquibase |
| Контейнеры | Docker |
| Оркестрация | Kubernetes (MicroK8s) |
| Деплой | Helm |
