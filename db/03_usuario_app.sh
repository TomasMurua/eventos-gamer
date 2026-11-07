#!/bin/sh
# Crea el usuario que usa la API, con permisos minimos sobre eventos_gamer:
# solo lectura en todas las tablas, y escritura unicamente en usuarios (para fijar las claves al arrancar).
# La clave llega por la variable de entorno DB_APP_PASSWORD; nunca queda escrita en el repositorio.
set -e
if [ -z "$DB_APP_PASSWORD" ]; then
  echo "Falta DB_APP_PASSWORD" >&2
  exit 1
fi
mysql -uroot -p"$MYSQL_ROOT_PASSWORD" <<SQL
CREATE USER IF NOT EXISTS 'ljcs_app'@'%' IDENTIFIED BY '${DB_APP_PASSWORD}';
GRANT SELECT ON eventos_gamer.* TO 'ljcs_app'@'%';
GRANT INSERT, UPDATE ON eventos_gamer.usuarios TO 'ljcs_app'@'%';
FLUSH PRIVILEGES;
SQL
