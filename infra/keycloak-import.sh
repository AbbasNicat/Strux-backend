#!/bin/bash

# ==========================================
# Keycloak Realm Import Script
# Keycloak-ı avtomatik konfiqurasiya edir
# ==========================================

set -e

echo "⏳ Keycloak-ın başlamasını gözləyirik..."
sleep 30

echo "🔑 Keycloak realm import edilir..."

# Keycloak container-də realm import et
docker exec -it strux-keycloak /opt/keycloak/bin/kc.sh import \
  --file /tmp/keycloak-realm-strux.json \
  --override true

echo "✅ Keycloak realm uğurla import edildi!"
echo ""
echo "📋 Məlumatlar:"
echo "   Realm: Strux-realm"
echo "   Client ID: Strux-backend"
echo "   Client Secret: APWka3cMRiX8YVtfdVp1L8nC4YyWseK6"
echo "   Admin User: admin / admin123"
echo ""
echo "🌐 Keycloak URL: http://77.42.73.35:8080"
