package co.edu.udea.arqsoftware.inventario.service;

import co.edu.udea.arqsoftware.inventario.config.CacheConfig;
import co.edu.udea.arqsoftware.inventario.domain.Almacen;
import co.edu.udea.arqsoftware.inventario.domain.Inventario;
import co.edu.udea.arqsoftware.inventario.domain.Producto;
import co.edu.udea.arqsoftware.inventario.observability.InventarioMetrics;
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
import co.edu.udea.arqsoftware.inventario.web.dto.ResumenInventarioResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventarioService {

    private final AlmacenRepository almacenRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final InventarioMetrics metrics;

    @Cacheable(cacheNames = CacheConfig.CACHE_INVENTARIO,
            key = "#sedeId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public PageResponse<InventarioItemResponse> consultar(Long sedeId, Pageable pageable) {
        validarAlmacenExiste(sedeId);
        log.debug("Consultando inventario en base de datos: sede={}, pagina={}", sedeId, pageable);
        return PageResponse.from(inventarioRepository.findByAlmacenId(sedeId, pageable)
                .map(InventarioItemResponse::from));
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_RESUMEN, key = "#sedeId")
    public ResumenInventarioResponse resumir(Long sedeId) {
        validarAlmacenExiste(sedeId);
        return ResumenInventarioResponse.from(sedeId, inventarioRepository.resumirPorAlmacen(sedeId));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.CACHE_INVENTARIO, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.CACHE_RESUMEN, key = "#sedeId"),
            @CacheEvict(cacheNames = CacheConfig.CACHE_DASHBOARD, allEntries = true)
    })
    @Transactional
    public List<InventarioItemResponse> cargar(Long sedeId, InventarioCargaRequest request) {
        Almacen almacen = almacenRepository.findById(sedeId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Almacén", sedeId));

        List<InventarioItemRequest> items = request.productos();
        Set<String> skus = validarSkusUnicos(items);

        List<String> yaEnAlmacen = inventarioRepository.findSkusExistentesEnAlmacen(sedeId, skus);
        if (!yaEnAlmacen.isEmpty()) {
            metrics.registrarCargaRechazada("sku_existente_en_sede");
            log.warn("Carga rechazada en sede {}: SKU ya registrados {}", almacen.getCodigoSede(), yaEnAlmacen);
            throw new ConflictoException("Los siguientes SKU ya están registrados en el almacén "
                    + almacen.getCodigoSede() + ": " + String.join(", ", yaEnAlmacen));
        }

        Map<String, Producto> existentes = productoRepository.findBySkuIn(skus).stream()
                .collect(Collectors.toMap(Producto::getSku, Function.identity()));

        int productosNuevos = skus.size() - existentes.size();

        List<Inventario> nuevos = items.stream()
                .map(item -> new Inventario(
                        almacen,
                        existentes.computeIfAbsent(item.sku(), sku -> productoRepository.save(nuevoProducto(item))),
                        item.cantidadInicial(),
                        Objects.requireNonNullElse(item.stockMinimo(), 0)))
                .toList();

        List<InventarioItemResponse> creados = inventarioRepository.saveAll(nuevos).stream()
                .map(InventarioItemResponse::from)
                .toList();

        log.info("Carga de inventario en sede {}: {} productos registrados ({} nuevos en catálogo)",
                almacen.getCodigoSede(), creados.size(), productosNuevos);
        alConfirmar(() -> metrics.registrarCarga(almacen.getCodigoSede(), creados.size(), productosNuevos));
        return creados;
    }

    private void alConfirmar(Runnable accion) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            accion.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                accion.run();
            }
        });
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
            metrics.registrarCargaRechazada("sku_repetido_en_solicitud");
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
