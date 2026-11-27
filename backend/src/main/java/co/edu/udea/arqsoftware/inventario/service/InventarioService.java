package co.edu.udea.arqsoftware.inventario.service;

import co.edu.udea.arqsoftware.inventario.domain.Almacen;
import co.edu.udea.arqsoftware.inventario.domain.Inventario;
import co.edu.udea.arqsoftware.inventario.domain.Producto;
import co.edu.udea.arqsoftware.inventario.repository.AlmacenRepository;
import co.edu.udea.arqsoftware.inventario.repository.InventarioRepository;
import co.edu.udea.arqsoftware.inventario.repository.ProductoRepository;
import co.edu.udea.arqsoftware.inventario.service.exception.ConflictoException;
import co.edu.udea.arqsoftware.inventario.service.exception.RecursoNoEncontradoException;
import co.edu.udea.arqsoftware.inventario.service.exception.SolicitudInvalidaException;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioCargaRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemRequest;
import co.edu.udea.arqsoftware.inventario.web.dto.InventarioItemResponse;
import co.edu.udea.arqsoftware.inventario.web.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventarioService {

    private final AlmacenRepository almacenRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;

    public PageResponse<InventarioItemResponse> consultar(Long sedeId, Pageable pageable) {
        validarAlmacenExiste(sedeId);
        return PageResponse.from(inventarioRepository.findByAlmacenId(sedeId, pageable)
                .map(InventarioItemResponse::from));
    }

    @Transactional
    public List<InventarioItemResponse> cargar(Long sedeId, InventarioCargaRequest request) {
        Almacen almacen = almacenRepository.findById(sedeId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Almacén", sedeId));

        List<InventarioItemRequest> items = request.productos();
        Set<String> skus = validarSkusUnicos(items);

        List<String> yaEnAlmacen = inventarioRepository.findSkusExistentesEnAlmacen(sedeId, skus);
        if (!yaEnAlmacen.isEmpty()) {
            throw new ConflictoException("Los siguientes SKU ya están registrados en el almacén "
                    + almacen.getCodigoSede() + ": " + String.join(", ", yaEnAlmacen));
        }

        Map<String, Producto> existentes = productoRepository.findBySkuIn(skus).stream()
                .collect(Collectors.toMap(Producto::getSku, Function.identity()));

        List<Inventario> nuevos = items.stream()
                .map(item -> new Inventario(
                        almacen,
                        existentes.computeIfAbsent(item.sku(), sku -> productoRepository.save(nuevoProducto(item))),
                        item.cantidadInicial(),
                        Objects.requireNonNullElse(item.stockMinimo(), 0)))
                .toList();

        return inventarioRepository.saveAll(nuevos).stream()
                .map(InventarioItemResponse::from)
                .toList();
    }

    private void validarAlmacenExiste(Long sedeId) {
        if (!almacenRepository.existsById(sedeId)) {
            throw new RecursoNoEncontradoException("Almacén", sedeId);
        }
    }

    private Set<String> validarSkusUnicos(List<InventarioItemRequest> items) {
        Set<String> skus = new HashSet<>();
        Set<String> duplicados = items.stream()
                .map(InventarioItemRequest::sku)
                .filter(sku -> !skus.add(sku))
                .collect(Collectors.toSet());
        if (!duplicados.isEmpty()) {
            throw new SolicitudInvalidaException("SKU repetidos en la solicitud: " + String.join(", ", duplicados));
        }
        return skus;
    }

    private Producto nuevoProducto(InventarioItemRequest item) {
        Producto producto = new Producto();
        producto.setSku(item.sku());
        producto.setNombre(item.nombre());
        producto.setDescripcion(item.descripcion());
        producto.setCategoria(item.categoria());
        producto.setUnidadMedida(item.unidadMedida());
        producto.setPrecioUnitario(item.precioUnitario());
        return producto;
    }
}
