package co.edu.udea.arqsoftware.inventario.web.dto;

import co.edu.udea.arqsoftware.inventario.domain.UnidadMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record InventarioItemRequest(
        @Schema(description = "Código único del producto", example = "TEC-010")
        @NotBlank @Size(max = 40) String sku,
        @Schema(example = "Cable HDMI 2 m") @NotBlank @Size(max = 150) String nombre,
        @Schema(example = "Cable HDMI 2.1 de 2 metros") @Size(max = 500) String descripcion,
        @Schema(example = "Tecnología") @NotBlank @Size(max = 80) String categoria,
        @Schema(example = "UNIDAD") @NotNull UnidadMedida unidadMedida,
        @Schema(example = "25000.00")
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precioUnitario,
        @Schema(description = "Cantidad inicial en stock", example = "50") @NotNull @Min(0) Integer cantidadInicial,
        @Schema(description = "Stock mínimo antes de generar alerta (por defecto 0)", example = "10") @Min(0) Integer stockMinimo
) {
}
