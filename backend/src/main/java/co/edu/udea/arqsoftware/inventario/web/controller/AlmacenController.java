package co.edu.udea.arqsoftware.inventario.web.controller;

import co.edu.udea.arqsoftware.inventario.service.AlmacenService;
import co.edu.udea.arqsoftware.inventario.web.assembler.AlmacenModelAssembler;
import co.edu.udea.arqsoftware.inventario.web.dto.AlmacenRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.AlmacenResponse;
import co.edu.udea.arqsoftware.inventario.web.error.ProblemaResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Almacenes", description = "Gestión de las sedes (almacenes)")
@RestController
@RequestMapping("/api/almacenes")
@RequiredArgsConstructor
public class AlmacenController {

    private final AlmacenService almacenService;
    private final AlmacenModelAssembler assembler;
    private final PagedResourcesAssembler<AlmacenResponse> pagedAssembler;

    @Operation(operationId = "crearAlmacen", summary = "Crear una sede (almacén)")
    @ApiResponse(responseCode = "201", description = "Sede creada; el header Location apunta al nuevo recurso")
    @ProblemaResponses.BadRequest
    @ProblemaResponses.Conflict
    @PostMapping
    public ResponseEntity<EntityModel<AlmacenResponse>> crear(@Valid @RequestBody AlmacenRequest request) {
        EntityModel<AlmacenResponse> modelo = assembler.toModel(almacenService.crear(request));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }

    @Operation(operationId = "listarAlmacenes", summary = "Listar sedes (paginado)")
    @ApiResponse(responseCode = "200", description = "Página de sedes con enlaces HATEOAS")
    @ProblemaResponses.BadRequest
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<AlmacenResponse>>> listar(
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(pagedAssembler.toModel(almacenService.listar(pageable).toPage(pageable.getSort()), assembler));
    }

    @Operation(operationId = "obtenerAlmacen", summary = "Obtener una sede por su ID")
    @ApiResponse(responseCode = "200", description = "Sede encontrada")
    @ProblemaResponses.BadRequest
    @ProblemaResponses.NotFound
    @GetMapping("/{sedeId}")
    public ResponseEntity<EntityModel<AlmacenResponse>> obtener(
            @Parameter(description = "ID de la sede del almacén", example = "1") @PathVariable Long sedeId) {
        return ResponseEntity.ok(assembler.toModel(almacenService.obtener(sedeId)));
    }
}
