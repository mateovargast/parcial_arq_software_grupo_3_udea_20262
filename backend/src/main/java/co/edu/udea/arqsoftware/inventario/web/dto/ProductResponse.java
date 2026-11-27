package co.edu.udea.arqsoftware.inventario.web.dto;

import co.edu.udea.arqsoftware.inventario.domain.Product;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.hateoas.server.core.Relation;

@Relation(itemRelation = "product", collectionRelation = "products")
public record ProductResponse(
        Long id,
        String sku,
        String nombre,
        String descripcion,
        String categoria,
        String unidadMedida,
        BigDecimal precioUnitario,
        Instant createdAt,
        Instant updatedAt) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getNombre(),
                product.getDescripcion(),
                product.getCategoria(),
                product.getUnidadMedida(),
                product.getPrecioUnitario(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
