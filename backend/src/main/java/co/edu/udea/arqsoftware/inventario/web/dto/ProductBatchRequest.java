package co.edu.udea.arqsoftware.inventario.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProductBatchRequest(
        @NotNull @Positive Long almacenId,
        @NotEmpty @Size(max = 100) List<@Valid ProductBatchItemRequest> productos) {
}
