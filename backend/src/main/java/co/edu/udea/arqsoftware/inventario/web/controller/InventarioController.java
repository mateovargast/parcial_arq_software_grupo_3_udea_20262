package co.edu.udea.arqsoftware.inventario.web.controller;

import co.edu.udea.arqsoftware.inventario.service.InventarioService;
import co.edu.udea.arqsoftware.inventario.web.assembler.InventarioModelAssembler;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioCargaRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemResponse;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemV2Response;
import co.edu.udea.arqsoftware.inventario.web.dto.ResumenInventarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/almacenes/{sedeId}/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;
    private final InventarioModelAssembler assembler;
    private final PagedResourcesAssembler<InventarioItemResponse> pagedAssemblerV1;
    private final PagedResourcesAssembler<InventarioItemV2Response> pagedAssemblerV2;

    @GetMapping(version = "1")
    public ResponseEntity<PagedModel<EntityModel<InventarioItemResponse>>> consultarV1(
            @PathVariable Long sedeId,
            @PageableDefault(sort = "producto.nombre") Pageable pageable) {
        Page<InventarioItemResponse> pagina = inventarioService.consultar(sedeId, pageable).toPage(pageable.getSort());
        PagedModel<EntityModel<InventarioItemResponse>> modelo = pagedAssemblerV1.toModel(pagina, assembler::toModelV1);
        modelo.add(assembler.enlaceAlmacen(sedeId));
        return ResponseEntity.ok(modelo);
    }

    @GetMapping(version = "2")
    public ResponseEntity<PagedModel<EntityModel<InventarioItemV2Response>>> consultarV2(
            @PathVariable Long sedeId,
            @PageableDefault(sort = "producto.nombre") Pageable pageable) {
        Page<InventarioItemV2Response> pagina = inventarioService.consultar(sedeId, pageable)
                .toPage(pageable.getSort())
                .map(InventarioItemV2Response::from);
        PagedModel<EntityModel<InventarioItemV2Response>> modelo = pagedAssemblerV2.toModel(pagina, assembler::toModelV2);
        modelo.add(assembler.enlaceAlmacen(sedeId), assembler.enlaceResumen(sedeId));
        return ResponseEntity.ok(modelo);
    }

    @GetMapping(path = "/resumen", version = "2")
    public ResponseEntity<EntityModel<ResumenInventarioResponse>> resumir(@PathVariable Long sedeId) {
        return ResponseEntity.ok(assembler.toModel(inventarioService.resumir(sedeId)));
    }

    @PostMapping
    public ResponseEntity<CollectionModel<EntityModel<InventarioItemResponse>>> cargar(
            @PathVariable Long sedeId,
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
