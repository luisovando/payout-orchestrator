#!/bin/bash

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ENV_FILE="$PROJECT_ROOT/.env"

# Check if .env file exists
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}Error: .env file not found at $ENV_FILE${NC}"
    echo "Please create the .env file with the required environment variables."
    echo "Project root: $PROJECT_ROOT"
    exit 1
fi

echo -e "${GREEN}Loading environment variables from .env file...${NC}"

# Load environment variables from .env file
set -a
source "$ENV_FILE"
set +a

# Validate required variables
REQUIRED_VARS=("DB_HOST" "DB_PORT" "DB_NAME" "DB_USER" "DB_PASSWORD" "SERVER_PORT")
for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        echo -e "${RED}Error: Required environment variable $var is not set${NC}"
        exit 1
    fi
done

echo -e "${GREEN}Starting PostgreSQL with Docker Compose...${NC}"
docker compose -f "$PROJECT_ROOT/docker-compose.local.yml" up -d postgres

echo -e "${YELLOW}Waiting for database to be ready...${NC}"
for i in {1..30}; do
    if docker compose -f "$PROJECT_ROOT/docker-compose.local.yml" exec -T postgres pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
        echo -e "${GREEN}Database is ready!${NC}"
        break
    fi
    echo -n "."
    sleep 1
done
echo

echo -e "${GREEN}Starting Spring Boot application with Docker profile...${NC}"
export SPRING_PROFILES_ACTIVE=docker
export DB_HOST=postgres  # Override for Docker networking
export DB_PORT=5432
export DB_NAME=payouts
export DB_USER=payout_user
export DB_PASSWORD=payout_pass
export SERVER_PORT=8080

cd "$PROJECT_ROOT"
./mvnw spring-boot:run
