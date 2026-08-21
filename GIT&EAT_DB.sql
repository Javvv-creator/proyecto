DROP DATABASE IF EXISTS GITEAT;
CREATE DATABASE GITEAT;
USE GITEAT;

-- ---------------------------------------------------------------------
-- Tabla: categoria
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
    id_categoria    INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(50)  NOT NULL,
    descripcion     VARCHAR(150),
    estado          TINYINT(1)   NOT NULL DEFAULT 1
);

-- ---------------------------------------------------------------------
-- Tabla: usuario (con ENUM de rol)
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario      INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(80)                    NOT NULL,
    pin_acceso      VARCHAR(10)                    NOT NULL UNIQUE,
    contrasena      VARCHAR(255)                   NULL,
    rol             ENUM('ADMINISTRADOR','CAJERO') NOT NULL DEFAULT 'CAJERO',
    estado          TINYINT(1)                     NOT NULL DEFAULT 1
);

-- ---------------------------------------------------------------------
-- Tabla: turno_menu
-- ---------------------------------------------------------------------
CREATE TABLE turno_menu (
    id_turno        INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(50)  NOT NULL,
    hora_inicio     TIME         NOT NULL,
    hora_fin        TIME         NOT NULL,
    estado          TINYINT(1)   NOT NULL DEFAULT 1
);

-- ---------------------------------------------------------------------
-- Tabla: ingrediente
-- ---------------------------------------------------------------------
CREATE TABLE ingrediente (
    id_ingrediente          INT AUTO_INCREMENT PRIMARY KEY,
    nombre                  VARCHAR(100)  NOT NULL,
    precio_extra_defecto    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    estado                  TINYINT(1)    NOT NULL DEFAULT 1
);

-- ---------------------------------------------------------------------
-- Tabla: producto
-- ---------------------------------------------------------------------
CREATE TABLE producto (
    id_producto     INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100)  NOT NULL,
    precio_base     DECIMAL(10,2) NOT NULL,
    es_combo        TINYINT(1)    NOT NULL DEFAULT 0,
    estado          TINYINT(1)    NOT NULL DEFAULT 1,
    id_categoria    INT           NULL,
    id_turno        INT           NULL,
    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    CONSTRAINT fk_producto_turno
        FOREIGN KEY (id_turno) REFERENCES turno_menu(id_turno)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- ---------------------------------------------------------------------
-- Tabla: receta_producto
-- ---------------------------------------------------------------------
CREATE TABLE receta_producto (
    id_producto         INT           NOT NULL,
    id_ingrediente      INT           NOT NULL,
    cantidad_base       DECIMAL(8,2)  NOT NULL DEFAULT 1.00,
    es_removible        TINYINT(1)    NOT NULL DEFAULT 1,
    es_extra_permitido  TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (id_producto, id_ingrediente),
    CONSTRAINT fk_receta_producto
        FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_receta_ingrediente
        FOREIGN KEY (id_ingrediente) REFERENCES ingrediente(id_ingrediente)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Tabla: combo_componente
-- ---------------------------------------------------------------------
CREATE TABLE combo_componente (
    id_componente        INT AUTO_INCREMENT PRIMARY KEY,
    grupo                VARCHAR(50)  NOT NULL,
    id_producto_combo    INT          NOT NULL,
    CONSTRAINT fk_combocomp_producto
        FOREIGN KEY (id_producto_combo) REFERENCES producto(id_producto)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Tabla: combo_opcion_intercambio
-- ---------------------------------------------------------------------
CREATE TABLE combo_opcion_intercambio (
    id_opcion           INT AUTO_INCREMENT PRIMARY KEY,
    costo_extra         DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    id_componente       INT           NOT NULL,
    id_producto_opcion  INT           NOT NULL,
    CONSTRAINT fk_comboopc_componente
        FOREIGN KEY (id_componente) REFERENCES combo_componente(id_componente)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_comboopc_producto
        FOREIGN KEY (id_producto_opcion) REFERENCES producto(id_producto)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Tabla: orden (sin el campo subtotal)
-- ---------------------------------------------------------------------
CREATE TABLE orden (
    id_orden        INT AUTO_INCREMENT PRIMARY KEY,
    fecha           DATE          NOT NULL,
    hora            TIME          NOT NULL,
    estado          VARCHAR(20)   NOT NULL DEFAULT 'En cocina',
    total           DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    id_usuario      INT           NOT NULL,
    CONSTRAINT fk_orden_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- Tabla: detalle_orden
-- ---------------------------------------------------------------------
CREATE TABLE detalle_orden (
    id_detalle      INT AUTO_INCREMENT PRIMARY KEY,
    cantidad        INT           NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal        DECIMAL(10,2) NOT NULL,
    es_agrandado    TINYINT(1)    NOT NULL DEFAULT 0,
    id_orden        INT           NOT NULL,
    id_producto     INT           NOT NULL,
    CONSTRAINT fk_detalle_orden
        FOREIGN KEY (id_orden) REFERENCES orden(id_orden)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto
        FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- Tabla: modificacion_orden
-- ---------------------------------------------------------------------
CREATE TABLE modificacion_orden (
    id_modificacion INT AUTO_INCREMENT PRIMARY KEY,
    accion          ENUM('AGREGAR', 'QUITAR') NOT NULL,
    costo_aplicado  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    id_detalle      INT           NOT NULL,
    id_ingrediente  INT           NOT NULL,
    CONSTRAINT fk_modificacion_detalle
        FOREIGN KEY (id_detalle) REFERENCES detalle_orden(id_detalle)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_modificacion_ingrediente
        FOREIGN KEY (id_ingrediente) REFERENCES ingrediente(id_ingrediente)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- Tabla: pago_orden (con ENUM de metodo_pago)
-- ---------------------------------------------------------------------
CREATE TABLE pago_orden (
    id_pago         INT AUTO_INCREMENT PRIMARY KEY,
    monto           DECIMAL(10,2) NOT NULL,
    metodo_pago     ENUM('Efectivo', 'Tarjeta de Crédito', 'Tarjeta de Débito', 'Vale', 'Defecto de Fábrica') NOT NULL,
    id_orden        INT           NOT NULL,
    CONSTRAINT fk_pago_orden
        FOREIGN KEY (id_orden) REFERENCES orden(id_orden)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Inserción de datos iniciales mínimos
-- ---------------------------------------------------------------------
INSERT INTO turno_menu (nombre, hora_inicio, hora_fin) VALUES 
('Mañana', '06:00:00', '11:59:59'),
('Tarde', '12:00:00', '05:59:59');

-- ---------------------------------------------------------------------
-- Índices optimizados
-- ---------------------------------------------------------------------
CREATE INDEX idx_turno_horario ON turno_menu(hora_inicio, hora_fin);
CREATE INDEX idx_producto_categoria ON producto(id_categoria);
CREATE INDEX idx_producto_turno ON producto(id_turno);
CREATE INDEX idx_producto_combo ON producto(es_combo);
CREATE INDEX idx_receta_ingrediente ON receta_producto(id_ingrediente);
CREATE INDEX idx_combocomp_producto ON combo_componente(id_producto_combo);
CREATE INDEX idx_comboopc_componente ON combo_opcion_intercambio(id_componente);
CREATE INDEX idx_comboopc_producto ON combo_opcion_intercambio(id_producto_opcion);
CREATE INDEX idx_orden_usuario ON orden(id_usuario);
CREATE INDEX idx_orden_fecha ON orden(fecha);
CREATE INDEX idx_orden_estado ON orden(estado);
CREATE INDEX idx_detalle_orden ON detalle_orden(id_orden);
CREATE INDEX idx_detalle_producto ON detalle_orden(id_producto);
CREATE INDEX idx_modificacion_detalle ON modificacion_orden(id_detalle);
CREATE INDEX idx_modificacion_ingrediente ON modificacion_orden(id_ingrediente);
CREATE INDEX idx_pago_orden ON pago_orden(id_orden);
CREATE INDEX idx_pago_metodo ON pago_orden(metodo_pago);