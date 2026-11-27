package co.edu.udea.arqsoftware.inventario.web.controller;

import co.edu.udea.arqsoftware.inventario.service.InventarioService;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioCargaRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/almacenes/{sedeId}/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<PageResponse<InventarioItemResponse>> consultar(
            @PathVariable Long sedeId,
            @PageableDefault(sort = "producto.nombre") Pageable pageable) {
        return ResponseEntity.ok(inventarioService.consultar(sedeId, pageable));
    }

    @PostMapping
    public ResponseEntity<List<InventarioItemResponse>> cargar(
            @PathVariable Long sedeId,
            @Valid @RequestBody InventarioCargaRequest request) {
        List<InventarioItemResponse> creados = inventarioService.cargar(sedeId, request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().build().toUri())
                .body(creados);
    }
}
