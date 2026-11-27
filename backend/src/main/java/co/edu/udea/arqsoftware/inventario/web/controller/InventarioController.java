package co.edu.udea.arqsoftware.inventario.web.controller;

import co.edu.udea.arqsoftware.inventario.service.InventarioService;
import co.edu.udea.arqsoftware.inventario.web.assembler.InventarioModelAssembler;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioCargaRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemResponse;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemV2Response;
import co.edu.udea.arqsoftware.inventario.web.dto.ResumenInventarioResponse;
import co.edu.udea.arqsoftware.inventario.web.error.ProblemaResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Inventario", description = "Consulta y carga del inventario de productos de una sede")
@RestController
@RequestMapping("/api/almacenes/{sedeId}/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;
    private final InventarioModelAssembler assembler;
    private final PagedResourcesAssembler<InventarioItemResponse> pagedAssemblerV1;
    private final PagedResourcesAssembler<InventarioItemV2Response> pagedAssemblerV2;

    @Operation(operationId = "consultarInventarioV1", summary = "Consultar inventario de una sede (v1)",
            description = "Lista paginada de los productos y cantidades en stock del almacén identificado por el ID de la sede.")
    @ApiResponse(responseCode = "200", description = "Página de inventario con enlaces HATEOAS")
    @ProblemaResponses.BadRequest
    @ProblemaResponses.NotFound
    @GetMapping(version = "1")
    public ResponseEntity<PagedModel<EntityModel<InventarioItemResponse>>> consultarV1(
            @Parameter(description = "ID de la sede del almacén", example = "1") @PathVariable Long sedeId,
            @ParameterObject @PageableDefault(sort = "producto.nombre") Pageable pageable) {
        Page<InventarioItemResponse> pagina = inventarioService.consultar(sedeId, pageable).toPage(pageable.getSort());
        PagedModel<EntityModel<InventarioItemResponse>> modelo = pagedAssemblerV1.toModel(pagina, assembler::toModelV1);
        modelo.add(assembler.enlaceAlmacen(sedeId));
        return ResponseEntity.ok(modelo);
    }

    @Operation(operationId = "consultarInventarioV2", summary = "Consultar inventario de una sede (v2)",
            description = "Igual que la v1, y además incluye por producto el valor total, el estado del stock (AGOTADO, BAJO, NORMAL) "
                    + "y una alerta de reabastecimiento, más un enlace al resumen del inventario.")
    @ApiResponse(responseCode = "200", description = "Página de inventario enriquecida con enlaces HATEOAS")
    @ProblemaResponses.BadRequest
    @ProblemaResponses.NotFound
    @GetMapping(version = "2")
    public ResponseEntity<PagedModel<EntityModel<InventarioItemV2Response>>> consultarV2(
            @Parameter(description = "ID de la sede del almacén", example = "1") @PathVariable Long sedeId,
            @ParameterObject @PageableDefault(sort = "producto.nombre") Pageable pageable) {
        Page<InventarioItemV2Response> pagina = inventarioService.consultar(sedeId, pageable)
                .toPage(pageable.getSort())
                .map(InventarioItemV2Response::from);
        PagedModel<EntityModel<InventarioItemV2Response>> modelo = pagedAssemblerV2.toModel(pagina, assembler::toModelV2);
        modelo.add(assembler.enlaceAlmacen(sedeId), assembler.enlaceResumen(sedeId));
        return ResponseEntity.ok(modelo);
    }

    @Operation(operationId = "resumirInventario", summary = "Resumen del inventario de una sede (solo v2)",
            description = "Totales de productos, unidades, valor del inventario y cantidad de productos con stock bajo.")
    @ApiResponse(responseCode = "200", description = "Resumen del inventario")
    @ProblemaResponses.BadRequest
    @ProblemaResponses.NotFound
    @GetMapping(path = "/resumen", version = "2")
    public ResponseEntity<EntityModel<ResumenInventarioResponse>> resumir(
            @Parameter(description = "ID de la sede del almacén", example = "1") @PathVariable Long sedeId) {
        return ResponseEntity.ok(assembler.toModel(inventarioService.resumir(sedeId)));
    }

    @Operation(operationId = "cargarInventario", summary = "Registrar productos con su cantidad inicial",
            description = "Carga un lote de productos en el almacén. Si el SKU no existe en el catálogo se crea el producto; "
                    + "si ya existe se reutiliza. Falla con 409 si algún SKU ya está registrado en esa sede.")
    @ApiResponse(responseCode = "201", description = "Productos registrados en el inventario de la sede")
    @ProblemaResponses.BadRequest
    @ProblemaResponses.NotFound
    @ProblemaResponses.Conflict
    @PostMapping
    public ResponseEntity<CollectionModel<EntityModel<InventarioItemResponse>>> cargar(
            @Parameter(description = "ID de la sede del almacén", example = "1") @PathVariable Long sedeId,
            @Valid @RequestBody InventarioCargaRequest request) {
        List<EntityModel<InventarioItemResponse>> creados = inventarioService.cargar(sedeId, request).stream()
                .map(assembler::toModelV1)
                .toList();
        CollectionModel<EntityModel<InventarioItemResponse>> modelo = CollectionModel.of(creados,
                assembler.enlaceInventario(sedeId),
                assembler.enlaceAlmacen(sedeId));
        return ResponseEntity.created(assembler.enlaceInventario(sedeId).expand().toUri()).body(modelo);
    }
}
