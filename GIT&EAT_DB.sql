DROP DATABASE IF EXISTS GITEAT;
CREATE DATABASE GITEAT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
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
    nombre          VARCHAR(50)                    NOT NULL,
    apellido        VARCHAR(50)                    NOT NULL,
    codigo_empleado VARCHAR(10)                    NOT NULL UNIQUE,
    contrasena      VARCHAR(255)                   NULL,
    rol             ENUM('ADMINISTRADOR','CAJERO') NOT NULL DEFAULT 'CAJERO',
    estado          TINYINT(1)                     NOT NULL DEFAULT 1,
    turno           VARCHAR(50)                    NOT NULL
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
--   imagen     -> nombre del archivo en /gui/images/
--   seccion    -> subtítulo dentro de la categoría (Desayunos, Pollo, Sodas...)
--   orden_menu -> orden en que se muestran en la pantalla
-- ---------------------------------------------------------------------
CREATE TABLE producto (
    id_producto     INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100)  NOT NULL,
    precio_base     DECIMAL(10,2) NOT NULL,
    es_combo        TINYINT(1)    NOT NULL DEFAULT 0,
    estado          TINYINT(1)    NOT NULL DEFAULT 1,
    imagen          VARCHAR(150)  NULL,
    seccion         VARCHAR(50)   NULL,
    orden_menu      INT           NOT NULL DEFAULT 0,
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
    metodo_pago     ENUM('Efectivo', 'Tarjeta de Crédito', 'Tarjeta de Débito', 'Tarjeta', 'Mixto', 'Vale', 'Defecto de Fábrica') NOT NULL,
    id_orden        INT           NOT NULL,
    CONSTRAINT fk_pago_orden
        FOREIGN KEY (id_orden) REFERENCES orden(id_orden)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
 
-- =====================================================================
-- DATOS INICIALES
-- =====================================================================
 
-- ---------------------------------------------------------------------
-- Turnos del menú
-- ---------------------------------------------------------------------
-- Mañana: 04:00 a 11:00 (desayunos) | Tarde: 11:01 a 02:00 (hamburguesas)
-- El turno Tarde cruza la medianoche, el programa ya lo sabe manejar.
INSERT INTO turno_menu (id_turno, nombre, hora_inicio, hora_fin) VALUES
(1, 'Mañana', '04:00:00', '11:00:00'),
(2, 'Tarde',  '11:01:00', '02:00:00');
 
-- ---------------------------------------------------------------------
-- Categorías (las pestañas de arriba)
-- ---------------------------------------------------------------------
INSERT INTO categoria (id_categoria, nombre, descripcion) VALUES
(1, 'Hamburguesas', 'Desayunos, hamburguesas, pollo y gourmet'),
(2, 'Bebidas',      'Bebidas frías, calientes, sodas y naturales'),
(3, 'Postres',      'Postres y helados'),
(4, 'Combos',       'Combos del menú');
 
-- ---------------------------------------------------------------------
-- Productos
--    id_turno: 1 = Mañana (desayunos), 2 = Tarde, NULL = todo el día
-- ---------------------------------------------------------------------
INSERT INTO producto (nombre, precio_base, es_combo, id_categoria, id_turno, imagen, seccion, orden_menu) VALUES
 
-- Hamburguesas / Desayunos
('McMuffin Cheddar McMelt', 28.00, 0, 1, 1, 'mcmuffin_cheddar_mcmelt.png', 'Desayunos', 1),
('McMuffin Tocino Doble Huevo', 30.00, 0, 1, 1, 'mcmuffin_tocino_doble_huevo.png', 'Desayunos', 2),
('McMuffin Salchicha y doble huevo', 30.00, 0, 1, 1, 'mcmuffin_salchicha_y_doble_huevo.png', 'Desayunos', 3),
('Egg McMuffin Doble Huevo', 32.00, 0, 1, 1, 'egg_mcmuffin_doble_huevo.png', 'Desayunos', 4),
('McMuffin de Salchicha y Huevo', 28.00, 0, 1, 1, 'mcmuffin_de_salchicha_y_huevo.png', 'Desayunos', 5),
('McMuffin de Salchicha', 26.00, 0, 1, 1, 'mcmuffin_de_salchicha.png', 'Desayunos', 6),
('McMuffin Chapín Con Salchicha', 30.00, 0, 1, 1, 'mcmuffin_chapin_con_salchicha.png', 'Desayunos', 7),
('Egg McMuffin', 26.00, 0, 1, 1, 'egg_mcmuffin.png', 'Desayunos', 8),
('McMuffin Super Chapín Con Salchicha', 32.00, 0, 1, 1, 'mcmuffin_super_chapin_con_salchicha.png', 'Desayunos', 9),
('Egg McMuffin Doble', 30.00, 0, 1, 1, 'egg_mcmuffin_doble.png', 'Desayunos', 10),
('McMuffin de Tocino y Huevo', 28.00, 0, 1, 1, 'mcmuffin_de_tocino_y_huevo.png', 'Desayunos', 11),
('McMuffin Super Chapín Con Jamón', 32.00, 0, 1, 1, 'mcmuffin_super_chapin_con_jamon.png', 'Desayunos', 12),
('McMuffin de Salchicha Doble y Huevo', 32.00, 0, 1, 1, 'mcmuffin_de_salchicha_doble_y_huevo.png', 'Desayunos', 13),
('McMuffin Tocino Doble y Huevo', 32.00, 0, 1, 1, 'mcmuffin_tocino_doble_y_huevo.png', 'Desayunos', 14),
('McMuffin Huevo y Frijol', 25.00, 0, 1, 1, 'mcmuffin_huevo_y_frijol.png', 'Desayunos', 15),
('McMuffin Huevo y Queso', 26.00, 0, 1, 1, 'mcmuffin_huevo_y_queso.png', 'Desayunos', 16),
('McMuffin Chapín Con Jamón', 28.00, 0, 1, 1, 'mcmuffin_chapin_con_jamon.png', 'Desayunos', 17),
 
-- Hamburguesas / Hamburguesas
('Bacon Cheddar McMelt', 38.00, 0, 1, 2, 'bacon_cheddar_mcmelt.png', 'Hamburguesas', 18),
('McCrispy Bacon Cheddar', 40.00, 0, 1, 2, 'mccrispy_bacon_cheddar.png', 'Hamburguesas', 19),
('Git Mac Doble', 41.00, 0, 1, 2, 'git_mac_doble.png', 'Hamburguesas', 20),
('Cuarto de Libra con Queso', 39.00, 0, 1, 2, 'cuarto_de_libra_con_queso.png', 'Hamburguesas', 21),
('Cuarto de Libra Doble con Queso', 44.00, 0, 1, 2, 'cuarto_de_libra_doble_con_queso.png', 'Hamburguesas', 22),
('Cuarto de Libra Deluxe con Queso', 42.00, 0, 1, 2, 'cuarto_de_libra_deluxe_con_queso.png', 'Hamburguesas', 23),
('Cuarto de Libra Deluxe Doble con Queso', 46.00, 0, 1, 2, 'cuarto_de_libra_deluxe_doble_con_queso.png', 'Hamburguesas', 24),
('Cuarto de Libra Bacon con Queso', 42.00, 0, 1, 2, 'cuarto_de_libra_bacon_con_queso.png', 'Hamburguesas', 25),
('Cuarto de Libra Bacon Doble con Queso', 46.00, 0, 1, 2, 'cuarto_de_libra_bacon_doble_con_queso.png', 'Hamburguesas', 26),
('Big Tasty', 48.00, 0, 1, 2, 'big_tasty.png', 'Hamburguesas', 27),
('Big Tasty Doble', 55.00, 0, 1, 2, 'big_tasty_doble.png', 'Hamburguesas', 28),
('Big Tasty Bacon', 50.00, 0, 1, 2, 'big_tasty_bacon.png', 'Hamburguesas', 29),
('Big Tasty Bacon Doble', 58.00, 0, 1, 2, 'big_tasty_bacon_doble.png', 'Hamburguesas', 30),
('Triple Bacon', 60.00, 0, 1, 2, 'triple_bacon.png', 'Hamburguesas', 31),
('Quesoburguesa', 32.00, 0, 1, 2, 'quesoburguesa.png', 'Hamburguesas', 32),
('Quesoburguesa Doble', 44.00, 0, 1, 2, 'quesoburguesa_doble.png', 'Hamburguesas', 33),
('Quesoburguesa Triple', 46.00, 0, 1, 2, 'quesoburguesa_triple.png', 'Hamburguesas', 34),
('Hamburguesa', 20.00, 0, 1, 2, 'hamburguesa.png', 'Hamburguesas', 35),
('Hamburguesa Jr.', 18.00, 0, 1, 2, 'hamburguesa_jr.png', 'Hamburguesas', 36),
('GitNífica de Res', 42.00, 0, 1, 2, 'gitnifica_de_res.png', 'Hamburguesas', 37),
('GitNífica de Res Doble', 48.00, 0, 1, 2, 'gitnifica_de_res_doble.png', 'Hamburguesas', 38),
 
-- Hamburguesas / Pollo
('McCrispy Chicken Bacon Ranch', 42.00, 0, 1, 2, 'mccrispy_chicken_bacon_ranch.png', 'Pollo', 39),
('McCrispy Chicken Deluxe', 40.00, 0, 1, 2, 'mccrispy_chicken_deluxe.png', 'Pollo', 40),
('Big Tasty de Pollo', 48.00, 0, 1, 2, 'big_tasty_de_pollo.png', 'Pollo', 41),
('Sándwich McPollo Doble', 49.00, 0, 1, 2, 'sandwich_mcpollo_doble.png', 'Pollo', 42),
 
-- Hamburguesas / Creaciones Gourmet
('Smoke Tocino Gourmet de Res', 50.00, 0, 1, 2, 'smoke_tocino_gourmet_de_res.png', 'Creaciones Gourmet', 43),
('Smoke Tocino Gourmet doble', 58.00, 0, 1, 2, 'smoke_tocino_gourmet_doble.png', 'Creaciones Gourmet', 44),
('Clásica Gourmet Res', 48.00, 0, 1, 2, 'clasica_gourmet_res.png', 'Creaciones Gourmet', 45),
('Clásica Gourmet Res doble', 55.00, 0, 1, 2, 'clasica_gourmet_res_doble.png', 'Creaciones Gourmet', 46),
('Pico Guacamol Gourmet Res', 50.00, 0, 1, 2, 'pico_guacamol_gourmet_res.png', 'Creaciones Gourmet', 47),
('Pico Guacamol Gourmet doble', 58.00, 0, 1, 2, 'pico_guacamol_gourmet_doble.png', 'Creaciones Gourmet', 48),
 
-- Bebidas / Bebidas Frías
('Horchata', 15.00, 0, 2, NULL, 'horchata.png', 'Bebidas Frías', 49),
('Iced Coffee Horchata', 22.00, 0, 2, NULL, 'iced_coffee_horchata.png', 'Bebidas Frías', 50),
('GITFizz Pink', 20.00, 0, 2, NULL, 'mc_fizzpink.png', 'Bebidas Frías', 51),
('GITFizz Manzana Verde', 20.00, 0, 2, NULL, 'mc_fizz_manzana_verde.png', 'Bebidas Frías', 52),
('GITFizz Blue', 20.00, 0, 2, NULL, 'mc_fizz_blue.png', 'Bebidas Frías', 53),
('Frappé Oreo', 32.00, 0, 2, NULL, 'frappe_oreo.png', 'Bebidas Frías', 54),
('Frappé Original', 28.00, 0, 2, NULL, 'frappe_original.png', 'Bebidas Frías', 55),
('Frappé Vainilla', 30.00, 0, 2, NULL, 'frappe_vainilla.png', 'Bebidas Frías', 56),
('Frappé Chocolate', 30.00, 0, 2, NULL, 'frappe_chocolate.png', 'Bebidas Frías', 57),
('Frappé Caramelo', 30.00, 0, 2, NULL, 'frappe_caramelo.png', 'Bebidas Frías', 58),
('Frappé Vainilla Light', 28.00, 0, 2, NULL, 'frappe_vainilla_light.png', 'Bebidas Frías', 59),
('Iced Coffee Original', 22.00, 0, 2, NULL, 'iced_coffee_original.png', 'Bebidas Frías', 60),
('Iced Coffee Vainilla', 24.00, 0, 2, NULL, 'iced_coffee_vainilla.png', 'Bebidas Frías', 61),
('Iced Coffee Vainilla Light', 24.00, 0, 2, NULL, 'iced_coffee_vainilla_light.png', 'Bebidas Frías', 62),
('Iced Coffee Chocolate', 24.00, 0, 2, NULL, 'iced_coffee_chocolate.png', 'Bebidas Frías', 63),
('Iced Coffee Caramelo', 24.00, 0, 2, NULL, 'iced_coffee_caramelo.png', 'Bebidas Frías', 64),
('Té Chai Frappé Té Verde', 28.00, 0, 2, NULL, 'te_chai_frappe_te_verde.png', 'Bebidas Frías', 65),
('Té Chai Frappé Original', 28.00, 0, 2, NULL, 'te_chai_frappe_original.png', 'Bebidas Frías', 66),
('Té Chai Frappé Vainilla', 30.00, 0, 2, NULL, 'te_chai_frappe_vainilla.png', 'Bebidas Frías', 67),
('Té Chai Frappé Vainilla Light', 28.00, 0, 2, NULL, 'te_chai_frappe_vainilla_light.png', 'Bebidas Frías', 68),
('Smoothie de Berries', 35.00, 0, 2, NULL, 'smoothie_de_berries.png', 'Bebidas Frías', 69),
('Smoothie de Mango', 35.00, 0, 2, NULL, 'smoothie_de_mango.png', 'Bebidas Frías', 70),
('GITFizz A.M.', 22.00, 0, 2, NULL, 'mc_fizz_am.png', 'Bebidas Frías', 71),
 
-- Bebidas / Bebidas Calientes
('Té Chai Original', 18.00, 0, 2, NULL, 'te_chai_original.png', 'Bebidas Calientes', 72),
('Té Chai Té Verde', 18.00, 0, 2, NULL, 'te_chai_te_verde.png', 'Bebidas Calientes', 73),
('Té Chai Vainilla', 20.00, 0, 2, NULL, 'te_chai_vainilla.png', 'Bebidas Calientes', 74),
('Té Chai Vainilla Light', 20.00, 0, 2, NULL, 'te_chai_vainilla_light.png', 'Bebidas Calientes', 75),
('Cappuccino', 22.00, 0, 2, NULL, 'cappuccino.png', 'Bebidas Calientes', 76),
('Latte', 22.00, 0, 2, NULL, 'latte.png', 'Bebidas Calientes', 77),
('Café Guatemalteco', 18.00, 0, 2, NULL, 'cafe_guatemalteco.png', 'Bebidas Calientes', 78),
('Té Guatemalteco Manzanilla Relax', 15.00, 0, 2, NULL, 'te_guatemalteco_manzanilla_relax.png', 'Bebidas Calientes', 79),
('Té Guatemalteco Melocotón Mix', 15.00, 0, 2, NULL, 'te_guatemalteco_melocoton_mix.png', 'Bebidas Calientes', 80),
('Té Guatemalteco Bora Bora', 15.00, 0, 2, NULL, 'te_guatemalteco_borabora.png', 'Bebidas Calientes', 81),
('Té Guatemalteco Menta Fusión', 15.00, 0, 2, NULL, 'te_guatemalteco_menta_fusion.png', 'Bebidas Calientes', 82),
('Chocolate caliente', 20.00, 0, 2, NULL, 'chocolate_caliente.png', 'Bebidas Calientes', 83),
 
-- Bebidas / Café en Bolsa
('Blend Molido', 45.00, 0, 2, NULL, 'blend_molido.png', 'Café en Bolsa', 84),
('Blend Grano', 45.00, 0, 2, NULL, 'blend_grano.png', 'Café en Bolsa', 85),
 
-- Bebidas / Sodas
('Sprite', 10.00, 0, 2, NULL, 'sprite.png', 'Sodas', 86),
('Coca-Cola', 10.00, 0, 2, NULL, 'coca_cola.png', 'Sodas', 87),
('Coca Cola Zero', 10.00, 0, 2, NULL, 'coca_cola_zero.png', 'Sodas', 88),
('Fanta', 10.00, 0, 2, NULL, 'fanta.png', 'Sodas', 89),
 
-- Bebidas / Naturales
('Jugo de Naranja', 12.00, 0, 2, NULL, 'jugo_de_naranja.png', 'Naturales', 90),
('Té Lipton', 12.00, 0, 2, NULL, 'te_lipton.png', 'Naturales', 91),
('Rosa de Jamaica', 12.00, 0, 2, NULL, 'rosa_de_jamaica.png', 'Naturales', 92),
('Agua Pura', 8.00, 0, 2, NULL, 'agua_pura.png', 'Naturales', 93),
('Jugo de Manzana', 12.00, 0, 2, NULL, 'jugo_de_manzana.png', 'Naturales', 94),
 
-- Bebidas / Calientes
('Café', 15.00, 0, 2, NULL, 'cafe.png', 'Calientes', 95),
('Café Con Leche', 18.00, 0, 2, NULL, 'cafe_con_leche.png', 'Calientes', 96),
('Chocolate', 18.00, 0, 2, NULL, 'chocolate.png', 'Calientes', 97),
('Té Caliente', 15.00, 0, 2, NULL, 'te_caliente.png', 'Calientes', 98);

-- Postres / Helados McFlurry
('McFlurry Oreo', 20.00, 0, 3, NULL, 'McFlurryOreo.png', 'Helados McFlurry', 99),
('McFlurry Oreo Caramelo', 22.00, 0, 3, NULL, 'McFlurryOreoCaramelo.png', 'Helados McFlurry', 100),
('McFlurry Oreo Chocolate', 22.00, 0, 3, NULL, 'McFlurryOreoChocolate.png', 'Helados McFlurry', 101),
('McFlurry Oreo Fresa', 22.00, 0, 3, NULL, 'McFlurryOreoFresa.png', 'Helados McFlurry', 102),
('McFlurry M&M''s', 20.00, 0, 3, NULL, 'McFlurryM&Ms.png', 'Helados McFlurry', 103),
('McFlurry M&M''s Caramelo', 22.00, 0, 3, NULL, 'McFlurryM&MsCaramelo.png', 'Helados McFlurry', 104),
('McFlurry M&M''s Chocolate', 22.00, 0, 3, NULL, 'McFlurryM&MsChocolate.png', 'Helados McFlurry', 105),
('McFlurry M&M''s Fresa', 22.00, 0, 3, NULL, 'McFlurryM&MsFresa.png', 'Helados McFlurry', 106),

-- Postres / Sundaes
('Sundae de Caramelo', 15.00, 0, 3, NULL, 'SundaedeCaramelo.png', 'Sundaes', 107),
('Sundae de Chocolate', 15.00, 0, 3, NULL, 'SundaedeChocolate.png', 'Sundaes', 108),
('Sundae de Fresa', 15.00, 0, 3, NULL, 'SundaedeFresa.png', 'Sundaes', 109),

-- Postres / Pasteles
('Pastel de Manzana', 12.00, 0, 3, NULL, 'PasteldeManzana.png', 'Pasteles', 110),
('Pastel de Queso', 12.00, 0, 3, NULL, 'PasteldeQueso.png', 'Pasteles', 111);

-- Combos / Cajas y Cajitas Felices 
('Bucket Para Todos', 115.00, 1, 4, 2, 'BucketParaTodos.jpg', 'Familiares', 112),
('Bucket Pollo McCrispy', 95.00, 1, 4, 2, 'BucketPolloMcCrispy.jpg', 'Familiares', 113),
('Bucket Pollo McCrispy Para Tres', 105.00, 1, 4, 2, 'BucketPolloMcCrispyParaTres.jpg', 'Familiares', 114),
('Bucket Pollo McCrispy Snack', 85.00, 1, 4, 2, 'BucketPolloMcCrispySnack.jpg', 'Familiares', 115),
('Caja de McNuggets', 85.00, 1, 4, NULL, 'CajadeMcNuggets.jpg', 'Cajas', 116),
('Caja Grande', 120.00, 1, 4, NULL, 'CajaGrande.jpg', 'Cajas', 117),
('Caja Grande Con Postre', 135.00, 1, 4, NULL, 'CajaGrandeConPostre.jpg', 'Cajas', 118),
('Caja Grande Deluxe', 140.00, 1, 4, NULL, 'CajaGrandeDeluxe.jpg', 'Cajas', 119),
('Caja Grande Desayuno', 110.00, 1, 4, 1, 'CajaGrandeDesayuno.jpg', 'Cajas', 120),
('Caja Grande Snack', 100.00, 1, 4, NULL, 'CajaGrandeSnack.jpg', 'Cajas', 121),
('Cajita Feliz de Derretido', 35.00, 1, 4, NULL, 'CajitaFelizdeDerretido.png', 'Cajita Feliz', 122),
('Cajita Feliz de Derretido Almuerzo Cena', 35.00, 1, 4, 2, 'CajitaFelizdeDerretidoAlmuerzoCena.png', 'Cajita Feliz', 123),
('Cajita Feliz de Hamburguesa', 38.00, 1, 4, 2, 'CajitaFelizdeHamburguesa.jpg', 'Cajita Feliz', 124),
('Cajita Feliz de Hamburguesa Jr.', 36.00, 1, 4, 2, 'CajitaFelizdeHamburguesaJr.jpg', 'Cajita Feliz', 125),
('Cajita Feliz de Hotcakes', 35.00, 1, 4, 1, 'CajitaFelizdeHotcakes.jpg', 'Cajita Feliz', 126),
('Cajita Feliz de McMuffin De Frijol', 35.00, 1, 4, 1, 'CajitaFelizdeMcMuffindeFrijol.jpg', 'Cajita Feliz', 127),
('Cajita Feliz de McMuffin de Huevo y Queso', 35.00, 1, 4, 1, 'CajitaFelizdeMcMuffindeHuevoyQueso.jpg', 'Cajita Feliz', 128),
('Cajita Feliz de McMuffin de Salchicha', 35.00, 1, 4, 1, 'CajitaFelizdeMcMuffindeSalchicha.png', 'Cajita Feliz', 129),
('Cajita Feliz de McNuggets', 38.00, 1, 4, NULL, 'CajitaFelizdeMcNuggets.jpg', 'Cajita Feliz', 130),
('Cajita Feliz de Pollo McCrispy', 40.00, 1, 4, 2, 'CajitaFelizdePolloMcCrispy.jpg', 'Cajita Feliz', 131),
('Cajita Feliz de Quesoburguesa', 38.00, 1, 4, 2, 'CajitaFelizdeQuesoburguesa.jpg', 'Cajita Feliz', 132);

-- ---------------------------------------------------------------------
-- AGREGADOS FALTANTES: Usuarios Iniciales
-- ---------------------------------------------------------------------
INSERT INTO usuario (id_usuario, nombre, apellido, codigo_empleado, contrasena, rol, estado, turno) VALUES
(1, 'Admin', 'General', 'ADM001', 'admin123', 'ADMINISTRADOR', 1, 'Mañana'),
(2, 'Carlos', 'Gómez', 'CAJ001', 'cajero123', 'CAJERO', 1, 'Mañana'),
(3, 'Ana', 'Martínez', 'CAJ002', 'cajero123', 'CAJERO', 1, 'Tarde');

-- ---------------------------------------------------------------------
-- AGREGADOS FALTANTES: Ingredientes
-- ---------------------------------------------------------------------
INSERT INTO ingrediente (id_ingrediente, nombre, precio_extra_defecto) VALUES
(1, 'Pepinillos', 3.00),
(2, 'Queso Cheddar', 5.00),
(3, 'Cebolla', 2.00),
(4, 'Salsa Git Mac', 4.00),
(5, 'Tocino', 6.00),
(6, 'Torta de Res', 12.00),
(7, 'Papas Fritas Medianas', 10.00),
(8, 'Lechuga', 2.50),
(9, 'Tomate', 3.00),
(10, 'Mayonesa', 2.00),
(11, 'Huevo', 4.00),
(12, 'Salchicha', 5.00);


-- ---------------------------------------------------------------------
-- AGREGADOS FALTANTES: Componentes y Opciones de Intercambio para Combos
-- ---------------------------------------------------------------------
-- Componentes para 'Combo Git Mac Doble' (id_producto: 102)
INSERT INTO combo_componente (id_componente, grupo, id_producto_combo) VALUES
(1, 'Principal', 102),
(2, 'Acompañamiento', 102),
(3, 'Bebida', 102);

-- Opciones intercambiables para el Combo Git Mac Doble
INSERT INTO combo_opcion_intercambio (id_opcion, costo_extra, id_componente, id_producto_opcion) VALUES
(1, 0.00, 1, 20), -- Git Mac Doble como principal
(2, 0.00, 3, 87), -- Coca-Cola
(3, 0.00, 3, 86), -- Sprite
(4, 2.00, 3, 88); -- Coca Cola Zero con extra

-- ---------------------------------------------------------------------
-- AGREGADOS FALTANTES: Receta Base de Productos
-- ---------------------------------------------------------------------
INSERT INTO receta_producto (id_producto, id_ingrediente, cantidad_base, es_removible, es_extra_permitido) VALUES
(20, 1, 2.00, 1, 1), -- Git Mac Doble incluye Pepinillos
(20, 2, 2.00, 1, 1), -- Git Mac Doble incluye Queso Cheddar
(20, 3, 1.00, 1, 1), -- Git Mac Doble incluye Cebolla
(20, 4, 1.00, 1, 1), -- Git Mac Doble incluye Salsa Git Mac
(20, 6, 2.00, 0, 1); -- Git Mac Doble incluye Torta de Res

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
CREATE INDEX idx_producto_orden_menu ON producto(id_categoria, orden_menu);