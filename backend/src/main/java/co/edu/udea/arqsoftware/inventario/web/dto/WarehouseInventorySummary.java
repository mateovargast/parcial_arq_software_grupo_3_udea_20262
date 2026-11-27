package co.edu.udea.arqsoftware.inventario.web.dto;

import java.io.Serializable;

public record WarehouseInventorySummary(
        Long almacenId,
        String almacen,
        Long totalUnidades,
        Long productos,
        Long stockBajo) implements Serializable {
}
