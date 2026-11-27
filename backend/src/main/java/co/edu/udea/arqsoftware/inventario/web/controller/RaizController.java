package co.edu.udea.arqsoftware.inventario.web.controller;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api")
public class RaizController {

    @GetMapping
    public ResponseEntity<RepresentationModel<?>> raiz() {
        RepresentationModel<?> modelo = new RepresentationModel<>();
        modelo.add(linkTo(methodOn(RaizController.class).raiz()).withSelfRel());
        modelo.add(linkTo(methodOn(AlmacenController.class).listar(null)).withRel("almacenes"));
        modelo.add(linkTo(methodOn(AlmacenController.class).crear(null)).withRel("crear-almacen"));
        return ResponseEntity.ok(modelo);
    }
}
