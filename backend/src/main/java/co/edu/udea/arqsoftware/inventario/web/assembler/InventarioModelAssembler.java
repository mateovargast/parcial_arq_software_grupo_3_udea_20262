package co.edu.udea.arqsoftware.inventario.web.assembler;

import co.edu.udea.arqsoftware.inventario.web.controller.AlmacenController;
import co.edu.udea.arqsoftware.inventario.web.controller.InventarioController;
import co.edu.udea.arqsoftware.inventario.web.controller.ProductoController;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemResponse;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemV2Response;
import co.edu.udea.arqsoftware.inventario.web.dto.ResumenInventarioResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class InventarioModelAssembler {

    public EntityModel<InventarioItemResponse> toModelV1(InventarioItemResponse item) {
        return EntityModel.of(item, enlacesItem(item.productoId(), item.almacenId()));
    }

    public EntityModel<InventarioItemV2Response> toModelV2(InventarioItemV2Response item) {
        return EntityModel.of(item, enlacesItem(item.productoId(), item.almacenId()));
    }

    public EntityModel<ResumenInventarioResponse> toModel(ResumenInventarioResponse resumen) {
        return EntityModel.of(resumen,
                linkTo(methodOn(InventarioController.class).resumir(resumen.almacenId())).withSelfRel(),
                linkTo(methodOn(InventarioController.class).consultarV2(resumen.almacenId(), null)).withRel("inventario"),
                linkTo(methodOn(AlmacenController.class).obtener(resumen.almacenId())).withRel("almacen"));
    }

    public Link enlaceInventario(Long sedeId) {
        return linkTo(methodOn(InventarioController.class).consultarV1(sedeId, null)).withRel("inventario");
    }

    public Link enlaceResumen(Long sedeId) {
        return linkTo(methodOn(InventarioController.class).resumir(sedeId)).withRel("resumen");
    }

    public Link enlaceAlmacen(Long sedeId) {
        return linkTo(methodOn(AlmacenController.class).obtener(sedeId)).withRel("almacen");
    }

    private Link[] enlacesItem(Long productoId, Long almacenId) {
        return new Link[]{
                linkTo(methodOn(ProductoController.class).obtener(productoId)).withRel("producto"),
                enlaceAlmacen(almacenId)
        };
    }
}
