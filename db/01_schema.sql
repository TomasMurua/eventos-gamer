-- Creacion de la base de datos de LJCS (MySQL 8)
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS eventos_gamer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eventos_gamer;

DROP TABLE IF EXISTS evento_compartidos;
DROP TABLE IF EXISTS evento_valoraciones;
DROP TABLE IF EXISTS evento_comentarios;
DROP TABLE IF EXISTS evento_medios;
DROP TABLE IF EXISTS evento_fotos;
DROP TABLE IF EXISTS eventos;
DROP TABLE IF EXISTS banners;
DROP TABLE IF EXISTS usuarios;

CREATE TABLE usuarios (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    username       VARCHAR(40)  NOT NULL,
    email          VARCHAR(120) NOT NULL,
    nombre         VARCHAR(80)  NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    rol            VARCHAR(20)  NOT NULL,
    juegos_interes VARCHAR(255) NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_username UNIQUE (username),
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT ck_usuarios_rol CHECK (rol IN ('USER', 'ADMIN'))
);

CREATE TABLE eventos (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(120) NOT NULL,
    videojuego    VARCHAR(80)  NOT NULL,
    categoria     VARCHAR(40)  NOT NULL,
    modalidad     VARCHAR(12)  NOT NULL,
    pais          VARCHAR(40)  NOT NULL,
    ciudad        VARCHAR(60)  NULL,
    fecha_hora    DATETIME     NOT NULL,
    ubicacion     VARCHAR(200) NULL,
    enlace_acceso VARCHAR(200) NULL,
    organizador   VARCHAR(120) NOT NULL,
    resumen       VARCHAR(255) NOT NULL,
    descripcion   TEXT         NOT NULL,
    requisitos    TEXT         NOT NULL,
    inscripcion   TEXT         NOT NULL,
    cupos         INT          NULL,
    popularidad   INT          NOT NULL DEFAULT 0,
    destacado     BOOLEAN      NOT NULL DEFAULT FALSE,
    publicado_en  DATETIME     NOT NULL,
    autor_id      BIGINT       NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_eventos_modalidad CHECK (modalidad IN ('PRESENCIAL', 'ONLINE')),
    CONSTRAINT fk_eventos_autor FOREIGN KEY (autor_id) REFERENCES usuarios (id) ON DELETE SET NULL
);

CREATE INDEX ix_eventos_fecha ON eventos (fecha_hora);
CREATE INDEX ix_eventos_categoria ON eventos (categoria);

CREATE TABLE evento_fotos (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    evento_id   BIGINT       NOT NULL,
    url         VARCHAR(200) NOT NULL,
    descripcion VARCHAR(160) NOT NULL,
    orden       INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_fotos_evento FOREIGN KEY (evento_id) REFERENCES eventos (id) ON DELETE CASCADE
);

-- Fotos y videos que sube el autor del evento. El archivo vive en disco con un nombre aleatorio;
-- aqui solo queda ese nombre, el tipo verificado y el tamano.
CREATE TABLE evento_medios (
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

CREATE TABLE evento_comentarios (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    evento_id  BIGINT        NOT NULL,
    usuario_id BIGINT        NOT NULL,
    texto      VARCHAR(1000) NOT NULL,
    creado_en  DATETIME      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comentarios_evento FOREIGN KEY (evento_id) REFERENCES eventos (id) ON DELETE CASCADE,
    CONSTRAINT fk_comentarios_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE INDEX ix_comentarios_evento ON evento_comentarios (evento_id, creado_en);

-- Una valoracion por usuario y evento (se puede cambiar)
CREATE TABLE evento_valoraciones (
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

-- Eventos que los usuarios comparten en el muro del sitio
CREATE TABLE evento_compartidos (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    evento_id  BIGINT       NOT NULL,
    usuario_id BIGINT       NOT NULL,
    mensaje    VARCHAR(280) NULL,
    creado_en  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_compartidos_evento FOREIGN KEY (evento_id) REFERENCES eventos (id) ON DELETE CASCADE,
    CONSTRAINT fk_compartidos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE banners (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    marca   VARCHAR(60)  NOT NULL,
    mensaje VARCHAR(160) NOT NULL,
    rubro   VARCHAR(40)  NOT NULL,
    activo  BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
);
