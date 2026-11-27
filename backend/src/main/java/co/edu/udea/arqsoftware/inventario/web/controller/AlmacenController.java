package co.edu.udea.arqsoftware.inventario.web.controller;

import co.edu.udea.arqsoftware.inventario.service.AlmacenService;
import co.edu.udea.arqsoftware.inventario.web.assembler.AlmacenModelAssembler;
import co.edu.udea.arqsoftware.inventario.web.dto.AlmacenRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.AlmacenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/almacenes")
@RequiredArgsConstructor
public class AlmacenController {

    private final AlmacenService almacenService;
    private final AlmacenModelAssembler assembler;
    private final PagedResourcesAssembler<AlmacenResponse> pagedAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<AlmacenResponse>> crear(@Valid @RequestBody AlmacenRequest request) {
        EntityModel<AlmacenResponse> modelo = assembler.toModel(almacenService.crear(request));
        return ResponseEntity.created(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<AlmacenResponse>>> listar(@PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(pagedAssembler.toModel(almacenService.listar(pageable).toPage(pageable.getSort()), assembler));
    }

    @GetMapping("/{sedeId}")
    public ResponseEntity<EntityModel<AlmacenResponse>> obtener(@PathVariable Long sedeId) {
        return ResponseEntity.ok(assembler.toModel(almacenService.obtener(sedeId)));
    }
}
