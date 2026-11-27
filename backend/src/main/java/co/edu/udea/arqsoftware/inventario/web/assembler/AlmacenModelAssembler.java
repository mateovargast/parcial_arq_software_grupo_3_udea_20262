package co.edu.udea.arqsoftware.inventario.web.assembler;

import co.edu.udea.arqsoftware.inventario.web.controller.AlmacenController;
import co.edu.udea.arqsoftware.inventario.web.controller.InventarioController;
import co.edu.udea.arqsoftware.inventario.web.dto.AlmacenResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AlmacenModelAssembler implements RepresentationModelAssembler<AlmacenResponse, EntityModel<AlmacenResponse>> {

    @Override
    public EntityModel<AlmacenResponse> toModel(AlmacenResponse almacen) {
        return EntityModel.of(almacen,
                linkTo(methodOn(AlmacenController.class).obtener(almacen.id())).withSelfRel(),
                linkTo(methodOn(InventarioController.class).consultarV1(almacen.id(), null)).withRel("inventario"),
                linkTo(methodOn(InventarioController.class).cargar(almacen.id(), null)).withRel("cargar-inventario"),
                linkTo(methodOn(AlmacenController.class).listar(null)).withRel("almacenes"));
    }
}
