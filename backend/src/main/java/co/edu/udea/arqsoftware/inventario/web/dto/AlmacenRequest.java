package co.edu.udea.arqsoftware.inventario.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AlmacenRequest(
        @Schema(description = "Código único de la sede", example = "BAQ-01")
        @NotBlank @Size(max = 20) @Pattern(regexp = "^[A-Z0-9-]+$", message = "solo admite mayúsculas, números y guiones")
        String codigoSede,
        @Schema(example = "Almacén Barranquilla Centro") @NotBlank @Size(max = 120) String nombre,
        @Schema(example = "Barranquilla") @NotBlank @Size(max = 80) String ciudad,
        @Schema(example = "Calle 72 # 50-10") @NotBlank @Size(max = 200) String direccion,
        @Schema(example = "6053851000") @Size(max = 30) String telefono
) {
}
