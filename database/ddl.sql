DROP TABLE IF EXISTS inventario;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS almacen;

CREATE TABLE almacen (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo_sede  VARCHAR(20)  NOT NULL,
    nombre       VARCHAR(120) NOT NULL,
    ciudad       VARCHAR(80)  NOT NULL,
    direccion    VARCHAR(200) NOT NULL,
    telefono     VARCHAR(30),
    activo       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_almacen_codigo_sede UNIQUE (codigo_sede)
);

CREATE TABLE producto (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sku              VARCHAR(40)    NOT NULL,
    nombre           VARCHAR(150)   NOT NULL,
    descripcion      VARCHAR(500),
    categoria        VARCHAR(80)    NOT NULL,
    unidad_medida    VARCHAR(20)    NOT NULL,
    precio_unitario  NUMERIC(12, 2) NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uk_producto_sku UNIQUE (sku),
    CONSTRAINT ck_producto_precio CHECK (precio_unitario >= 0),
    CONSTRAINT ck_producto_unidad_medida CHECK (unidad_medida IN ('UNIDAD', 'PAQUETE', 'CAJA', 'LITRO', 'KILOGRAMO', 'METRO'))
);

CREATE TABLE inventario (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    almacen_id     BIGINT      NOT NULL,
    producto_id    BIGINT      NOT NULL,
    cantidad       INTEGER     NOT NULL,
    stock_minimo   INTEGER     NOT NULL DEFAULT 0,
    fecha_ingreso  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_inventario_almacen  FOREIGN KEY (almacen_id)  REFERENCES almacen (id),
    CONSTRAINT fk_inventario_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT uk_inventario_almacen_producto UNIQUE (almacen_id, producto_id),
    CONSTRAINT ck_inventario_cantidad CHECK (cantidad >= 0),
    CONSTRAINT ck_inventario_stock_minimo CHECK (stock_minimo >= 0)
);

CREATE INDEX idx_inventario_producto ON inventario (producto_id);
CREATE INDEX idx_producto_categoria  ON producto (categoria);
