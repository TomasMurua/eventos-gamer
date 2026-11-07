-- Creacion de la base de datos de LJCS (MySQL 8)
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS eventos_gamer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eventos_gamer;

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
    PRIMARY KEY (id),
    CONSTRAINT ck_eventos_modalidad CHECK (modalidad IN ('PRESENCIAL', 'ONLINE'))
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

CREATE TABLE banners (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    marca   VARCHAR(60)  NOT NULL,
    mensaje VARCHAR(160) NOT NULL,
    rubro   VARCHAR(40)  NOT NULL,
    activo  BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
);
