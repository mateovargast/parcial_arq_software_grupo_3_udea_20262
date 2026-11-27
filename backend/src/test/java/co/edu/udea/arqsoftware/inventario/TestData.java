package co.edu.udea.arqsoftware.inventario;

import co.edu.udea.arqsoftware.inventario.domain.Almacen;
import co.edu.udea.arqsoftware.inventario.domain.Inventario;
import co.edu.udea.arqsoftware.inventario.domain.Producto;
import co.edu.udea.arqsoftware.inventario.domain.UnidadMedida;
import co.edu.udea.arqsoftware.inventario.web.dto.AlmacenResponse;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemResponse;

import java.math.BigDecimal;
import java.time.Instant;

public final class TestData {

    public static final Instant FECHA = Instant.parse("2026-11-27T10:00:00Z");

    private TestData() {
    }

    public static Almacen almacen(Long id, String codigoSede) {
        Almacen almacen = new Almacen();
        almacen.setId(id);
        almacen.setCodigoSede(codigoSede);
        almacen.setNombre("Almacén " + codigoSede);
        almacen.setCiudad("Medellín");
        almacen.setDireccion("Calle 67 # 53-108");
        almacen.setCreatedAt(FECHA);
        return almacen;
    }

    public static Producto producto(Long id, String sku, String precio) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setSku(sku);
        producto.setNombre("Producto " + sku);
        producto.setCategoria("Tecnología");
        producto.setUnidadMedida(UnidadMedida.UNIDAD);
        producto.setPrecioUnitario(new BigDecimal(precio));
        return producto;
    }

    public static Inventario inventario(Long id, Almacen almacen, Producto producto, int cantidad, int stockMinimo) {
        Inventario inventario = new Inventario(almacen, producto, cantidad, stockMinimo);
        inventario.setId(id);
        inventario.setFechaIngreso(FECHA);
        return inventario;
    }

    public static InventarioItemRequest itemRequest(String sku, int cantidad) {
        return new InventarioItemRequest(sku, "Producto " + sku, null, "Tecnología", UnidadMedida.UNIDAD,
                new BigDecimal("1000.00"), cantidad, 5);
    }

    public static InventarioItemResponse itemResponse(Long productoId, int cantidad, int stockMinimo) {
        return new InventarioItemResponse(10L + productoId, 1L, productoId, "SKU-" + productoId, "Producto " + productoId,
                "Tecnología", UnidadMedida.UNIDAD, new BigDecimal("1000.00"), cantidad, stockMinimo,
                cantidad <= stockMinimo, FECHA);
    }

    public static AlmacenResponse almacenResponse(Long id, String codigoSede) {
        return AlmacenResponse.from(almacen(id, codigoSede));
    }
}
