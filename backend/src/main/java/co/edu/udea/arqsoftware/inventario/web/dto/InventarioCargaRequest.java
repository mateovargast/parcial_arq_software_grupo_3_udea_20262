package co.edu.udea.arqsoftware.inventario.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InventarioCargaRequest(
        @NotEmpty @Size(max = 500) List<@Valid InventarioItemRequest> productos
) {
}
