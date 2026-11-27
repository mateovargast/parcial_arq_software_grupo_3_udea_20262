INSERT INTO almacen (codigo_sede, nombre, ciudad, direccion, telefono) VALUES
    ('MED-01', 'Almacén Central Medellín', 'Medellín', 'Calle 67 # 53-108', '6042198000'),
    ('BOG-01', 'Almacén Bogotá Norte',     'Bogotá',   'Av. Calle 100 # 15-20', '6017456000'),
    ('CAL-01', 'Almacén Cali Sur',         'Cali',     'Carrera 100 # 5-169',   '6023301000');

INSERT INTO producto (sku, nombre, descripcion, categoria, unidad_medida, precio_unitario) VALUES
    ('TEC-001', 'Teclado mecánico',   'Teclado mecánico switch rojo, layout ES', 'Tecnología', 'UNIDAD', 189900.00),
    ('TEC-002', 'Mouse inalámbrico',  'Mouse óptico inalámbrico 2.4 GHz',        'Tecnología', 'UNIDAD',  59900.00),
    ('TEC-003', 'Monitor 27"',        'Monitor IPS 27 pulgadas 2K',              'Tecnología', 'UNIDAD', 1249900.00),
    ('OFI-001', 'Resma papel carta',  'Resma 500 hojas 75 g',                    'Oficina',    'PAQUETE',  21900.00),
    ('OFI-002', 'Silla ergonómica',   'Silla de oficina con soporte lumbar',     'Oficina',    'UNIDAD',  689900.00),
    ('ASE-001', 'Alcohol antiséptico','Alcohol al 70% x 1 litro',                'Aseo',       'LITRO',    12500.00);

INSERT INTO inventario (almacen_id, producto_id, cantidad, stock_minimo)
SELECT a.id, p.id, v.cantidad, v.stock_minimo
FROM (VALUES
    ('MED-01', 'TEC-001', 40, 10),
    ('MED-01', 'TEC-002', 120, 20),
    ('MED-01', 'TEC-003', 8, 10),
    ('MED-01', 'OFI-001', 300, 50),
    ('MED-01', 'OFI-002', 15, 5),
    ('MED-01', 'ASE-001', 3, 25),
    ('BOG-01', 'TEC-001', 25, 10),
    ('BOG-01', 'OFI-001', 150, 50),
    ('CAL-01', 'TEC-002', 60, 20)
) AS v(codigo_sede, sku, cantidad, stock_minimo)
JOIN almacen  a ON a.codigo_sede = v.codigo_sede
JOIN producto p ON p.sku = v.sku;
