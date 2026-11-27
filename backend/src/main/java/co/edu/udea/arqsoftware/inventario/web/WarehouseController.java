package co.edu.udea.arqsoftware.inventario.web;

import co.edu.udea.arqsoftware.inventario.domain.Warehouse;
import co.edu.udea.arqsoftware.inventario.service.PageRequestFactory;
import co.edu.udea.arqsoftware.inventario.service.WarehouseService;
import co.edu.udea.arqsoftware.inventario.web.dto.WarehouseResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/warehouses", version = "1")
public class WarehouseController {
    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true, example = "1")
    public PagedModel<EntityModel<WarehouseResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q) {
        PageRequest pageable = PageRequestFactory.create(page, size, "nombre");
        Page<Warehouse> results = warehouseService.findAll(q, pageable);
        List<EntityModel<WarehouseResponse>> content = results.getContent().stream()
                .map(this::warehouseModel)
                .toList();
        return PagedModelFactory.from(
                results,
                content,
                pageNumber -> WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WarehouseController.class)
                        .list(pageNumber, size, q)).withSelfRel());
    }

    @GetMapping("/{id}")
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true, example = "1")
    public EntityModel<WarehouseResponse> getById(@PathVariable Long id) {
        return warehouseModel(warehouseService.getById(id));
    }

    private EntityModel<WarehouseResponse> warehouseModel(Warehouse warehouse) {
        WarehouseResponse response = WarehouseResponse.from(warehouse);
        return EntityModel.of(response,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WarehouseController.class)
                        .getById(warehouse.getId())).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(InventoryController.class)
                        .byWarehouse(warehouse.getId(), null, 0, 10)).withRel("inventory"));
    }
}
