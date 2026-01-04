# Scripts de Desarrollo

## start-dev.sh
Inicia la aplicación en modo desarrollo local:
- Lee variables desde el archivo `.env`
- Valida que todas las variables requeridas existan
- Inicia PostgreSQL con Docker si no está corriendo
- Espera a que la base de datos esté lista
- Aplicación: Spring Boot local
- Perfil: `local

Uso:
```bash
./scripts/start-dev.sh
```

## start-docker.sh
Inicia la aplicación completa en Docker:
- Lee variables desde el archivo `.env`
- Valida que todas las variables requeridas existan
- Inicia PostgreSQL con Docker Compose
- Espera a que la base de datos esté lista
- Aplicación: Spring Boot local (con perfil docker)
- Perfil: `docker`

Uso:
```bash
./scripts/start-docker.sh
```

## Variables de Entorno Requeridas

Ambos scripts requieren las siguientes variables en el archivo `.env`:

```bash
SPRING_PROFILES_ACTIVE=local
DB_HOST=localhost
DB_PORT=5432
DB_NAME=payouts
DB_USER=payout_user
DB_PASSWORD=payout_pass
SERVER_PORT=8080
```

## Características

- ✅ Validación de existencia del archivo `.env`
- ✅ Validación de variables requeridas
- ✅ Output colorido para mejor legibilidad
- ✅ Manejo de errores con `set -e`
- ✅ Espera inteligente de la base de datos
- ✅ Verificación del estado de PostgreSQL

## Requisitos
- Docker y Docker Compose instalados
- Maven wrapper disponible en la raíz del proyecto
- Archivo `.env` configurado en la raíz del proyecto
