#!/bin/bash
# Deployment script for Email Notification Microservice

set -e

# Configuration
APP_NAME="email-notification-service"
APP_PORT="${APP_PORT:-8085}"
DEPLOY_DIR="/opt/${APP_NAME}"

echo "=== Deploying ${APP_NAME} ==="

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Docker is not installed. Installing..."
    curl -fsSL https://get.docker.com | sh
    sudo systemctl enable docker
    sudo systemctl start docker
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "Docker Compose is not installed. Installing..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi

# Create deployment directory
sudo mkdir -p "${DEPLOY_DIR}"
sudo cp -r . "${DEPLOY_DIR}/"
cd "${DEPLOY_DIR}"

# Build and start containers
echo "Building and starting containers..."
docker-compose down 2>/dev/null || true
docker-compose up -d --build

# Wait for health check
echo "Waiting for service to be healthy..."
sleep 30

# Check if service is running
if curl -s "http://localhost:${APP_PORT}/api/v1/templates" > /dev/null; then
    echo ""
    echo "=== Deployment Successful ==="
    echo "Service is running at: http://localhost:${APP_PORT}"
    echo "MailHog UI: http://localhost:8025"
    echo ""
    echo "API Endpoints:"
    echo "  - POST /api/v1/emails/send"
    echo "  - GET  /api/v1/emails/{id}/status"
    echo "  - GET  /api/v1/templates"
else
    echo "Service may still be starting. Check with: docker-compose logs -f"
fi
