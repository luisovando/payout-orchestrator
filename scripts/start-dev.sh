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

echo -e "${GREEN}Environment variables validated successfully:${NC}"
echo -e "  DB_HOST: ${YELLOW}$DB_HOST${NC}"
echo -e "  DB_PORT: ${YELLOW}$DB_PORT${NC}"
echo -e "  DB_NAME: ${YELLOW}$DB_NAME${NC}"
echo -e "  DB_USER: ${YELLOW}$DB_USER${NC}"
echo -e "  SERVER_PORT: ${YELLOW}$SERVER_PORT${NC}"

# Check if PostgreSQL is running
echo -e "${GREEN}Ensuring PostgreSQL is running...${NC}"
docker compose -f "$PROJECT_ROOT/docker-compose.local.yml" up -d postgres

echo -e "${YELLOW}Waiting for PostgreSQL healthcheck...${NC}"
for i in {1..30}; do
  STATUS=$(docker inspect --format='{{.State.Health.Status}}' "$(docker compose -f "$PROJECT_ROOT/docker-compose.local.yml" ps -q postgres)" 2>/dev/null || true)
  if [ "$STATUS" = "healthy" ]; then
    echo -e "${GREEN}Database is healthy!${NC}"
    break
  fi
  echo -n "."
  sleep 1
done
echo

if [ "$STATUS" != "healthy" ]; then
  echo -e "${RED}Error: Database did not become healthy in time.${NC}"
  docker compose -f "$PROJECT_ROOT/docker-compose.local.yml" logs postgres --tail=50
  exit 1
fi

echo -e "${GREEN}Starting Spring Boot application...${NC}"
export SPRING_PROFILES_ACTIVE=local

cd "$PROJECT_ROOT"
./mvnw spring-boot:run
