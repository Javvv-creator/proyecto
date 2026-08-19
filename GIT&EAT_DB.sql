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
-- Tabla: producto
-- ---------------------------------------------------------------------
CREATE TABLE producto (
    id_producto     INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(80)    NOT NULL,
    precio          DECIMAL(10,2)  NOT NULL,
    stock           INT            NOT NULL DEFAULT 0,
    turno           ENUM('MANANA','TARDE_NOCHE','TODO_DIA') NOT NULL DEFAULT 'TODO_DIA',
    es_combo        TINYINT(1)     NOT NULL DEFAULT 0, -- 1 = Combo, 0 = Producto simple
    estado          TINYINT(1)     NOT NULL DEFAULT 1,
    id_categoria    INT            NOT NULL,
    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- Tabla: modificador
-- Catálogo de modificaciones (ej. "Sin Cebolla", "Extra Queso")
-- ---------------------------------------------------------------------
CREATE TABLE modificador (
    id_modificador    INT AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(50)   NOT NULL,
    precio_adicional  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    estado            TINYINT(1)    NOT NULL DEFAULT 1
);

-- ---------------------------------------------------------------------
-- Tablas para la configuración de Combos
-- ---------------------------------------------------------------------
CREATE TABLE combo_grupo (
    id_grupo           INT AUTO_INCREMENT PRIMARY KEY,
    id_producto_combo  INT NOT NULL, -- Producto tipo combo (ej. Combo Hamburguesa)
    nombre_grupo       VARCHAR(50) NOT NULL, -- ej. "Acompañamiento", "Bebida"
    CONSTRAINT fk_combogrupo_producto
        FOREIGN KEY (id_producto_combo) REFERENCES producto(id_producto)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE combo_opcion (
    id_opcion            INT AUTO_INCREMENT PRIMARY KEY,
    id_grupo             INT NOT NULL,
    id_producto_opcion   INT NOT NULL, -- Producto elegible (ej. Papas Fritas, Ensalada)
    costo_adicional      DECIMAL(10,2) NOT NULL DEFAULT 0.00, -- Cargo extra por upgrade
    CONSTRAINT fk_comboopcion_grupo
        FOREIGN KEY (id_grupo) REFERENCES combo_grupo(id_grupo)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_comboopcion_producto
        FOREIGN KEY (id_producto_opcion) REFERENCES producto(id_producto)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- Tabla: usuario
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario      INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(80)    NOT NULL,
    usuario         VARCHAR(40)    NOT NULL UNIQUE,
    contrasena      VARCHAR(255)   NOT NULL,
    rol             ENUM('ADMINISTRADOR','CAJERO') NOT NULL DEFAULT 'CAJERO',
    estado          TINYINT(1)     NOT NULL DEFAULT 1
);

-- ---------------------------------------------------------------------
-- Tabla: venta (Encabezado)
-- ---------------------------------------------------------------------
CREATE TABLE venta (
    id_venta        INT AUTO_INCREMENT PRIMARY KEY,
    fecha           DATE          NOT NULL,
    hora            TIME          NOT NULL,
    subtotal        DECIMAL(10,2) NOT NULL,
    iva             DECIMAL(10,2) NOT NULL,
    total           DECIMAL(10,2) NOT NULL,
    id_usuario      INT           NOT NULL,
    CONSTRAINT fk_venta_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- Tabla: pago_venta
-- Permite cobros mixtos, tarjetas, vales y cortesiás por error
-- ---------------------------------------------------------------------
CREATE TABLE pago_venta (
    id_pago           INT AUTO_INCREMENT PRIMARY KEY,
    id_venta          INT NOT NULL,
    metodo_pago       ENUM('EFECTIVO', 'TARJETA_DEBITO', 'TARJETA_CREDITO', 'VALE', 'CORTESIA_ERROR') NOT NULL,
    monto             DECIMAL(10,2) NOT NULL,
    monto_recibido    DECIMAL(10,2) NULL, -- Aplica para efectivo
    cambio            DECIMAL(10,2) NULL, -- Aplica para efectivo
    referencia        VARCHAR(50)   NULL, -- Autorización de tarjeta / folio de vale
    motivo_cortesia   VARCHAR(150)  NULL, -- Explicación si se marca como error de producción
    CONSTRAINT fk_pagoventa_venta
        FOREIGN KEY (id_venta) REFERENCES venta(id_venta)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Tabla: detalleventa
-- ---------------------------------------------------------------------
CREATE TABLE detalleventa (
    id_detalle      INT AUTO_INCREMENT PRIMARY KEY,
    cantidad        INT           NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal        DECIMAL(10,2) NOT NULL,
    id_venta        INT           NOT NULL,
    id_producto     INT           NOT NULL,
    CONSTRAINT fk_detalle_venta
        FOREIGN KEY (id_venta) REFERENCES venta(id_venta)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto
        FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- Tabla: detalle_modificacion
-- Registro de extras/ingredientes quitados por cada ítem vendido
-- ---------------------------------------------------------------------
CREATE TABLE detalle_modificacion (
    id_detalle_mod   INT AUTO_INCREMENT PRIMARY KEY,
    id_detalle       INT NOT NULL,
    id_modificador   INT NOT NULL,
    precio_aplicado  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_detmod_detalle
        FOREIGN KEY (id_detalle) REFERENCES detalleventa(id_detalle)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_detmod_modificador
        FOREIGN KEY (id_modificador) REFERENCES modificador(id_modificador)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- Tabla: detalle_combo_seleccion
-- Registra las opciones específicas elegidas en los combos de la venta
-- ---------------------------------------------------------------------
CREATE TABLE detalle_combo_seleccion (
    id_combo_sel       INT AUTO_INCREMENT PRIMARY KEY,
    id_detalle         INT NOT NULL,
    id_producto_opcion INT NOT NULL,
    costo_adicional    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_detcombo_detalle
        FOREIGN KEY (id_detalle) REFERENCES detalleventa(id_detalle)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_detcombo_producto
        FOREIGN KEY (id_producto_opcion) REFERENCES producto(id_producto)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- Índices para optimización
-- ---------------------------------------------------------------------
CREATE INDEX idx_producto_categoria ON producto(id_categoria);
CREATE INDEX idx_producto_turno ON producto(turno);
CREATE INDEX idx_pago_venta ON pago_venta(id_venta);
CREATE INDEX idx_pago_metodo ON pago_venta(metodo_pago);
CREATE INDEX idx_venta_usuario ON venta(id_usuario);
CREATE INDEX idx_venta_fecha ON venta(fecha);
CREATE INDEX idx_detalle_venta ON detalleventa(id_venta);
CREATE INDEX idx_detalle_producto ON detalleventa(id_producto);
CREATE INDEX idx_detmod_detalle ON detalle_modificacion(id_detalle);
CREATE INDEX idx_detcombo_detalle ON detalle_combo_seleccion(id_detalle);