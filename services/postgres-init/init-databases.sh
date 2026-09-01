#!/bin/sh
set -e

for db in incident_db priority_db resource_db responder_db shelter_db assignment_db notification_db; do
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -c "CREATE DATABASE $db;"
done
