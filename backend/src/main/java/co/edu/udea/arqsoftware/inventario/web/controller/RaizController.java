package co.edu.udea.arqsoftware.inventario.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Raíz", description = "Punto de entrada HATEOAS de la API")
@RestController
@RequestMapping("/api")
public class RaizController {

    @Operation(operationId = "raiz", summary = "Enlaces de entrada a la API")
    @ApiResponse(responseCode = "200", description = "Enlaces disponibles")
    @GetMapping
    public ResponseEntity<RepresentationModel<?>> raiz() {
        RepresentationModel<?> modelo = new RepresentationModel<>();
        modelo.add(linkTo(methodOn(RaizController.class).raiz()).withSelfRel());
        modelo.add(linkTo(methodOn(AlmacenController.class).listar(null)).withRel("almacenes"));
        modelo.add(linkTo(methodOn(AlmacenController.class).crear(null)).withRel("crear-almacen"));
        return ResponseEntity.ok(modelo);
    }
}
