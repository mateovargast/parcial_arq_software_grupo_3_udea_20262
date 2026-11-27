package co.edu.udea.arqsoftware.inventario.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductBatchItemRequest(
        @NotBlank @Size(max = 40) String sku,
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 500) String descripcion,
        @NotBlank @Size(max = 80) String categoria,
        @NotBlank @Size(max = 20) String unidadMedida,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precioUnitario,
        @Min(0) int cantidadInicial,
        @Min(0) int stockMinimo) {
}
