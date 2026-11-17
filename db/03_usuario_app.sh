#!/bin/sh
# Crea el usuario que usa la API, con permisos minimos sobre eventos_gamer: lectura en todas las tablas y
# escritura solo donde la aplicacion la necesita (registro, publicar eventos, fotos y videos, comentarios,
# valoraciones y compartidos). No puede borrar eventos ni usuarios, ni cambiar el esquema.
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
GRANT INSERT ON eventos_gamer.eventos TO 'ljcs_app'@'%';
GRANT INSERT, DELETE ON eventos_gamer.evento_medios TO 'ljcs_app'@'%';
GRANT INSERT, DELETE ON eventos_gamer.evento_comentarios TO 'ljcs_app'@'%';
GRANT INSERT, UPDATE ON eventos_gamer.evento_valoraciones TO 'ljcs_app'@'%';
GRANT INSERT ON eventos_gamer.evento_compartidos TO 'ljcs_app'@'%';
FLUSH PRIVILEGES;
SQL
