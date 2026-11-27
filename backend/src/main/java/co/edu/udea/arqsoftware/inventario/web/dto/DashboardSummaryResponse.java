package co.edu.udea.arqsoftware.inventario.web.dto;

import java.io.Serializable;
import java.util.List;

public record DashboardSummaryResponse(
        long almacenes,
        long productos,
        long totalUnidades,
        long productosStockBajo,
        List<WarehouseInventorySummary> inventarioPorAlmacen) implements Serializable {
}
