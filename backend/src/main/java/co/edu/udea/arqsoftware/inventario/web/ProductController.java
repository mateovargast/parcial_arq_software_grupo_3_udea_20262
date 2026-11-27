package co.edu.udea.arqsoftware.inventario.web;

import co.edu.udea.arqsoftware.inventario.domain.Product;
import co.edu.udea.arqsoftware.inventario.service.PageRequestFactory;
import co.edu.udea.arqsoftware.inventario.service.ProductService;
import co.edu.udea.arqsoftware.inventario.web.dto.ProductBatchRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/products", version = "1")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true, example = "1")
    public PagedModel<EntityModel<ProductResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q) {
        PageRequest pageable = PageRequestFactory.create(page, size, "nombre");
        Page<Product> results = productService.findAll(q, pageable);
        List<EntityModel<ProductResponse>> content = results.getContent().stream()
                .map(this::productModel)
                .toList();
        return PagedModelFactory.from(
                results,
                content,
                pageNumber -> WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class)
                        .list(pageNumber, size, q)).withSelfRel());
    }

    @GetMapping("/{id}")
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true, example = "1")
    public EntityModel<ProductResponse> getById(@PathVariable Long id) {
        return productModel(productService.getById(id));
    }

    @PostMapping("/batch")
    @ApiResponse(responseCode = "201", description = "Productos e inventario inicial creados.")
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = true, example = "1")
    public ResponseEntity<CollectionModel<EntityModel<ProductResponse>>> registerBatch(
            @Valid @RequestBody ProductBatchRequest request) {
        List<EntityModel<ProductResponse>> products = productService.registerBatch(request).stream()
                .map(this::productModel)
                .toList();
        CollectionModel<EntityModel<ProductResponse>> response = CollectionModel.of(
                products,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class)
                        .list(0, 10, null)).withRel("products"));
        return ResponseEntity.created(
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class)
                        .list(0, 10, null)).toUri())
                .body(response);
    }

    private EntityModel<ProductResponse> productModel(Product product) {
        ProductResponse response = ProductResponse.from(product);
        return EntityModel.of(response,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ProductController.class)
                        .getById(product.getId())).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(InventoryController.class)
                        .byProduct(product.getId(), 0, 10)).withRel("inventory"));
    }
}
