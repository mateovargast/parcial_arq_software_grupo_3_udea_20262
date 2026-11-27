package co.edu.udea.arqsoftware.inventario.web.dto;

import co.edu.udea.arqsoftware.inventario.domain.Inventario;
import co.edu.udea.arqsoftware.inventario.domain.Producto;
import co.edu.udea.arqsoftware.inventario.domain.UnidadMedida;

import java.math.BigDecimal;
import java.time.Instant;

public record InventarioItemResponse(
        Long inventarioId,
        Long almacenId,
        Long productoId,
        String sku,
        String nombre,
        String categoria,
        UnidadMedida unidadMedida,
        BigDecimal precioUnitario,
        Integer cantidad,
        Integer stockMinimo,
        boolean stockBajo,
        Instant fechaIngreso
) {

    public static InventarioItemResponse from(Inventario inventario) {
        Producto producto = inventario.getProducto();
        return new InventarioItemResponse(
                inventario.getId(),
                inventario.getAlmacen().getId(),
                producto.getId(),
                producto.getSku(),
                producto.getNombre(),
                producto.getCategoria(),
                producto.getUnidadMedida(),
                producto.getPrecioUnitario(),
                inventario.getCantidad(),
                inventario.getStockMinimo(),
                inventario.isStockBajo(),
                inventario.getFechaIngreso());
    }
}
