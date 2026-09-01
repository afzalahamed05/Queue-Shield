// Dev default for `ng serve` (matches Docker Compose's gateway port). In the built/containerized
// app, docker-entrypoint.sh regenerates this file from the API_BASE_URL env var at container
// startup - see frontend/Dockerfile and docker-entrypoint.sh. Keeping the API URL out of the
// Angular build entirely means the same image works against Compose (8090) or kind (9190)
// without a rebuild.
window.__env = {
  apiBaseUrl: 'http://localhost:8090/api'
};
