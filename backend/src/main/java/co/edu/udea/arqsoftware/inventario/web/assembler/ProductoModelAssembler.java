package co.edu.udea.arqsoftware.inventario.web.assembler;

import co.edu.udea.arqsoftware.inventario.web.controller.ProductoController;
import co.edu.udea.arqsoftware.inventario.web.dto.ProductoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductoModelAssembler implements RepresentationModelAssembler<ProductoResponse, EntityModel<ProductoResponse>> {

    @Override
    public EntityModel<ProductoResponse> toModel(ProductoResponse producto) {
        return EntityModel.of(producto,
                linkTo(methodOn(ProductoController.class).obtener(producto.id())).withSelfRel());
    }
}
