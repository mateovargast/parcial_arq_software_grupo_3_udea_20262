package co.edu.udea.arqsoftware.inventario.web.controller;

import co.edu.udea.arqsoftware.inventario.service.AlmacenService;
import co.edu.udea.arqsoftware.inventario.web.dto.AlmacenRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.AlmacenResponse;
import co.edu.udea.arqsoftware.inventario.web.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/almacenes")
@RequiredArgsConstructor
public class AlmacenController {

    private final AlmacenService almacenService;

    @PostMapping
    public ResponseEntity<AlmacenResponse> crear(@Valid @RequestBody AlmacenRequest request) {
        AlmacenResponse creado = almacenService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{sedeId}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @GetMapping
    public ResponseEntity<PageResponse<AlmacenResponse>> listar(@PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(almacenService.listar(pageable));
    }

    @GetMapping("/{sedeId}")
    public ResponseEntity<AlmacenResponse> obtener(@PathVariable Long sedeId) {
        return ResponseEntity.ok(almacenService.obtener(sedeId));
    }
}
