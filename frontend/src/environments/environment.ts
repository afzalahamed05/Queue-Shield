declare global {
  interface Window {
    __env?: { apiBaseUrl?: string };
  }
}

export const environment = {
  production: false,
  // Phase 2: the frontend talks to the API gateway only, never to a domain service directly.
  // The gateway (services/gateway) routes /api/incidents/** to incident-service, /api/priorities/**
  // to priority-service, and so on - see the README's "API boundaries" section.
  //
  // Resolved at runtime from assets/env-config.js (regenerated per-container from API_BASE_URL -
  // see frontend/docker-entrypoint.sh), not baked in at build time. That's what lets the same
  // Docker image work against Compose's gateway (port 8090) and kind's (port 9190) without a
  // separate build for each. The literal fallback here only matters for `ng serve`.
  get apiBaseUrl(): string {
    return window.__env?.apiBaseUrl ?? 'http://localhost:8090/api';
  },
};
