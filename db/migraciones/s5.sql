-- Migracion de una base de la semana 3 al esquema de las semanas 4 y 5, sin perder datos.
-- Se puede ejecutar mas de una vez. Los scripts 01 a 03 solo corren cuando el volumen de MySQL esta vacio.
--
--   docker compose exec -T db sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD"' < db/migraciones/s5.sql
USE eventos_gamer;
SET NAMES utf8mb4;

-- Autor de cada evento (los eventos cargados por el equipo de LJCS no tienen autor)
SET @existe := (SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = 'eventos_gamer' AND table_name = 'eventos' AND column_name = 'autor_id');
SET @sql := IF(@existe = 0,
    'ALTER TABLE eventos ADD COLUMN autor_id BIGINT NULL, ADD CONSTRAINT fk_eventos_autor FOREIGN KEY (autor_id) REFERENCES usuarios (id) ON DELETE SET NULL',
    'SELECT 1');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

CREATE TABLE IF NOT EXISTS evento_medios (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    evento_id    BIGINT       NOT NULL,
    usuario_id   BIGINT       NOT NULL,
    tipo         VARCHAR(5)   NOT NULL,
    archivo      VARCHAR(64)  NOT NULL,
    content_type VARCHAR(40)  NOT NULL,
    tamano       BIGINT       NOT NULL,
    descripcion  VARCHAR(160) NOT NULL,
    subido_en    DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_medios_archivo UNIQUE (archivo),
    CONSTRAINT ck_medios_tipo CHECK (tipo IN ('FOTO', 'VIDEO')),
    CONSTRAINT fk_medios_evento FOREIGN KEY (evento_id) REFERENCES eventos (id) ON DELETE CASCADE,
    CONSTRAINT fk_medios_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS evento_comentarios (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    evento_id  BIGINT        NOT NULL,
    usuario_id BIGINT        NOT NULL,
    texto      VARCHAR(1000) NOT NULL,
    creado_en  DATETIME      NOT NULL,
    PRIMARY KEY (id),
    INDEX ix_comentarios_evento (evento_id, creado_en),
    CONSTRAINT fk_comentarios_evento FOREIGN KEY (evento_id) REFERENCES eventos (id) ON DELETE CASCADE,
    CONSTRAINT fk_comentarios_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS evento_valoraciones (
    id             BIGINT   NOT NULL AUTO_INCREMENT,
    evento_id      BIGINT   NOT NULL,
    usuario_id     BIGINT   NOT NULL,
    puntaje        INT      NOT NULL,
    actualizado_en DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_valoraciones_evento_usuario UNIQUE (evento_id, usuario_id),
    CONSTRAINT ck_valoraciones_puntaje CHECK (puntaje BETWEEN 1 AND 5),
    CONSTRAINT fk_valoraciones_evento FOREIGN KEY (evento_id) REFERENCES eventos (id) ON DELETE CASCADE,
    CONSTRAINT fk_valoraciones_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS evento_compartidos (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    evento_id  BIGINT       NOT NULL,
    usuario_id BIGINT       NOT NULL,
    mensaje    VARCHAR(280) NULL,
    creado_en  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_compartidos_evento_usuario UNIQUE (evento_id, usuario_id),
    CONSTRAINT fk_compartidos_evento FOREIGN KEY (evento_id) REFERENCES eventos (id) ON DELETE CASCADE,
    CONSTRAINT fk_compartidos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

-- Permisos minimos del usuario de la aplicacion sobre las tablas nuevas
GRANT INSERT ON eventos_gamer.eventos TO 'ljcs_app'@'%';
GRANT INSERT, DELETE ON eventos_gamer.evento_medios TO 'ljcs_app'@'%';
GRANT INSERT, DELETE ON eventos_gamer.evento_comentarios TO 'ljcs_app'@'%';
GRANT INSERT, UPDATE ON eventos_gamer.evento_valoraciones TO 'ljcs_app'@'%';
GRANT INSERT ON eventos_gamer.evento_compartidos TO 'ljcs_app'@'%';
FLUSH PRIVILEGES;
