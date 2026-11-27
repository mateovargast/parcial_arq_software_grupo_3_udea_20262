package co.edu.udea.arqsoftware.inventario.observability;

import co.edu.udea.arqsoftware.inventario.repository.AlmacenRepository;
import co.edu.udea.arqsoftware.inventario.repository.InventarioRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class InventarioMetrics {

    private final MeterRegistry registry;
    private final Counter productosCatalogoCreados;

    public InventarioMetrics(MeterRegistry registry,
                             AlmacenRepository almacenRepository,
                             InventarioRepository inventarioRepository) {
        this.registry = registry;
        this.productosCatalogoCreados = Counter.builder("inventario.catalogo.productos.creados")
                .description("Productos nuevos creados en el catálogo")
                .register(registry);

        Gauge.builder("inventario.almacenes", almacenRepository, AlmacenRepository::count)
                .description("Cantidad de almacenes (sedes) registrados")
                .register(registry);
        Gauge.builder("inventario.registros", inventarioRepository, InventarioRepository::count)
                .description("Cantidad de registros de inventario (producto por sede)")
                .register(registry);
        Gauge.builder("inventario.stock.bajo", inventarioRepository, InventarioRepository::contarStockBajo)
                .description("Registros de inventario con cantidad menor o igual al stock mínimo")
                .register(registry);
    }

    public void registrarCarga(String codigoSede, int productosRegistrados, int productosNuevos) {
        Counter.builder("inventario.productos.registrados")
                .description("Productos registrados en el inventario de una sede")
                .tag("sede", codigoSede)
                .register(registry)
                .increment(productosRegistrados);
        productosCatalogoCreados.increment(productosNuevos);
    }

    public void registrarCargaRechazada(String motivo) {
        Counter.builder("inventario.cargas.rechazadas")
                .description("Cargas de inventario rechazadas por reglas de negocio")
                .tag("motivo", motivo)
                .register(registry)
                .increment();
    }
}
