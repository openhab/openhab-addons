#!/usr/bin/env bash
set -e

VOLUME_NAME="hasslink_ha_fixture_data"
CONTAINER_NAME="hasslink_test_ha"
PORT=9123
BASE_URL="http://localhost:${PORT}"

echo "==> Cleaning up previous Docker resources..."
docker stop "$CONTAINER_NAME" 2>/dev/null || true
docker volume rm "$VOLUME_NAME" 2>/dev/null || true

echo "==> Creating clean Docker volume..."
docker volume create "$VOLUME_NAME"

echo "==> Pre-configuring Home Assistant..."
docker run --rm \
  -v "$VOLUME_NAME:/config" \
  alpine sh -c 'cat <<EOF > /config/configuration.yaml
default_config:
demo:
EOF'

echo "==> Starting Home Assistant container on port ${PORT}..."
docker run -d \
  --name "$CONTAINER_NAME" \
  --rm \
  -p "${PORT}:8123" \
  -v "$VOLUME_NAME:/config" \
  ghcr.io/home-assistant/home-assistant:stable

echo "==> Waiting for Home Assistant API on port ${PORT}..."
until curl -s "$BASE_URL/api/" > /dev/null; do
  sleep 2
done

echo "==> Waiting for onboarding endpoint..."
until [ "$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/onboarding/users")" -ne 000 ]; do
  sleep 2
done

echo "==> Creating initial user..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/onboarding/users" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"FixtureGen\",
    \"username\": \"fixturegen\",
    \"password\": \"FixturePassword123!\",
    \"client_id\": \"$BASE_URL/\",
    \"language\": \"en\"
  }")

AUTH_CODE=$(echo "$RESPONSE" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('auth_code', ''))" 2>/dev/null || true)

if [ -z "$AUTH_CODE" ]; then
  echo "Error: Failed to obtain auth_code. Response was:"
  echo "$RESPONSE"
  exit 1
fi

echo "==> Exchanging auth code for access token..."
TOKEN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code&code=${AUTH_CODE}&client_id=${BASE_URL}/")

TOKEN=$(echo "$TOKEN_RESPONSE" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('access_token', ''))" 2>/dev/null || true)

if [ -z "$TOKEN" ]; then
  echo "Error: Failed to generate access token. Response was:"
  echo "$TOKEN_RESPONSE"
  exit 1
fi

# Complete remaining onboarding steps
curl -s -X POST "$BASE_URL/api/onboarding/core_config" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"location_name": "Home", "time_zone": "UTC", "elevation": 0, "unit_system": "metric", "currency": "USD"}' > /dev/null

curl -s -X POST "$BASE_URL/api/onboarding/analytics" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"preferences": {}}' > /dev/null

echo "$TOKEN" > .token
echo "==> Token successfully generated and saved to tools/.token"