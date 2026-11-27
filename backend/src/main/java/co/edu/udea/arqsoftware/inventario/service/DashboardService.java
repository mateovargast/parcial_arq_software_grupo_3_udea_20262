package co.edu.udea.arqsoftware.inventario.service;

import co.edu.udea.arqsoftware.inventario.repository.InventoryRepository;
import co.edu.udea.arqsoftware.inventario.repository.ProductRepository;
import co.edu.udea.arqsoftware.inventario.repository.WarehouseRepository;
import co.edu.udea.arqsoftware.inventario.repository.WarehouseInventoryAggregate;
import co.edu.udea.arqsoftware.inventario.web.dto.DashboardSummaryResponse;
import co.edu.udea.arqsoftware.inventario.web.dto.WarehouseInventorySummary;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public DashboardService(
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository) {
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Cacheable("dashboardSummary")
    public DashboardSummaryResponse getSummary() {
        List<WarehouseInventorySummary> warehouseSummaries = inventoryRepository.summarizeByWarehouse().stream()
                .map(this::toSummary)
                .toList();
        return new DashboardSummaryResponse(
                warehouseRepository.count(),
                productRepository.count(),
                inventoryRepository.sumAllUnits(),
                inventoryRepository.countLowStock(),
                warehouseSummaries);
    }

    private WarehouseInventorySummary toSummary(WarehouseInventoryAggregate aggregate) {
        return new WarehouseInventorySummary(
                aggregate.getAlmacenId(),
                aggregate.getAlmacen(),
                aggregate.getTotalUnidades(),
                aggregate.getProductos(),
                aggregate.getStockBajo());
    }
}
