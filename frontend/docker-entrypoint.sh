#!/bin/sh
set -e

# Regenerate env-config.js from the API_BASE_URL env var before nginx starts serving it - this is
# what lets the same built image point at a different gateway per environment (Compose vs. kind)
# without a separate Angular build for each.
cat > /usr/share/nginx/html/env-config.js <<EOF
window.__env = {
  apiBaseUrl: '${API_BASE_URL:-http://localhost:8090/api}'
};
EOF

exec "$@"
