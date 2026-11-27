package co.edu.udea.arqsoftware.inventario.web.dto;

import co.edu.udea.arqsoftware.inventario.repository.ResumenInventarioView;

import java.math.BigDecimal;

public record ResumenInventarioResponse(
        Long almacenId,
        long totalProductos,
        long unidadesTotales,
        BigDecimal valorTotal,
        long productosStockBajo
) {

    public static ResumenInventarioResponse from(Long almacenId, ResumenInventarioView view) {
        return new ResumenInventarioResponse(
                almacenId,
                view.getTotalProductos(),
                view.getUnidadesTotales(),
                view.getValorTotal(),
                view.getProductosStockBajo());
    }
}
