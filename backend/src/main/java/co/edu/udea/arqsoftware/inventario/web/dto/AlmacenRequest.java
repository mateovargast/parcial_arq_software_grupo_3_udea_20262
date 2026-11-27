package co.edu.udea.arqsoftware.inventario.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AlmacenRequest(
        @NotBlank @Size(max = 20) @Pattern(regexp = "^[A-Z0-9-]+$", message = "solo admite mayúsculas, números y guiones")
        String codigoSede,
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 80) String ciudad,
        @NotBlank @Size(max = 200) String direccion,
        @Size(max = 30) String telefono
) {
}
