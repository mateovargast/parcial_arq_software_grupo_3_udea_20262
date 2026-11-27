package co.edu.udea.arqsoftware.inventario.web.dto;

import co.edu.udea.arqsoftware.inventario.domain.UnidadMedida;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.Instant;

@Relation(itemRelation = "item", collectionRelation = "inventario")
public record InventarioItemV2Response(
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
        BigDecimal valorTotal,
        EstadoStock estadoStock,
        @JsonInclude(JsonInclude.Include.NON_NULL) String alerta,
        Instant fechaIngreso
) {

    public enum EstadoStock {
        AGOTADO,
        BAJO,
        NORMAL
    }

    public static InventarioItemV2Response from(InventarioItemResponse v1) {
        EstadoStock estado = v1.cantidad() == 0 ? EstadoStock.AGOTADO
                : v1.stockBajo() ? EstadoStock.BAJO
                : EstadoStock.NORMAL;
        String alerta = switch (estado) {
            case AGOTADO -> "Producto agotado: reabastecer de inmediato";
            case BAJO -> "Stock bajo: faltan %d unidades para superar el mínimo"
                    .formatted(v1.stockMinimo() - v1.cantidad() + 1);
            case NORMAL -> null;
        };
        return new InventarioItemV2Response(
                v1.inventarioId(),
                v1.almacenId(),
                v1.productoId(),
                v1.sku(),
                v1.nombre(),
                v1.categoria(),
                v1.unidadMedida(),
                v1.precioUnitario(),
                v1.cantidad(),
                v1.stockMinimo(),
                v1.precioUnitario().multiply(BigDecimal.valueOf(v1.cantidad())),
                estado,
                alerta,
                v1.fechaIngreso());
    }
}
