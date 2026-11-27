package co.edu.udea.arqsoftware.inventario.web.dto;

import co.edu.udea.arqsoftware.inventario.domain.InventoryItem;
import java.time.Instant;
import org.springframework.hateoas.server.core.Relation;

@Relation(itemRelation = "inventoryItem", collectionRelation = "inventory")
public record InventoryResponse(
        Long id,
        Long almacenId,
        Long productoId,
        ProductResponse producto,
        int cantidad,
        int stockMinimo,
        Instant fechaIngreso,
        Instant updatedAt) {
    public static InventoryResponse from(InventoryItem item) {
        return new InventoryResponse(
                item.getId(),
                item.getWarehouse().getId(),
                item.getProduct().getId(),
                ProductResponse.from(item.getProduct()),
                item.getCantidad(),
                item.getStockMinimo(),
                item.getFechaIngreso(),
                item.getUpdatedAt());
    }
}
