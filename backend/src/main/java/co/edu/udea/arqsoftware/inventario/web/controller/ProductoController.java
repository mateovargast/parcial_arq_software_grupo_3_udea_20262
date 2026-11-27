package co.edu.udea.arqsoftware.inventario.web.controller;

import co.edu.udea.arqsoftware.inventario.service.ProductoService;
import co.edu.udea.arqsoftware.inventario.web.assembler.ProductoModelAssembler;
import co.edu.udea.arqsoftware.inventario.web.dto.ProductoResponse;
import co.edu.udea.arqsoftware.inventario.web.error.ProblemaResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Productos", description = "Catálogo de productos")
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoModelAssembler assembler;

    @Operation(operationId = "obtenerProducto", summary = "Obtener un producto por su ID")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ProblemaResponses.BadRequest
    @ProblemaResponses.NotFound
    @GetMapping("/{productoId}")
    public ResponseEntity<EntityModel<ProductoResponse>> obtener(
            @Parameter(description = "ID del producto", example = "1") @PathVariable Long productoId) {
        return ResponseEntity.ok(assembler.toModel(productoService.obtener(productoId)));
    }
}
