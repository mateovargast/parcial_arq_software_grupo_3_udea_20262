package co.edu.udea.arqsoftware.inventario.web;

import co.edu.udea.arqsoftware.inventario.domain.InventoryItem;
import co.edu.udea.arqsoftware.inventario.service.InventoryService;
import co.edu.udea.arqsoftware.inventario.service.PageRequestFactory;
import co.edu.udea.arqsoftware.inventario.web.dto.InventoryResponse;
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
@RequestMapping(value = "/api", version = "1")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/warehouses/{id}/inventory")
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true, example = "1")
    public PagedModel<EntityModel<InventoryResponse>> byWarehouse(
            @PathVariable Long id,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequestFactory.create(page, size, "product.nombre");
        Page<InventoryItem> results = inventoryService.findByWarehouse(id, q, pageable);
        List<EntityModel<InventoryResponse>> content = results.getContent().stream()
                .map(this::inventoryModel)
                .toList();
        return PagedModelFactory.from(
                results,
                content,
                pageNumber -> WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(InventoryController.class)
                        .byWarehouse(id, q, pageNumber, size)).withSelfRel());
    }

    @GetMapping("/products/{id}/inventory")
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true, example = "1")
    public PagedModel<EntityModel<InventoryResponse>> byProduct(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequestFactory.create(page, size, "warehouse.nombre");
        Page<InventoryItem> results = inventoryService.findByProduct(id, pageable);
        List<EntityModel<InventoryResponse>> content = results.getContent().stream()
                .map(this::inventoryModel)
                .toList();
        return PagedModelFactory.from(
                results,
                content,
                pageNumber -> WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(InventoryController.class)
                        .byProduct(id, pageNumber, size)).withSelfRel());
    }

    private EntityModel<InventoryResponse> inventoryModel(InventoryItem item) {
        InventoryResponse response = InventoryResponse.from(item);
        return EntityModel.of(response,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class)
                        .getById(item.getProduct().getId())).withRel("product"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WarehouseController.class)
                        .getById(item.getWarehouse().getId())).withRel("warehouse"));
    }
}
