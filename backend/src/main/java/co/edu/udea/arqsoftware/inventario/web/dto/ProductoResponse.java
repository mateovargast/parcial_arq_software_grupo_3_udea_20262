package co.edu.udea.arqsoftware.inventario.web.dto;

import co.edu.udea.arqsoftware.inventario.domain.Producto;
import co.edu.udea.arqsoftware.inventario.domain.UnidadMedida;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String sku,
        String nombre,
        String descripcion,
        String categoria,
        UnidadMedida unidadMedida,
        BigDecimal precioUnitario
) {

    public static ProductoResponse from(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getSku(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoria(),
                producto.getUnidadMedida(),
                producto.getPrecioUnitario());
    }
}
