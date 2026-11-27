package co.edu.udea.arqsoftware.inventario.web.controller;

import co.edu.udea.arqsoftware.inventario.service.ProductoService;
import co.edu.udea.arqsoftware.inventario.web.assembler.ProductoModelAssembler;
import co.edu.udea.arqsoftware.inventario.web.dto.ProductoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoModelAssembler assembler;

    @GetMapping("/{productoId}")
    public ResponseEntity<EntityModel<ProductoResponse>> obtener(@PathVariable Long productoId) {
        return ResponseEntity.ok(assembler.toModel(productoService.obtener(productoId)));
    }
}
